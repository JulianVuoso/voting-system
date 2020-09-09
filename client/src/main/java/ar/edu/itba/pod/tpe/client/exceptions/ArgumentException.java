package ar.edu.itba.pod.tpe.client.exceptions;

import java.util.function.Supplier;

public class ArgumentException extends Exception implements Supplier<ArgumentException> {
    private static final long serialVersionUID = 7857831376678020829L;

    public ArgumentException(String message) {
        super(message);
    }

    @Override
    public ArgumentException get() {
        return this;
    }
}
