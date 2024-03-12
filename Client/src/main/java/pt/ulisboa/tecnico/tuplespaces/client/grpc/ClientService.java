package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc.TupleSpacesReplicaStub;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov.*;
import pt.ulisboa.tecnico.tuplespaces.client.TupleSpacesObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;

import static io.grpc.Status.NOT_FOUND;

import static pt.ulisboa.tecnico.tuplespaces.client.ClientMain.debugMessage;

import java.util.ArrayList;
import java.util.List;

public class ClientService {

    private ArrayList<String> targets;
    private ArrayList<ManagedChannel> channels;
    private ArrayList<TupleSpacesReplicaStub> stubs;
    OrderedDelayer delayer;
    private int clientId;

    public ClientService(ArrayList<String> targets, int clientId) {
        this.targets = targets;
        channels = new ArrayList<>();
        stubs = new ArrayList<>();
        this.clientId = clientId;

        debugMessage("Establishing rpc connection");
        targets.forEach(target -> {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.channels.add(channel);
            this.stubs.add(TupleSpacesReplicaGrpc.newStub(channel));
        });

        /* The delayer can be used to inject delays to the sending of requests to the 
        different servers, according to the per-server delays that have been set  */
        delayer = new OrderedDelayer(targets.size());
    }

    /* This method allows the command processor to set the request delay assigned to a given server */
    public void setDelay(int id, int delay) {
        // TODO: should we disallow the usage of setDelay when not in debug mode??
        delayer.setDelay(id, delay);

        debugMessage("[Debug only]: After setting the delay, I'll test it");
        for (Integer i : delayer) {
            debugMessage("[Debug only]: Now I can send request to stub[" + i + "]");
        }
        debugMessage("[Debug only]: Done.");
    }

    /* Example: How to use the delayer before sending requests to each server
     *          Before entering each iteration of this loop, the delayer has already 
     *          slept for the delay associated with server indexed by 'id'.
     *          id is in the range 0..(numServers-1).
    
        for (Integer id : delayer) {
            //stub[id].some_remote_method(some_arguments);
        }

    */

    public PutResponse put(String tuple) {
        debugMessage("Requesting put operation to TupleSpaces Server of tuple: " + tuple);
        PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
        ResponseCollector<PutResponse> collector = new ResponseCollector<>();
        for (Integer id : delayer)
            stubs.get(id).put(request, new TupleSpacesObserver<>(collector));
        // TODO: what if a replica does not respond? should we repeat after a certain time?
        collector.waitForAll();
        return collector.getResponses().get(0);
    }

    public ReadResponse read(String pattern) {  //TODO: should read wait for first?
        debugMessage("Requesting read operation to TupleSpaces Server of pattern: " + pattern);
        ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
        ResponseCollector<ReadResponse> collector = new ResponseCollector<>();
        for (Integer id : delayer)
            stubs.get(id).read(request, new TupleSpacesObserver<>(collector));
        try {
            collector.waitForFirst();
        } catch (RuntimeException e) {
            //TODO: what to do with caught NOT_FOUND
        }
        return collector.getResponses().get(0);
    }

    public List<String> get_intersection(List<List<String>> lists) {
        List<String> intersection = lists.get(0);

        for (int i = 1; i < lists.size(); i++) {
            List<String> temp_intersection = new ArrayList<>();
            for (String element : lists.get(i)) {
                if (intersection.contains(element))
                    temp_intersection.add(element);
            }
            intersection = temp_intersection;
        }
        return intersection;
    }

    public String take(String pattern) {
        List<TakePhase1Response> responses;
        List<List<String>> responses_strings;
        List<String> intersection = new ArrayList<>();
        long num_empties;
        do {
            // take phase 1 and convert responses to List<String>
            responses = take_p1(pattern);
            responses_strings = new ArrayList<>();
            for (TakePhase1Response response : responses)
                responses_strings.add(response.getReservedTuplesList());

            num_empties = responses_strings.stream().filter(List::isEmpty).count(); // number of empty responses
            // minority responded
            if (num_empties > targets.size() / 2)
                take_p1_release();
            if (num_empties > 0)
                continue;

            intersection = get_intersection(responses_strings);
        } while (intersection.isEmpty());

        take_p2(intersection.get(0));
        return intersection.get(0);
    }

    public List<TakePhase1Response> take_p1(String pattern) {
        debugMessage("Requesting take phase 1 operation to TupleSpaces Servers of patter: " + pattern);
        TakePhase1Request request = TakePhase1Request.newBuilder().setSearchPattern(pattern).setClientId(
                clientId).build();
        ResponseCollector<TakePhase1Response> collector = new ResponseCollector<>();
        for (Integer id : delayer)
            stubs.get(id).takePhase1(request, new TupleSpacesObserver<>(collector));
        // TODO: what if a replica does not respond? should we repeat after a certain time?
        collector.waitForAll();
        return collector.getResponses();
    }

    public List<TakePhase1ReleaseResponse> take_p1_release() {
        debugMessage("Requesting take phase 1 release operation to TupleSpaces Servers");
        TakePhase1ReleaseRequest request = TakePhase1ReleaseRequest.newBuilder().setClientId(clientId).build();
        ResponseCollector<TakePhase1ReleaseResponse> collector = new ResponseCollector<>();
        for (Integer id : delayer)
            stubs.get(id).takePhase1Release(request, new TupleSpacesObserver<>(collector));
        // TODO: what if a replica does not respond? should we repeat after a certain time?
        collector.waitForAll();
        return collector.getResponses();
    }

    public List<TakePhase2Response> take_p2(String tuple) {
        debugMessage("Requesting take phase 2 operation to TupleSpaces Servers of tuple: " + tuple);
        TakePhase2Request request = TakePhase2Request.newBuilder().setTuple(tuple).setClientId(clientId).build();
        ResponseCollector<TakePhase2Response> collector = new ResponseCollector<>();
        for (Integer id : delayer)
            stubs.get(id).takePhase2(request, new TupleSpacesObserver<>(collector));
        // TODO: what if a replica does not respond? should we repeat after a certain time?
        collector.waitForAll();
        return collector.getResponses();
    }

    public getTupleSpacesStateResponse getTupleSpacesState(String qualifier) {
        debugMessage("Requesting getTupleSpacesState operation to TupleSpaces Server replica: " + qualifier);
        getTupleSpacesStateRequest request = getTupleSpacesStateRequest.getDefaultInstance();
        ResponseCollector<getTupleSpacesStateResponse> collector = new ResponseCollector<>();
        stubs.get(targets.indexOf(qualifier)).getTupleSpacesState(request, new TupleSpacesObserver<>(collector));
        collector.waitForFirst();
        return collector.getResponses().get(0);
    }

    public void exit() {
        channels.forEach(ManagedChannel::shutdownNow);
    }
}
