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

    public int lock(int clientId) {
        if (!this.canBeLocked(clientId))
            return -1;
        this.clientId = clientId;
        return 0;
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

    public boolean canBeLocked(int clientId) {
        return !this.isLocked() || this.clientId == clientId;
    }
}
