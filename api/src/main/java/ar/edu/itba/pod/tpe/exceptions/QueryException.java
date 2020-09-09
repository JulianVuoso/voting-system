package ar.edu.itba.pod.tpe.exceptions;

public class QueryException extends RuntimeException{
    public QueryException() {
        super();
    }

    public QueryException(String message) {
        super(message);
    }
}
