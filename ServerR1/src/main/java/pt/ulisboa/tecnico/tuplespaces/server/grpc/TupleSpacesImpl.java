package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesCentralized;
import pt.ulisboa.tecnico.tuplespaces.centralized.contract.TupleSpacesGrpc;
import pt.ulisboa.tecnico.tuplespaces.server.domain.ServerState;

public class TupleSpacesImpl extends TupleSpacesGrpc.TupleSpacesImplBase {

    /* Server state implementation
     * TODO: Is this the right place to instantiate the ServerState? */
    private ServerState state = new ServerState();

    @Override
    public void put(TupleSpacesCentralized.PutRequest request,
                    StreamObserver<TupleSpacesCentralized.PutResponse> responseObserver) {
        String newTuple = request.getNewTuple();
        state.put(newTuple);

        // create and send response
        TupleSpacesCentralized.PutResponse response = TupleSpacesCentralized.PutResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void read(TupleSpacesCentralized.ReadRequest request,
                     StreamObserver<TupleSpacesCentralized.ReadResponse> responseObserver) {
        String pattern = request.getSearchPattern();
        String tuple = state.read(pattern);

        if (tuple == null) {
            // TODO: send exception or wait??
            return;
        }

        // create and send response
        TupleSpacesCentralized.ReadResponse response = TupleSpacesCentralized.ReadResponse.newBuilder().setResult(
                tuple).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void take(TupleSpacesCentralized.TakeRequest request,
                     StreamObserver<TupleSpacesCentralized.TakeResponse> responseObserver) {
        String pattern = request.getSearchPattern();
        String tuple = state.read(pattern);

        if (tuple == null) {
            // TODO: send exception or wait??
            return;
        }

        // create and send response
        TupleSpacesCentralized.TakeResponse response = TupleSpacesCentralized.TakeResponse.newBuilder().setResult(
                tuple).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getTupleSpacesState(TupleSpacesCentralized.getTupleSpacesStateRequest request,
                                    StreamObserver<TupleSpacesCentralized.getTupleSpacesStateResponse> responseObserver) {
        // TODO
    }
}
