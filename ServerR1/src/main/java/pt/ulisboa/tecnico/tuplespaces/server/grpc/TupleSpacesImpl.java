package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

import java.util.List;

import static pt.ulisboa.tecnico.tuplespaces.server.ServerMain.debugMessage;

public class TupleSpacesImpl extends TupleSpacesGrpc.TupleSpacesImplBase {

    // Server state implementation
    private final ServerState state;

    public TupleSpacesImpl() {
        this.state = new ServerState();
    }

    @Override
    public void put(TupleSpacesCentralized.PutRequest request,
                    StreamObserver<TupleSpacesCentralized.PutResponse> responseObserver) {
        String newTuple = request.getNewTuple();
        debugMessage("Received put request for tuple " + newTuple);
        state.put(newTuple);

        // create and send response
        TupleSpacesCentralized.PutResponse response = TupleSpacesCentralized.PutResponse.getDefaultInstance();
        debugMessage("Sending put response of tuple " + newTuple);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void read(TupleSpacesCentralized.ReadRequest request,
                     StreamObserver<TupleSpacesCentralized.ReadResponse> responseObserver) {
        String pattern = request.getSearchPattern();
        debugMessage("Received read request of pattern " + pattern);
        String tuple = state.read(pattern);

        // create and send response
        TupleSpacesCentralized.ReadResponse response = TupleSpacesCentralized.ReadResponse.newBuilder().setResult(
                tuple).build();
        debugMessage("Sending read response of tuple " + tuple);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void take(TupleSpacesCentralized.TakeRequest request,
                     StreamObserver<TupleSpacesCentralized.TakeResponse> responseObserver) {
        String pattern = request.getSearchPattern();
        debugMessage("Received take request of pattern " + pattern);
        String tuple = state.take(pattern);

        // create and send response
        TupleSpacesCentralized.TakeResponse response = TupleSpacesCentralized.TakeResponse.newBuilder().setResult(
                tuple).build();
        debugMessage("Sending take response of tuple " + tuple);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTupleSpacesState(TupleSpacesCentralized.getTupleSpacesStateRequest request,
                                    StreamObserver<TupleSpacesCentralized.getTupleSpacesStateResponse> responseObserver) {
        List<String> tuples = state.getTupleSpacesState();
        debugMessage("Received getTupleSpacesState request");

        // create and send response
        TupleSpacesCentralized.getTupleSpacesStateResponse response = TupleSpacesCentralized.getTupleSpacesStateResponse
                .newBuilder().addAllTuple(tuples).build();
        debugMessage("Sending getTupleSpacesState response: " + tuples.toString());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
