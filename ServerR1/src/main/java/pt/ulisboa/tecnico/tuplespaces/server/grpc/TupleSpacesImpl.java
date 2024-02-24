package pt.ulisboa.tecnico.tuplespaces.server.grpc;

import io.grpc.Server;
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
        // TODO
    }

    @Override
    public void read(TupleSpacesCentralized.ReadRequest request,
                     StreamObserver<TupleSpacesCentralized.ReadResponse> responseObserver) {
        // TODO
    }

    @Override
    public void take(TupleSpacesCentralized.TakeRequest request,
                     StreamObserver<TupleSpacesCentralized.TakeResponse> responseObserver) {
        // TODO
    }

    @Override
    public void getTupleSpacesState(TupleSpacesCentralized.getTupleSpacesStateRequest request,
                                    StreamObserver<TupleSpacesCentralized.getTupleSpacesStateResponse> responseObserver) {
        // TODO
    }
}
