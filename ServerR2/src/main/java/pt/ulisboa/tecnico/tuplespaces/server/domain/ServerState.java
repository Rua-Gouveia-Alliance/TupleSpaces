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
    private final ReadWriteLock lock;

    public ServerState() {
        this.tuples = new ArrayList<>();
        this.lock = new ReentrantReadWriteLock();
    }

    public synchronized void put(String tuple) {
        lock.writeLock().lock();
        try {
            tuples.add(new Tuple(tuple));
            debugMessage("Add tuple: " + tuple);
            notifyAll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String getMatchingTuple(String pattern) {
        lock.readLock().lock();
        try {
            for (Tuple tuple : this.tuples) {
                if (tuple.matches(pattern)) {
                    debugMessage("Found tuple: " + tuple);
                    return tuple.getTuple();
                }
            }
            debugMessage("Pattern %s not found", pattern);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String read(String pattern) {
        // TODO this should block if not found tuple, revert
        lock.readLock().lock();
        try {
            return getMatchingTuple(pattern);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> takePhase1(String pattern, int clientId) {
        debugMessage("Take Phase 1. Pattern: " + pattern + ", clientId: " + clientId);
        lock.writeLock().lock();
        try {
            List<String> stuples = new ArrayList<>();
            for (Tuple t : tuples) {
                if (t.matches(pattern)) {
                    if (t.isLocked())
                        // TODO should we unlock all locked until now????
                        return new ArrayList<>();
                    t.lock(clientId);
                    stuples.add(t.getTuple());
                }
            }
            return stuples;
        } catch (TupleLockedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void releaseAll(int clientId) {
        for (Tuple t : tuples) {
            if (t.getClientId() == clientId)
                t.unlock();
        }
    }

    public void takePhase1Release(int clientId) {
        debugMessage("Take Phase 1 Release. clientId: " + clientId);
        lock.writeLock().lock();
        try {
            releaseAll(clientId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeTuple(String tuple, int clientId) {
        for (Tuple t : tuples) {
            if (t.getTuple().equals(tuple) && t.getClientId() == clientId) {
                tuples.remove(t);
                break;
            }
        }
    }

    public void takePhase2(String tuple, int clientId) {
        debugMessage("Take Phase 2. Tuple: " + tuple + ", clientId: " + clientId);
        lock.writeLock().lock();
        try {
            removeTuple(tuple, clientId);
            releaseAll(clientId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<String> getAllTuples() {
        List<String> stuples = new ArrayList<>();
        for (Tuple t : tuples) {
            stuples.add(t.getTuple());
        }
        return stuples;
    }

    public List<String> getTupleSpacesState() {
        lock.readLock().lock();
        try {
            return getAllTuples();
        } finally {
            lock.readLock().unlock();
        }
    }
}
