package pt.ulisboa.tecnico.tuplespaces.client.util;

import java.util.ArrayList;

public class ResponseCollector<R> {
    private final int id;
    private final int RESPONSE_COUNT = 3;
    private final ArrayList<R> responses = new ArrayList<>();

    public ResponseCollector() {
        this.id = -1;
    }

    public ResponseCollector(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public synchronized void collect(R response) {
        responses.add(response);
        notifyAll();
    }

    public synchronized void waitForCount(int count) {
        while (responses.size() < count) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
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
