package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import static pt.ulisboa.tecnico.tuplespaces.server.ServerMain.debugMessage;

public class ServerState {

    private final List<String> tuples;
    private final ReadWriteLock lock;

    public ServerState() {
        this.tuples = new ArrayList<String>();
        this.lock = new ReentrantReadWriteLock();
    }

    public synchronized void put(String tuple) {
        lock.writeLock().lock();
        try {
            tuples.add(tuple);
            debugMessage("Add tuple: " + tuple);
            notifyAll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String getMatchingTuple(String pattern) {
        lock.readLock().lock();
        try {
            for (String tuple : this.tuples) {
                if (tuple.matches(pattern)) {
                    debugMessage("Found tuple: " + tuple);
                    return tuple;
                }
            }
            debugMessage("Pattern %s not found", pattern);
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

    public String take(String pattern) {
        String tuple;
        boolean ret;
        do {
            tuple = read(pattern);
            // more than one thread can arrive here with the same tuple to be removed
            lock.writeLock().lock();
            try {
                // tries to remove the tuple but one of the threads might already have removed
                ret = tuples.remove(tuple);
            } finally {
                lock.writeLock().unlock();
            }
        } while (!ret); // because in the moment of removal the tuple can no longer exist we need to make sure
        debugMessage("Removed tuple " + tuple);
        return tuple;
    }

    public List<String> getTupleSpacesState() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(tuples);
        } finally {
            lock.readLock().unlock();
        }
    }
}
