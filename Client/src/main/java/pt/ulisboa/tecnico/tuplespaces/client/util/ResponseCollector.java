package pt.ulisboa.tecnico.tuplespaces.client.util;

import java.util.ArrayList;
import java.util.Hashtable;

public class ResponseCollector<R> {
    private int response_count = 0;
    private final int RESPONSE_COUNT = 3;
    private final ArrayList<R> responses = new ArrayList<>();
    private final Hashtable<Integer, R> tagged_responses = new Hashtable<>();

    public synchronized void collect(R response) {
        responses.add(response);
        response_count++;
        notifyAll();
    }

    public synchronized void collectTagged(Integer tag, R response) {
        tagged_responses.put(tag, response);
        response_count++;
        notifyAll();
    }

    public synchronized void waitForCount(int count) {
        while (response_count < count) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void waitForAll() {
        while (response_count < RESPONSE_COUNT) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void waitForFirst() {
        while (response_count < 1) {
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

    public Hashtable<Integer, R> getTaggedResponses() {
        return tagged_responses;
    }
}
