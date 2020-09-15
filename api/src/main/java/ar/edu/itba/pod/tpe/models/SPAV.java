package ar.edu.itba.pod.tpe.models;

import java.util.*;
import java.util.stream.Collectors;
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
        winners = new String[maxRounds];

        IntStream.range(0, maxRounds).forEach(n -> {
            rounds.add(n, fillRound(voteList));
            winners[n] = calculateWinner(rounds.get(n));
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
        return winners;
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
        voteList.forEach(vote -> processVote(vote.getScoreMap().keySet())
                        .forEach((key, val) -> roundMap.merge(key, val, Double::sum))); // This is for each key-value pair returned
        return roundMap;
    }

    /**
     * Process the vote according to the previous winners.
     * @param votedParties The parties that obtained at least a 1.
     * @return
     */
    private Map<String, Double> processVote(Set<String> votedParties) {
        // When there is no previous winners, all votes are taken into account
        if (winners.length == 0) {
            return votedParties.stream().collect(Collectors.toMap(party -> party, party -> 1d));
        }

        // There were already winners on previous rounds. Remove those from parties.
        Set<String> parties = new HashSet<>(votedParties);
        parties.removeAll(new HashSet<>(Arrays.asList(winners)));

        // Add parties and respective points without counting the previous winners
        return parties.stream().collect(Collectors.toMap(party -> party, party -> 1.0 / (votedParties.size() - parties.size() + 1)));
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
