package ar.edu.itba.pod.tpe.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FPTP extends Result {
    private static final long serialVersionUID = 8511167759635565855L;

    private Map<String, Integer> map;
    private Integer total;


    /**
     * Constructor sets defaults.
     */
    public FPTP() {
        partial = true;
        type = Type.FPTP;
        winners = new String[1];
        map = new HashMap<>();
        total = 0;
    }

    /**
     * Adds a vote given the party winner.
     * @param party The required party to add a vote.
     */
    public void addVote(String party) {
        map.put(party, map.getOrDefault(party, 0) + 1);
        total++;
    }

    /**
     * Gets a map with the percentage for each party.
     * @return The result map.
     */
    public Map<String, Double> getPercentagesMap() {
        return map
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().doubleValue() / total * 100));
    }

    /**
     * Sets the the Result to final and calculates the winner.
     */
    public void setFinal() {
        partial = false;
        winners[0] = calculateWinner();
    }

    /**
     * Gets the FPTP final winner.
     * @return String for the FPTP final winner.
     */
    public String getWinner() {
        return winners[0];
    }


    /**
     * Auxiliary functions.
     */


    /**
     * Calculates the winner given the collection.
     * @return The winner.
     */
    private String calculateWinner() {
        return Collections.max(map.entrySet(), sortIntegerMap).getKey();
    }

}