package ar.edu.itba.pod.tpe.exceptions;

public class IllegalElectionStateException extends Exception {
    private static final long serialVersionUID = 2459136901084292400L;

    public IllegalElectionStateException() {
        super();
    }

    public IllegalElectionStateException(String message) {
        super(message);
    }
}
