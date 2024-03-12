package pt.ulisboa.tecnico.tuplespaces.client.util.exceptions;

public class AllServersFailedException extends Exception {
    public AllServersFailedException(String errorMessage) {
        super(errorMessage);
    }
}
