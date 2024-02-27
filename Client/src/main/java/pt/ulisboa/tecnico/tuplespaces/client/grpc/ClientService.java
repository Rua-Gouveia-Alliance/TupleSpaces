package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized.*;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc.TupleSpacesBlockingStub;

import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass;
import pt.ulisboa.tecnico.nameserver.contract.NameServerGrpc.NameServerBlockingStub;
import pt.ulisboa.tecnico.nameserver.contract.NameServerOuterClass.*;

public class ClientService {

  private final ManagedChannel channel;
  private final TupleSpacesBlockingStub stub;
  //private final ManagedChannel dnsChannel;
  private final NameServerBlockingStub dnsStub;

  public ClientService(String target) {
    this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
    this.stub = TupleSpacesGrpc.newBlockingStub(channel);
    this.dnsStub = NameServerGrpc.newBlockingStub(channel);
  }

  public LookupResponse lookup(String service, String qualifier) {
    LookupRequest request = LookupRequest.newBuilder().setService(service).setQualifier(qualifier).build();
    return dnsStub.lookup(request);
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
