package pt.ulisboa.tecnico.tuplespaces.client.util;

import pt.ulisboa.tecnico.tuplespaces.client.util.exceptions.AllServersFailedException;

import java.util.ArrayList;

import static io.grpc.Status.NOT_FOUND;

public class ResponseCollector<R> {
    private int errors = 0;
    private final int RESPONSE_COUNT = 3;
    private ArrayList<R> responses = new ArrayList<>();

    public synchronized void collect(R response) {
        responses.add(response);
        notifyAll();
    }

    public synchronized void waitForAll() {
        while (responses.size() < RESPONSE_COUNT) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void incrementError() {
        errors++;
        notifyAll();
    }

    public synchronized void waitForFirst() throws AllServersFailedException {
        while (responses.size() < 1) {
            if (errors == responses.size())
                throw new AllServersFailedException("No server has requested tuple");
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ArrayList<R> getResponses() {
        return responses;
    }
}
