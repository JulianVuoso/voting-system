package ar.edu.itba.pod.tpe.models;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

public abstract class Result implements Serializable {
    String[] winner = new String[3];
    Type type;
    boolean partial;

    /**
     * Public comparators for Maps.
     */
    public static final Comparator<Map.Entry<String, Double>> sortDoubleMap = Map.Entry.<String, Double>comparingByValue().thenComparing(Map.Entry.comparingByKey());
    public static final Comparator<Map.Entry<String, Integer>> sortIntegerMap = Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey());

    /**
     * Default implementation for partial getter.
     * @return Partial value.
     */
    public boolean isPartial() {
        return partial;
    }

}
