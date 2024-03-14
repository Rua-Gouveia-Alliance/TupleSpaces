package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.tuplespaces.client.util.ResponseCollector;

import static pt.ulisboa.tecnico.tuplespaces.client.ClientMain.debugMessage;

public class TupleSpacesObserver<R> implements StreamObserver<R> {

    private final ResponseCollector<R> collector;
    private final Integer id;

    public TupleSpacesObserver(ResponseCollector<R> collector) {
        this.collector = collector;
        this.id = -1;
    }

    public TupleSpacesObserver(ResponseCollector<R> collector, Integer id) {
        this.collector = collector;
        this.id = id;
    }

    @Override
    public void onNext(R r) {
        debugMessage("Collected response");
        if (id == -1) {
            collector.collect(r);
        } else {
            collector.collectTagged(id, r);
        }
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
