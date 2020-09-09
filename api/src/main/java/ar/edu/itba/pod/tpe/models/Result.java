package ar.edu.itba.pod.tpe.models;

import java.io.Serializable;

public abstract class Result implements Serializable {
    String[] winner;
    Type type;
    boolean partial;
}
