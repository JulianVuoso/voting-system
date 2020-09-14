package ar.edu.itba.pod.tpe.models;

import java.io.Serializable;

public abstract class Result implements Serializable {
    String[] winner = new String[3];
    Type type;
    boolean partial;

    public boolean isPartial() {
        return partial;
    }
}
