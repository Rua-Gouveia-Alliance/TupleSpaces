package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesStub;
import pt.ulisboa.tecnico.tuplespaces.client.TupleSpacesObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;

import static pt.ulisboa.tecnico.tuplespaces.client.ClientMain.debugMessage;

import java.util.ArrayList;

public class ClientService {

  private ArrayList<ManagedChannel> channels;
  private ArrayList<TupleSpacesStub> stubs;

  public ClientService(ArrayList<String> targets) {
    debugMessage("Establishing rpc connection");
    targets.stream().forEach(target -> {
      ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
      this.channels.add(channel);
      this.stubs.add(TupleSpacesGrpc.newStub(channel));
    });
  }

  public PutResponse put(String tuple) {
    debugMessage("Requesting put operation to TupleSpaces Server");
    PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
    ResponseCollector<PutResponse> collector = new ResponseCollector<>();
    stubs.stream().forEach(stub -> stub.put(request, new TupleSpacesObserver<>(collector)));
    collector.waitForAll();
    return collector.getResponses().get(0);
}

  public ReadResponse read(String pattern) {
    debugMessage("Requesting read operation to TupleSpaces Server");
    ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
    ResponseCollector<ReadResponse> collector = new ResponseCollector<>();
    stubs.stream().forEach(stub -> stub.read(request, new TupleSpacesObserver<>(collector)));
    collector.waitForFirst();
    return collector.getResponses().get(0);
  }

  public TakeResponse take(String pattern) {
    debugMessage("Requesting take operation to TupleSpaces Server");
    throw new RuntimeException("unimplemented");
    //TakeRequest request = TakeRequest.newBuilder().setSearchPattern(pattern).build();
    //return stub.take(request);
  }

// TODO: nao percebi como e que e suposto implementar este (diz no enunciado para nao ser replicado)
  public getTupleSpacesStateResponse getTupleSpacesState() {
    debugMessage("Requesting getTupleSpacesState operation to TupleSpaces Server");
    throw new RuntimeException("unimplemented");
    //getTupleSpacesStateRequest request = getTupleSpacesStateRequest.getDefaultInstance();
    //return stub.getTupleSpacesState(request);
  }

  public void exit() {
    channels.stream().forEach(channel -> channel.shutdownNow());
  }
}
