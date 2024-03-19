package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaGrpc;
import pt.ulisboa.tecnico.tuplespaces.replicaTotalOrder.contract.TupleSpacesReplicaTotalOrder;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;
import pt.ulisboa.tecnico.tuplespaces.server.util.Tuple;

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
        return tuple.startsWith(BGN_TUPLE) && tuple.endsWith(END_TUPLE);
    }

    @Override
    public void put(TupleSpacesReplicaTotalOrder.PutRequest request,
            StreamObserver<TupleSpacesReplicaTotalOrder.PutResponse> responseObserver) {
        String newTuple = request.getNewTuple();
        int seqNumber = request.getSeqNumber();
        debugMessage("Received put request for tuple " + newTuple + " with seqNumber " + seqNumber);

        if (!tupleIsValid(newTuple)) {
            debugMessage("Pattern " + newTuple + " is not a valid format for a tuple");
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid tuple format").asRuntimeException());
            return;
        }

        state.put(newTuple, seqNumber);

        // create and send response
        TupleSpacesReplicaTotalOrder.PutResponse response = TupleSpacesReplicaTotalOrder.PutResponse
                .getDefaultInstance();
        debugMessage("Sending put response of tuple " + newTuple);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void read(TupleSpacesReplicaTotalOrder.ReadRequest request,
            StreamObserver<TupleSpacesReplicaTotalOrder.ReadResponse> responseObserver) {
        String pattern = request.getSearchPattern();
        debugMessage("Received read request of pattern " + pattern);
        if (!tupleIsValid(pattern)) {
            debugMessage("Pattern " + pattern + " is not a valid format for a tuple");
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid tuple format").asRuntimeException());
            return;
        }

        String tuple = state.read(pattern);
        if (tuple == null)
            tuple = "";

        TupleSpacesReplicaTotalOrder.ReadResponse response = TupleSpacesReplicaTotalOrder.ReadResponse.newBuilder()
                .setResult(tuple).build();
        debugMessage("Sending read response of tuple " + tuple);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void take(TupleSpacesReplicaTotalOrder.TakeRequest request,
            StreamObserver<TupleSpacesReplicaTotalOrder.TakeResponse> responseObserver) {

        String pattern = request.getSearchPattern();
        int seqNumber = request.getSeqNumber();
        debugMessage("Received takePhase1 request of pattern " + pattern + " with seqNumber " + seqNumber);
        if (!tupleIsValid(pattern)) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid tuple format").asRuntimeException());
            return;
        }

        String tuple = state.take(pattern, seqNumber);

        // create and send response
        TupleSpacesReplicaTotalOrder.TakeResponse response = TupleSpacesReplicaTotalOrder.TakeResponse
                .newBuilder().setResult(tuple).build();
        debugMessage("Sending take phase 1 response with tuple " + tuple);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTupleSpacesState(TupleSpacesReplicaTotalOrder.getTupleSpacesStateRequest request,
            StreamObserver<TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse> responseObserver) {
        debugMessage("Received getTupleSpacesState request");
        List<String> tuples = state.getTupleSpacesState();

        // create and send response
        TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse response = TupleSpacesReplicaTotalOrder.getTupleSpacesStateResponse
                .newBuilder().addAllTuple(tuples).build();
        debugMessage("Sending getTupleSpacesState response: " + tuples.toString());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
