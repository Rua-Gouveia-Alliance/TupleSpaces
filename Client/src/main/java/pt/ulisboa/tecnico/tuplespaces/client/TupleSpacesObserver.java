package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;

import static pt.ulisboa.tecnico.tuplespaces.client.ClientMain.debugMessage;

public class TupleSpacesObserver<R> implements StreamObserver<R> {

    private final ResponseCollector<R> collector;

    public TupleSpacesObserver(ResponseCollector<R> collector) {
        this.collector = collector;
    }

    @Override
    public void onNext(R r) {
        debugMessage("Collected response");
        collector.collect(r);
    }

    @Override
    public void onError(Throwable throwable) {
        debugMessage("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        debugMessage("Completed request");
    }
}
