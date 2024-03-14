package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.server.util.Tuple;
import pt.ulisboa.tecnico.tuplespaces.server.util.exception.TupleLockedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static pt.ulisboa.tecnico.tuplespaces.server.ServerMain.debugMessage;

public class ServerState {

    private final List<Tuple> tuples;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Condition cond = lock.newCondition();

    public ServerState() {
        this.tuples = new ArrayList<>();
    }

    public void put(String tuple) {
        lock.writeLock().lock();
        try {
            tuples.add(new Tuple(tuple));
            debugMessage("Add tuple: " + tuple);
            cond.signal();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String read(String pattern) {
        lock.readLock().lock();
        try {
            String tuple;
            while ((tuple = getMatchingTuple(pattern)) == null) {
                lock.readLock().unlock();
                cond.await();
                lock.readLock().lock();
            }
            return tuple;
        } finally {
            lock.readLock().unlock();
        }
    }


    /*
     * We only add a tuple to the response if it isn't repeated and locked by another client.
     * This way, if we have two equal tuples and two equal requests, we can lock one tuple for each one,
     * instead of locking two for only one client. Rejecting a request if we can't lock a matching tuple would
     * make this optimization useless, so we opted to always answer the clients with all the tuples we can lock
     */
    public List<String> takePhase1(String pattern, int clientId) {
        lock.writeLock().lock();
        try {
            debugMessage("Take Phase 1. Pattern: " + pattern + ", clientId: " + clientId);
            ArrayList<String> stuples = new ArrayList<>();
            for (Tuple t : tuples) {
                if (t.matches(pattern) && !stuples.contains(t.getTuple()) && !t.isLocked()) {
                    t.lock(clientId);
                    stuples.add(t.getTuple());
                }
            }
            return stuples;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void takePhase1Release(int clientId) {
        lock.writeLock().lock();
        try {
            debugMessage("Take Phase 1 Release. clientId: " + clientId);
            releaseAll(clientId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void takePhase2(String tuple, int clientId) {
        lock.writeLock().lock();
        try {
            debugMessage("Take Phase 2. Tuple: " + tuple + ", clientId: " + clientId);
            removeTuple(tuple, clientId);
            releaseAll(clientId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<String> getTupleSpacesState() {
        lock.readLock().lock();
        try{
            return getAllTuples();
        } finally {
            lock.readLock().unlock();
        }
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
