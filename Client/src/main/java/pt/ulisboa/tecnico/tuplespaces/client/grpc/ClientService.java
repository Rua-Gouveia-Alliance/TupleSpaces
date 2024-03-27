package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.client.TupleSpacesObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder.*;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc.*;
import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc;
import pt.ulisboa.tecnico.sequencer.contract.SequencerGrpc.SequencerStub;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass.GetSeqNumberRequest;
import pt.ulisboa.tecnico.sequencer.contract.SequencerOuterClass.GetSeqNumberResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static pt.ulisboa.tecnico.tuplespaces.client.ClientMain.debugMessage;

public class ClientService {

    private final int NUM_REPLICAS = 3;
    private final ArrayList<ManagedChannel> channels;
    private final ArrayList<TupleSpacesReplicaStub> stubs;
    private final SequencerStub seqStub;
    private final OrderedDelayer delayer;

    public ClientService(ArrayList<String> targets, String seqTarget) {
        channels = new ArrayList<>();
        stubs = new ArrayList<>();

        debugMessage("Establishing rpc connection");
        targets.forEach(target -> {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.channels.add(channel);
            this.stubs.add(TupleSpacesReplicaGrpc.newStub(channel));
        });

        ManagedChannel seqChannel = ManagedChannelBuilder.forTarget(seqTarget).usePlaintext().build();
        this.seqStub = SequencerGrpc.newStub(seqChannel);

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
        // get a sequence number from sequencer, send in request
        debugMessage("Requesting put operation to TupleSpaces Server of tuple: " + tuple);
        PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).setSeqNumber(getSeqNumber()).build();
        ResponseCollector<PutResponse> collector = new ResponseCollector<>();

        for (Integer id : delayer)
            stubs.get(id).put(request, new TupleSpacesObserver<>(collector));

        return collector.waitForResponse();
    }

    public String read(String pattern) {
        debugMessage("Requesting read operation to TupleSpaces Server of pattern: " + pattern);
        ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
        ResponseCollector<ReadResponse> collector = new ResponseCollector<>();

        for (Integer id : delayer)
            stubs.get(id).read(request, new TupleSpacesObserver<>(collector));

        return collector.waitForResponse().getResult();
    }

    public String take(String pattern) {
        // get a sequence number from sequencer, send in request
        debugMessage("Requesting take operation to TupleSpaces Server of pattern: " + pattern);
        TakeRequest request = TakeRequest.newBuilder().setSearchPattern(pattern).setSeqNumber(getSeqNumber()).build();
        ResponseCollector<TakeResponse> collector = new ResponseCollector<>();

        for (Integer id : delayer)
            stubs.get(id).take(request, new TupleSpacesObserver<>(collector));

        return collector.waitForResponse().getResult();
    }

    public int getSeqNumber() {
        debugMessage("Requesting Sequencer for total order number.");
        ResponseCollector<GetSeqNumberResponse> collector = new ResponseCollector<>();
        GetSeqNumberRequest request = GetSeqNumberRequest.getDefaultInstance();
        seqStub.getSeqNumber(request, new TupleSpacesObserver<>(collector));

        return collector.waitForResponse().getSeqNumber();
    }

    public String getIntersection(List<String> list) {
        for (String tuple : list)
            if (Collections.frequency(list, tuple) == NUM_REPLICAS)
                return tuple;
        return null;
    }

    public getTupleSpacesStateResponse getTupleSpacesState(int indexServerQualifier) {
        debugMessage("Requesting getTupleSpacesState to TupleSpaces Server replica w/ index: " + indexServerQualifier);
        getTupleSpacesStateRequest request = getTupleSpacesStateRequest.getDefaultInstance();
        ResponseCollector<getTupleSpacesStateResponse> collector = new ResponseCollector<>();
        TupleSpacesObserver<getTupleSpacesStateResponse> observer = new TupleSpacesObserver<>(collector);
        TupleSpacesReplicaStub stub = stubs.get(indexServerQualifier);

        stub.getTupleSpacesState(request, observer);

        return collector.waitForResponse();
    }

    public void exit() {
        channels.forEach(ManagedChannel::shutdownNow);
    }
}
