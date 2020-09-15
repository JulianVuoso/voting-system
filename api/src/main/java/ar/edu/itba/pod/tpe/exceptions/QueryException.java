package ar.edu.itba.pod.tpe.exceptions;

public class QueryException extends Exception{
    private static final long serialVersionUID = 8808123341922362522L;

    public QueryException() {
        super();
    }

    public QueryException(String message) {
        super(message);
    }
}
