package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.server.util.Tuple;
import pt.ulisboa.tecnico.tuplespaces.server.util.exception.TupleLockedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static pt.ulisboa.tecnico.tuplespaces.server.ServerMain.debugMessage;

public class ServerState {

    private final List<Tuple> tuples;

    public ServerState() {
        this.tuples = new ArrayList<>();
    }

    public synchronized void put(String tuple) {
        tuples.add(new Tuple(tuple));
        debugMessage("Add tuple: " + tuple);
        notifyAll();
    }

    public synchronized String read(String pattern) {
        String tuple;
        while ((tuple = getMatchingTuple(pattern)) == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return tuple;
    }

    /*
     * We only add a tuple to the response if it isn't repeated and locked by another client.
     * This way, if we have two equal tuples and two equal requests, we can lock one tuple for each one,
     * instead of locking two for only one client. Rejecting a request if we can't lock a matching tuple would
     * make this optimization useless, so we opted to always answer the clients with all the tuples we can lock
     */
    public synchronized List<String> takePhase1(String pattern, int clientId) {
        debugMessage("Take Phase 1. Pattern: " + pattern + ", clientId: " + clientId);
        try {
            ArrayList<String> stuples;
            boolean found;
            for (; ; ) {
                found = false;
                stuples = new ArrayList<>();
                for (Tuple t : tuples) {
                    if (t.matches(pattern)) {
                        found = true;
                        if ((!t.isLocked() || t.getClientId() == clientId) && !stuples.contains(t.getTuple())) {
                            t.lock(clientId);
                            stuples.add(t.getTuple());
                        }
                    }
                }
                if (found) break;
                wait();
            }
            return stuples;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void takePhase1Release(int clientId) {
        debugMessage("Take Phase 1 Release. clientId: " + clientId);
        releaseAll(clientId);
    }

    public synchronized void takePhase2(String tuple, int clientId) {
        debugMessage("Take Phase 2. Tuple: " + tuple + ", clientId: " + clientId);
        removeTuple(tuple, clientId);
        releaseAll(clientId);
    }

    public synchronized List<String> getTupleSpacesState() {
        return getAllTuples();
    }

    private String getMatchingTuple(String pattern) {
        for (Tuple tuple : this.tuples) {
            if (tuple.matches(pattern)) {
                debugMessage("Found tuple: " + tuple);
                return tuple.getTuple();
            }
        }
        debugMessage("Pattern %s not found", pattern);
        return null;
    }

    public void removeTuple(String tuple, int clientId) {
        for (Tuple t : tuples) {
            if (t.getTuple().equals(tuple) && t.getClientId() == clientId) {
                tuples.remove(t);
                break;
            }
        }
    }

    public void releaseAll(int clientId) {
        for (Tuple t : tuples) {
            if (t.getClientId() == clientId)
                t.unlock();
        }
    }

    public List<String> getAllTuples() {
        List<String> stuples = new ArrayList<>();
        for (Tuple t : tuples) {
            stuples.add(t.getTuple());
        }
        return stuples;
    }
}
