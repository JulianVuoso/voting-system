package ar.edu.itba.pod.tpe.exceptions;

public class NoVotesException extends Exception{
    private static final long serialVersionUID = 8808123341922362522L;

    public NoVotesException() {
        super();
    }

    public NoVotesException(String message) {
        super(message);
    }
}
