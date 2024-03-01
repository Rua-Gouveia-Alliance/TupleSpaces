package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerState {

    private List<String> tuples;
    private ReadWriteLock lock;

    public ServerState() {
        this.tuples = new ArrayList<String>();
        this.lock = new ReentrantReadWriteLock();
    }

    public synchronized void put(String tuple) {
        lock.writeLock().lock();
        try {
            tuples.add(tuple);
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
                    return tuple;
                }
            }
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

            lock.writeLock().lock();
            try {
                ret = tuples.remove(tuple);
            } finally {
                lock.writeLock().unlock();
            }
        } while (!ret); // because in the moment of removal the tuple can no longer exist we need to make sure
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
