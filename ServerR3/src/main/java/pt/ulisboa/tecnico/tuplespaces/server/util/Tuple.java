package pt.ulisboa.tecnico.tuplespaces.server.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import pt.ulisboa.tecnico.tuplespaces.server.util.exception.TupleLockedException;

public class Tuple {

    private final String pattern;
    private Condition cond;
    private String tuple;

    public Tuple(String pattern, Lock lock) {
        this.pattern = pattern;
        this.cond = lock.newCondition();
        this.tuple = null;
    }

    public String getPattern() {
        return this.pattern;
    }

    public Condition getCond() {
        return this.cond;
    }

    public String getTuple() {
        return this.tuple;
    }

    public void setTuple(String tuple) {
        this.tuple = tuple;
    }

}
