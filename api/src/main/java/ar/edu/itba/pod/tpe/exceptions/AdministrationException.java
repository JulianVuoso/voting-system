package ar.edu.itba.pod.tpe.exceptions;

public class AdministrationException extends Exception {

    private static final long serialVersionUID = 1533564635752790375L;

    public AdministrationException() {
        super();
    }

    public AdministrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdministrationException(String message) {
        super(message);
    }

    public AdministrationException(Throwable cause) {
        super(cause);
    }
}
