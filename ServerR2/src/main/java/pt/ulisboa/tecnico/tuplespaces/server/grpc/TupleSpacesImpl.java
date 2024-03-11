package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

import java.util.List;

import static pt.ulisboa.tecnico.tuplespaces.server.ServerMain.debugMessage;

public class TupleSpacesImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    // Server state implementation
    private final ServerState state;

    public TupleSpacesImpl() {
        this.state = new ServerState();
    }

    @Override
    public void put(TupleSpacesReplicaXuLiskov.PutRequest request,
                    StreamObserver<TupleSpacesReplicaXuLiskov.PutResponse> responseObserver) {
        String newTuple = request.getNewTuple();
        debugMessage("Received put request for tuple " + newTuple);
        state.put(newTuple);

        // create and send response
        TupleSpacesReplicaXuLiskov.PutResponse response = TupleSpacesReplicaXuLiskov.PutResponse.getDefaultInstance();
        debugMessage("Sending put response of tuple " + newTuple);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void read(TupleSpacesReplicaXuLiskov.ReadRequest request,
                     StreamObserver<TupleSpacesReplicaXuLiskov.ReadResponse> responseObserver) {
        String pattern = request.getSearchPattern();
        debugMessage("Received read request of pattern " + pattern);
        String tuple = state.read(pattern);

        // create and send response
        TupleSpacesReplicaXuLiskov.ReadResponse response = TupleSpacesReplicaXuLiskov.ReadResponse.newBuilder().setResult(
                tuple).build();
        debugMessage("Sending read response of tuple " + tuple);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void takePhase1(TupleSpacesReplicaXuLiskov.TakePhase1Request request,
                     StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase1Response> responseObserver) {

        String pattern = request.getSearchPattern();
        Integer clientId = request.getClientId();
        debugMessage("Received takePhase1 request of pattern " + pattern + "from client " + clientId);
        List<String> tuples = state.takePhase1(pattern, clientId);

        // create and send response
        TupleSpacesReplicaXuLiskov.TakePhase1Response response = TupleSpacesReplicaXuLiskov.TakePhase1Response
            .newBuilder().addAllReservedTuples(tuples).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void takePhase1Release(TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest request,
                     StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse> responseObserver) {
        
        String clientId = request.getClientId();
        debugMessage("Received takePhase1Release request of client " + clientId);
        state.takePhase1Release(clientId);

        // create and send response
        TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse response = TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse.getDefaultInstance();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void takePhase2(TupleSpacesReplicaXuLiskov.TakePhase2Request request,
                     StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase2Response> responseObserver) {

        String tuple = request.getTuple();
        debugMessage("Received takePhase2 request of tuple " + tuple);
        state.takePhase2(tuple);

        // create and send response
        TupleSpacesReplicaXuLiskov.TakePhase2Response response = TupleSpacesReplicaXuLiskov.TakePhase2Response
            .newBuilder().addAllReservedTuples(tuples).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTupleSpacesState(TupleSpacesReplicaXuLiskov.getTupleSpacesStateRequest request,
                                    StreamObserver<TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse> responseObserver) {
        List<String> tuples = state.getTupleSpacesState();
        debugMessage("Received getTupleSpacesState request");

        // create and send response
        TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse response = TupleSpacesReplicaXuLiskov.getTupleSpacesStateResponse
                .newBuilder().addAllTuple(tuples).build();
        debugMessage("Sending getTupleSpacesState response: " + tuples.toString());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
