package ar.edu.itba.pod.tpe.models;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SPAV extends Result {
    private static final long serialVersionUID = 2779903270007229997L;

    private List<Map<String, Double>> rounds;
    public static final int maxRounds = 3;

    private FPTP partialResult;

    /**
     * Constructor sets values to default.
     */
    public SPAV() {
        type = Type.SPAV;
        winners = new String[maxRounds];
        partialResult = new FPTP();
        rounds = new ArrayList<>();
    }

    /**
     * Adds a vote given the party winner.
     * @param vote The vote to process.
     */
    public void addPartialVote(Vote vote) {
        partialResult.addPartialVote(vote);
    }

    /**
     * Checks if the partial result is empty.
     * @return True if its empty, false otherwise.
     */
    public boolean isPartialEmpty() {
        return partialResult.isEmpty();
    }

    /**
     * Gets the partial result.
     * @return Partial result in form of a FPTP.
     */
    public FPTP getPartialResult() {
        return partialResult;
    }

    /**
     * Sets the winners and stages given the list of votes.
     * @param votes The final list of votes.
     */
    public void setFinal(List<Vote> votes) {
        if (!partial) return;
        partial = false;

        IntStream.range(0, maxRounds).forEach(n -> {
            rounds.add(n, fillRound(votes));
            winners[n] = Collections.min(rounds.get(n).entrySet(), sortDoubleMap).getKey();
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
     * @param votes The complete vote list.
     * @return
     */
    private Map<String, Double> fillRound(List<Vote> votes) {
        Map<String, Double> roundMap = new HashMap<>();
        votes.forEach(vote -> processVote(vote.getScoreMap().keySet())
                        .forEach((key, val) -> roundMap.merge(key, val, Double::sum))); // This is for each key-value pair returned from processing
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

}
