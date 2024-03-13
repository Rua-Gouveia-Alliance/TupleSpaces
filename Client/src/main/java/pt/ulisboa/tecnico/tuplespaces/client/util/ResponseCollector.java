package pt.ulisboa.tecnico.tuplespaces.client.util;

import java.util.ArrayList;

public class ResponseCollector<R> {
    private int errors = 0;
    private final int RESPONSE_COUNT = 3;
    private final ArrayList<R> responses = new ArrayList<>();

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

    public synchronized void waitForFirst() {
        while (responses.size() < 1) {
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
