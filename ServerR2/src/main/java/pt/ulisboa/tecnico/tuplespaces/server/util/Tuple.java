package pt.ulisboa.tecnico.tuplespaces.server.util;

import pt.ulisboa.tecnico.tuplespaces.server.util.exception.TupleLockedException;

public class Tuple {

    private final String tuple;
    private int clientId;

    public Tuple (String tuple) {
        this.tuple = tuple;
        this.clientId = -1;
    }

    public boolean isLocked() {
        return clientId != -1;
    }

    public void lock(int clientId) throws TupleLockedException {
        if (this.isLocked() && this.clientId != clientId)
            throw new TupleLockedException("Tuple already locked by another user");
        this.clientId = clientId;
    }

    public void unlock() {
        this.clientId = -1;
    }

    public int getClientId() {
        return this.clientId;
    }

    public String getTuple()  {
        return this.tuple;
    }

    public boolean matches(String pattern) {
        return this.tuple.matches(pattern);
    }

}
