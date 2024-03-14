package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaXuLiskov.contract.TupleSpacesReplicaXuLiskov;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

import java.util.List;

import static io.grpc.Status.INVALID_ARGUMENT;
import static pt.ulisboa.tecnico.tuplespaces.server.ServerMain.debugMessage;

public class TupleSpacesImpl extends TupleSpacesReplicaGrpc.TupleSpacesReplicaImplBase {

    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";

    // Server state implementation
    private final ServerState state;

    public TupleSpacesImpl() {
        this.state = new ServerState();
    }

    private boolean tupleIsValid(String tuple) {
        // TODO is this enough?? and should we add try catches on the client-side??
        return tuple.startsWith(BGN_TUPLE) && tuple.endsWith(END_TUPLE);
    }

    @Override
    public void put(TupleSpacesReplicaXuLiskov.PutRequest request,
                    StreamObserver<TupleSpacesReplicaXuLiskov.PutResponse> responseObserver) {
        String newTuple = request.getNewTuple();
        debugMessage("Received put request for tuple " + newTuple);

        if (!tupleIsValid(newTuple)) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid tuple format").asRuntimeException());
            return;
        }

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
        if (tuple == null) {
            tuple = "";
        } else {
            if (!tupleIsValid(tuple)) {
                responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid tuple format").asRuntimeException());
                return;
            }
        }

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
        int clientId = request.getClientId();
        debugMessage("Received takePhase1 request of pattern " + pattern + "from client " + clientId);
        if (!tupleIsValid(pattern)) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid tuple format").asRuntimeException());
            return;
        }

        List<String> tuples = state.takePhase1(pattern, clientId);

        // create and send response
        TupleSpacesReplicaXuLiskov.TakePhase1Response response = TupleSpacesReplicaXuLiskov.TakePhase1Response
                .newBuilder().addAllReservedTuples(tuples).build();
        debugMessage("Sending take phase 1 response of tuples " + tuples.toString());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void takePhase1Release(TupleSpacesReplicaXuLiskov.TakePhase1ReleaseRequest request,
                                  StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse> responseObserver) {

        int clientId = request.getClientId();
        debugMessage("Received takePhase1Release request of client " + clientId);
        state.takePhase1Release(clientId);

        // create and send response
        TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse response = TupleSpacesReplicaXuLiskov.TakePhase1ReleaseResponse.getDefaultInstance();
        debugMessage("Sending take phase 1 release response");

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void takePhase2(TupleSpacesReplicaXuLiskov.TakePhase2Request request,
                           StreamObserver<TupleSpacesReplicaXuLiskov.TakePhase2Response> responseObserver) {

        String tuple = request.getTuple();
        int clientId = request.getClientId();
        debugMessage("Received takePhase2 request of tuple " + tuple);
        if (!tupleIsValid(tuple)) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid tuple format").asRuntimeException());
            return;
        }
        state.takePhase2(tuple, clientId);

        // create and send response
        TupleSpacesReplicaXuLiskov.TakePhase2Response response = TupleSpacesReplicaXuLiskov.TakePhase2Response.getDefaultInstance();
        debugMessage("Sending take phase 2 response");

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
