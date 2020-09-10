package ar.edu.itba.pod.tpe.models;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class STAR extends Result {
    Map<String, Integer> firstRound;
    Map<String, Double> secondRound;

    public STAR(Map<String, Integer> firstRound, Map<String, Double> secondRound) {
        this.firstRound = firstRound;
        this.secondRound = secondRound;
        this.winner[0] = Collections.max(firstRound.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        this.partial= false;
        this.type = Type.STAR;
    }

    public String getWinner(){
        return winner[0];
    }

}
