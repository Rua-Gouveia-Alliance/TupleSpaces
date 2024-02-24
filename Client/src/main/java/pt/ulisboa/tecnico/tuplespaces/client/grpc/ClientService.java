package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.PutResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.ReadResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.TakeResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateRequest;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.getTupleSpacesStateResponse;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesBlockingStub;

public class ClientService {

  /*
   * TODO: The gRPC client-side logic should be here.
   * This should include a method that builds a channel and stub,
   * as well as individual methods for each remote operation of this service.
   */

  private final ManagedChannel channel;
  private final TupleSpacesBlockingStub stub;

  public ClientService(String target) {
    this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.stub = TupleSpacesGrpc.newBlockingStub(channel);
  }

  public PutResponse put(String tuple) {
    PutRequest request = PutRequest.newBuilder().setNewTuple(tuple).build();
    return stub.put(request);
  }

  public TakeResponse take(String pattern) {
    TakeRequest request = TakeRequest.newBuilder().setSearchPattern(pattern).build();
    return stub.take(request);
  }

  public ReadResponse read(String pattern) {
    ReadRequest request = ReadRequest.newBuilder().setSearchPattern(pattern).build();
    return stub.read(request);
  }

  public getTupleSpacesStateResponse getTupleSpacesState() {
    getTupleSpacesStateRequest request = getTupleSpacesStateRequest.getDefaultInstance();
    return stub.getTupleSpacesState(request);
  }

  public void exit() {
    channel.shutdownNow();
  }
}
