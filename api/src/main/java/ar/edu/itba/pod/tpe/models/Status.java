package ar.edu.itba.pod.tpe.models;

import java.io.Serializable;

public enum Status implements Serializable {
    REGISTRATION("In Registration"),
    STARTED("Started"),
    OPEN("Open"),
    ENDED("Ended"),
    CLOSE("Closed");

    private String message;

    Status(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
