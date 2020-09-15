package ar.edu.itba.pod.tpe.models;

import java.util.*;
import java.util.stream.IntStream;

public class SPAV extends Result {
    private static final long serialVersionUID = 2779903270007229997L;

    private List<Map<String, Double>> rounds;
    public static final Integer maxRounds = 3;

    /**
     * Constructor sets defaults.
     * Fills the rounds.
     */
    public SPAV(List<Vote> voteList) {
        partial = false;
        type = Type.SPAV;
        rounds = new ArrayList<>();

        IntStream.range(0, maxRounds).forEach(n -> {
            rounds.add(n, fillRound(voteList));
            winner[n] = calculateWinner(rounds.get(n));
        });
    }


    /**
     * Gets the corresponding round.
     * @param number The round number to
     * @return
     */
    public Map<String, Double> getRound(int number) {
        return rounds.get(number);
    }


    /**
     * Gets the SPAV final winners.
     * @return Strings for the SPAV final winners.
     */
    public String[] getWinner(){
        return winner;
    }


    /**
     * Auxiliary methods
     */


    /**
     * Returns the map of a round given the time its executed
     * @param voteList The complete vote list.
     * @return
     */
    private Map<String, Double> fillRound(List<Vote> voteList) {
        Map<String, Double> roundMap = new HashMap<>();

        // For each vote
        for(Vote vote : voteList) {
            // Get its party -> score
            Map<String, Double> mapVote = vote.getSPAV(this.winner);
            mapVote.keySet().forEach(party -> roundMap.put(party, roundMap.getOrDefault(party, 0.0) + mapVote.get(party)));
        }
        return roundMap;
    }


    /**
     * Calculates the winner for the given round.
     * @param round The round to calculate the winner.
     * @return The winner.
     */
    private String calculateWinner(Map<String, Double> round) {
        return Collections.max(round.entrySet(), sortDoubleMap).getKey();
    }

}
