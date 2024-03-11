package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesStub;
import pt.ulisboa.tecnico.tuplespaces.client.TupleSpacesObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.OrderedDelayer;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;

import static pt.ulisboa.tecnico.tuplespaces.client.ClientMain.debugMessage;

import java.util.ArrayList;

public class ClientService {

    private ArrayList<String> targets;
    private ArrayList<ManagedChannel> channels;
    private ArrayList<TupleSpacesStub> stubs;
    OrderedDelayer delayer;

    public ClientService(ArrayList<String> targets) {
        this.targets = targets;
        debugMessage("Establishing rpc connection");
        targets.forEach(target -> {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.channels.add(channel);
            this.stubs.add(TupleSpacesGrpc.newStub(channel));
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
        debugMessage("Requesting put operation to TupleSpaces Server");
        PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
        ResponseCollector<PutResponse> collector = new ResponseCollector<>();
        for (Integer id : delayer)
            stubs.get(id).put(request, new TupleSpacesObserver<>(collector));
        collector.waitForAll();
        return collector.getResponses().get(0);
    }

    public ReadResponse read(String pattern) {
        debugMessage("Requesting read operation to TupleSpaces Server");
        ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
        ResponseCollector<ReadResponse> collector = new ResponseCollector<>();
        for (Integer id : delayer)
            stubs.get(id).read(request, new TupleSpacesObserver<>(collector));
        collector.waitForFirst();
        return collector.getResponses().get(0);
    }

    public TakeResponse take(String pattern) {
        debugMessage("Requesting take operation to TupleSpaces Server");
        throw new RuntimeException("unimplemented");
        //TakeRequest request = TakeRequest.newBuilder().setSearchPattern(pattern).build();
        //return stub.take(request);
    }

    public getTupleSpacesStateResponse getTupleSpacesState(String qualifier) {
        debugMessage("Requesting getTupleSpacesState operation to TupleSpaces Server");
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
