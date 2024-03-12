package pt.ulisboa.tecnico.tuplespaces.server.util.exception;

public class TupleLockedException extends Exception {
    public TupleLockedException(String errorMessage) {
        super(errorMessage);
    }
}
