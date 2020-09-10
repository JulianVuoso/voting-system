package ar.edu.itba.pod.tpe.models;

import java.util.Map;

public class SPAV extends Result {
    Map<String, Double> round1;
    Map<String, Double> round2;
    Map<String, Double> round3;

    public SPAV(Map<String, Double> round1, Map<String, Double> round2, Map<String, Double> round3, String[] winners) {
        this.round1 = round1;
        this.round2 = round2;
        this.round3 = round3;
        this.winner = winners;
        this.partial = false;
        this.type = Type.SPAV;
    }
}
