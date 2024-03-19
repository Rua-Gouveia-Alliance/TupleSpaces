package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.server.util.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static pt.ulisboa.tecnico.tuplespaces.server.ServerMain.debugMessage;

public class ServerState {

    private final List<String> tuples;
    private int seqCounter;
    private Condition condSeq;
    private List<Tuple> waitingTuples;
    private Lock lock;

    public ServerState() {
        this.tuples = new ArrayList<>();
        this.waitingTuples = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.condSeq = this.lock.newCondition();
        this.seqCounter = 1;
    }

    public void put(String tuple, int seqNumber) {
        this.lock.lock();

        try {
            // wait for turn
            while (this.seqCounter < seqNumber) {
                try {
                    condSeq.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // increment seqCounter
            this.seqCounter++;

            // notify waiting take specifically
            if (!notifyTake(tuple)) {
                tuples.add(tuple);      // actual put
                debugMessage("Add tuple: " + tuple);
                condSeq.signalAll();
            }

        } finally {
            this.lock.unlock();
        }
    }

    public boolean notifyTake(String tuple) {
        for (Tuple t : this.waitingTuples) {
            if (tuple.matches(t.getPattern())) {
                t.setTuple(tuple);
                t.getCond().signal();
                return true;
            }
        }
        return false;
    }

    public String read(String pattern) {
        this.lock.lock();
        try {
            String tuple = getMatchingTuple(pattern);
            return tuple == null ? "" : tuple;
        } finally {
            this.lock.unlock();
        }
    }

    public String take(String pattern, int seqNumber) {
        this.lock.lock();

        try {
            // wait for turn
            while (this.seqCounter < seqNumber) {
                try {
                    condSeq.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // increment seqCounter
            this.seqCounter++;

            // actual take
            String tuple = getMatchingTuple(pattern);
            if (tuple == null) {
                Tuple wTuple = new Tuple(pattern, this.lock);
                waitingTuples.add(wTuple);
                condSeq.signalAll();
                while (tuple == null) {
                    try {
                        wTuple.getCond().await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    tuple = wTuple.getTuple();
                }
                waitingTuples.remove(wTuple);
            } else {
                tuples.remove(tuple);
                debugMessage("Take tuple: " + tuple);
            }

            // notify next inline
            condSeq.signalAll();

            return tuple;
        } finally {
            this.lock.unlock();
        }
    }

    public List<String> getTupleSpacesState() {
        return new ArrayList<>(this.tuples);
    }

    private String getMatchingTuple(String pattern) {
        for (String tuple : this.tuples) {
            if (tuple.matches(pattern)) {
                debugMessage("Found tuple: " + tuple);
                return tuple;
            }
        }
        debugMessage("Pattern %s not found", pattern);
        return null;
    }

    public void removeTuple(String tuple) {
        this.tuples.remove(tuple);
    }

}
