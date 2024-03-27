package pt.ulisboa.tecnico.tuplespaces.client.util;

import java.util.ArrayList;

public class ResponseCollector<R> {
    private int response_count = 0;
    private final ArrayList<R> responses = new ArrayList<>();

    public synchronized void collect(R response) {
        responses.add(response);
        response_count++;
        notifyAll();
    }

    public synchronized R waitForResponse() {
        while (response_count < 1) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return responses.get(0);
    }
}
