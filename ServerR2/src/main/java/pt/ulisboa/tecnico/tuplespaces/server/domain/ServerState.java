package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.server.util.Tuple;

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
            debugMessage("Patter %s not found", pattern);
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String read(String pattern) {
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

    public List<String> takePhase1(String pattern, int clientId) {
    }

    public void takePhase1Release(int clientId) {
        lock.writeLock().lock();
        try {
            for (Tuple t : tuples) {
                if (t.getClientId() == clientId)
                    t.unlock();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeTuple(String tuple) {
        for (Tuple t : tuples) {
            if (t.getTuple().equals(tuple)) {
                tuples.remove(t);
                break;
            }
        }
    }

    public void takePhase2(String tuple, int clientId) {
        // TODO identify duplicated removes and don't remove again
        lock.writeLock().lock();
        try {
            removeTuple(tuple);
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
