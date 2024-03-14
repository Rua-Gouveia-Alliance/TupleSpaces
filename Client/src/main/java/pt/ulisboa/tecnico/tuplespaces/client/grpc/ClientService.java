package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.client.TupleSpacesObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import static pt.ulisboa.tecnico.tuplespaces.client.ClientMain.debugMessage;

public class ClientService {

    private final int NUM_REPLICAS = 3;
    private final ArrayList<ManagedChannel> channels;
    private final ArrayList<TupleSpacesReplicaStub> stubs;
    private final OrderedDelayer delayer;
    private final int clientId;

    public ClientService(ArrayList<String> targets, int clientId) {
        channels = new ArrayList<>();
        stubs = new ArrayList<>();
        this.clientId = clientId;

        debugMessage("Establishing rpc connection");
        targets.forEach(target -> {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.channels.add(channel);
            this.stubs.add(TupleSpacesReplicaGrpc.newStub(channel));
        });

        /*
         * The delayer can be used to inject delays to the sending of requests to the
         * different servers, according to the per-server delays that have been set
         */
        delayer = new OrderedDelayer(targets.size());
    }

    /*
     * This method allows the command processor to set the request delay assigned to
     * a given server
     */
    public void setDelay(int id, int delay) {
        delayer.setDelay(id, delay);
    }

    public PutResponse put(String tuple) {
        debugMessage("Requesting put operation to TupleSpaces Server of tuple: " + tuple);
        PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
        ResponseCollector<PutResponse> collector = new ResponseCollector<>();

        for (Integer id : delayer)
            stubs.get(id).put(request, new TupleSpacesObserver<>(collector));

        collector.waitForAll();
        return collector.getResponses().get(0);
    }

    public ReadResponse read(String pattern) {
        debugMessage("Requesting read operation to TupleSpaces Server of pattern: " + pattern);
        ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
        ResponseCollector<ReadResponse> collector = new ResponseCollector<>();

        for (Integer id : delayer)
            stubs.get(id).read(request, new TupleSpacesObserver<>(collector));

        collector.waitForFirst();
        return collector.getResponses().get(0);
    }

    public String get_intersection(List<String> list) {
        for (String tuple : list)
            if (Collections.frequency(list, tuple) == NUM_REPLICAS)
                return tuple;

        return null;
    }

    public String take(String pattern) {
        Random generator = new Random();
        String intersection = null;
        int millis = 0;

        do {
            ArrayList<Integer> empties = new ArrayList<>();
            ArrayList<String> tuples = new ArrayList<>();

            // Sending the tuple template to all replicas
            take_p1(pattern).forEach((id, r) -> {
                if (r.getReservedTuplesCount() == 0) {
                    empties.add(id);
                } else {
                    tuples.addAll(r.getReservedTuplesList());
                }
            });
            debugMessage("Empty replicas: " + empties.toString());
            debugMessage("Received tuples: " + tuples.toString());

            // Only a minority was able to return tuples
            if (empties.size() > NUM_REPLICAS / 2) {
                take_p1_release(empties);

                // Increasingly bigger random wait time before repeating take phase 1
                millis += generator.nextInt(1000);
                debugMessage("Waiting for " + millis / 1000.0 + " seconds before retrying phase 1");
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (empties.size() > 0)
                continue;

            intersection = get_intersection(tuples);
            debugMessage("Intersection: " + intersection);
        } while (intersection == null);

        take_p2(intersection);
        return intersection;
    }

    private Hashtable<Integer, TakePhase1Response> take_p1(String pattern) {
        TakePhase1Request request = TakePhase1Request.newBuilder().setSearchPattern(pattern).setClientId(clientId)
                .build();
        ResponseCollector<TakePhase1Response> collector = new ResponseCollector<>();
        for (Integer id : delayer) {
            debugMessage("Requesting take phase 1 to replica " + id + " of pattern: " + pattern);
            stubs.get(id).takePhase1(request, new TupleSpacesObserver<>(collector, id));
        }
        collector.waitForAll();
        return collector.getTaggedResponses();
    }

    private ArrayList<TakePhase1ReleaseResponse> take_p1_release(ArrayList<Integer> empties) {
        TakePhase1ReleaseRequest request = TakePhase1ReleaseRequest.newBuilder().setClientId(clientId).build();
        ResponseCollector<TakePhase1ReleaseResponse> collector = new ResponseCollector<>();

        int count = 0;
        for (Integer id : delayer) {
            if (!empties.contains(id)) {
                count++;
                debugMessage(
                        "Requesting take phase 1 release to replica " + id + " with clientId: " + clientId);
                stubs.get(id).takePhase1Release(request, new TupleSpacesObserver<>(collector));
            }
        }

        collector.waitForCount(count);
        return collector.getResponses();
    }

    private ArrayList<TakePhase2Response> take_p2(String tuple) {
        TakePhase2Request request = TakePhase2Request.newBuilder().setTuple(tuple).setClientId(clientId).build();
        ResponseCollector<TakePhase2Response> collector = new ResponseCollector<>();
        for (Integer id : delayer) {
            debugMessage("Requesting take phase 2 operation to replica " + id + " of tuple: " + tuple);
            stubs.get(id).takePhase2(request, new TupleSpacesObserver<>(collector));
        }
        collector.waitForAll();
        return collector.getResponses();
    }

    public getTupleSpacesStateResponse getTupleSpacesState(int indexServerQualifier) {
        debugMessage("Requesting getTupleSpacesState to TupleSpaces Server replica w/ index: " + indexServerQualifier);
        getTupleSpacesStateRequest request = getTupleSpacesStateRequest.getDefaultInstance();
        ResponseCollector<getTupleSpacesStateResponse> collector = new ResponseCollector<>();
        TupleSpacesObserver<getTupleSpacesStateResponse> observer = new TupleSpacesObserver<>(collector);
        TupleSpacesReplicaStub stub = stubs.get(indexServerQualifier);

        stub.getTupleSpacesState(request, observer);

        collector.waitForFirst();
        return collector.getResponses().get(0);
    }

    public void exit() {
        channels.forEach(ManagedChannel::shutdownNow);
    }
}
