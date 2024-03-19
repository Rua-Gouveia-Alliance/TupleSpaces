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

    public synchronized ArrayList<String> takePhase1(String pattern, int clientId) {
        debugMessage("Take Phase 1. Pattern: " + pattern + ", clientId: " + clientId);
        ArrayList<String> stuples = new ArrayList<>();
        boolean found = false;
        while (!found) {
            for (Tuple t : tuples) {
                if (t.matches(pattern)) {
                    found = true;
                    // We don't care about repeated tuples
                    if (!stuples.contains(t.getTuple())) {
                        if (t.canBeLocked(clientId)) {
                            stuples.add(t.getTuple());
                            t.lock(clientId);
                        } else {
                            // Tuple exists but can't be locked, we must reject this request
                            return new ArrayList<>();
                        }
                    }
                }
            }
            if (!found) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return stuples;
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
