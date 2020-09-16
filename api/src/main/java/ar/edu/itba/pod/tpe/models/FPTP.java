package ar.edu.itba.pod.tpe.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FPTP extends Result {
    private static final long serialVersionUID = 8511167759635565855L;

    private Map<String, Integer> map;
    private int total;

    /**
     * Constructor sets defaults.
     */
    public FPTP() {
        type = Type.FPTP;
        winners = new String[1];
        map = new HashMap<>();
        total = 0;
    }

    /**
     * Adds a vote given the party winner.
     * @param vote The vote to process.
     */
    public synchronized void addPartialVote(Vote vote) {
        if (partial) {
            map.put(vote.getWinner(), map.getOrDefault(vote.getWinner(), 0) + 1);
            total++;
        }
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
     * @param votes The list of votes.
     */
    public synchronized void setFinal(List<Vote> votes) {
        if (!partial) return;
        partial = false;

        map = votes.stream().collect(Collectors.groupingBy(Vote::getWinner, Collectors.reducing(0, e -> 1, Integer::sum)));
        total = votes.size();
        winners[0] = Collections.min(map.entrySet(), sortIntegerMap).getKey();
    }

    /**
     * Gets the FPTP final winner.
     * @return String for the FPTP final winner.
     */
    public String getWinner() {
        return winners[0];
    }

    /**
     * Checks if there are any votes.
     * @return Boolean if its empty or not.
     */
    public synchronized boolean isEmpty() {
        return total == 0;
    }
}