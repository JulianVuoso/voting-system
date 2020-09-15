package ar.edu.itba.pod.tpe.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FPTP extends Result {
    private static final long serialVersionUID = 8511167759635565855L;

    private Map<String, Integer> map;
    private Integer total;

    /**
     * Constructor sets defaults.
     */
    public FPTP() {
        this.map = new HashMap<>();
        this.partial = true;
        this.type = Type.FPTP;
        this.total = 0;
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
        this.partial = false;
        this. winner[0] = calculateWinner();
    }

    /**
     * Calculates the winner given the collection.
     * @return The winner.
     */
    private String calculateWinner() {
        return Collections.max(map.entrySet(), sortIntegerMap).getKey();
    }

    /**
     * Gets the FPTP final winner.
     * @return String for the FPTP final winner.
     */
    public String getWinner() {
        return winner[0];
    }

}