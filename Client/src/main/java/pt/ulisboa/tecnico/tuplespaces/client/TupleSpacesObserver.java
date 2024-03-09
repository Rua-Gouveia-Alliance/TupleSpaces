package pt.ulisboa.tecnico.tuplespaces.client;

import static pt.ulisboa.tecnico.tuplespaces.client.ClientMain.debugMessage;

import io.grpc.stub.StreamObserver;

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
        System.err.println("Received error: " + throwable);
    }

    @Override
    public void onCompleted() {
        debugMessage("Completed request");
    }
}
