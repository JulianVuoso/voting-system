package ar.edu.itba.pod.tpe.models;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPAV extends Result {
    private static final long serialVersionUID = 2779903270007229997L;

    private Map<String, Double> round1;
    private Map<String, Double> round2;
    private Map<String, Double> round3;

    /**
     * Constructor sets defaults.
     */
    public SPAV(List<Vote> voteList) {
        this.round1 = fillRound(voteList);
        this.winner[0] = calculateWinner(this.round1);

        this.round2 = fillRound(voteList);
        this.winner[1] = calculateWinner(this.round2);

        this.round3 = fillRound(voteList);
        this.winner[2] = calculateWinner(this.round3);

        this.partial = false;
        this.type = Type.SPAV;
    }

    public Map<String, Double> getRound1() {
        return round1;
    }

    public Map<String, Double> getRound2() {
        return round2;
    }

    public Map<String, Double> getRound3() {
        return round3;
    }


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
            for(String party : mapVote.keySet()) {
                roundMap.put(party, roundMap.getOrDefault(party, 0.0) + mapVote.get(party));
            }
        }
        return roundMap;
    }

    /**
     * Calculates the winner for the given round.
     * @param round The round to calculate the winner.
     * @return The winner.
     */
    private String calculateWinner(Map<String, Double> round) {
        return Collections.max(round.entrySet(),
                (o1, o2) -> o1.getValue() > o2.getValue()?
                        1:(o1.getValue().equals(o2.getValue())?
                        (o2.getKey().compareTo(o1.getKey())):-1)).getKey();
    }

    /**
     * Gets the SPAV final winneres.
     * @return Strings for the SPAV final winneres.
     */
    public String[] getWinner(){
        return winner;
    }

}
