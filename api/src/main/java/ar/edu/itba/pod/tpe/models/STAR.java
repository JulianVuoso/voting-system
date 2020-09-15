package ar.edu.itba.pod.tpe.models;

import java.util.*;

public class STAR extends Result {
    private static final long serialVersionUID = 6273175734504242179L;

    private Map<String, Integer> firstStage;
    private Map<String, Double> secondStage;


    /**
     * Constructor sets defaults.
     * @param votes
     */
    public STAR(List<Vote> votes) {
        type = Type.STAR;
        winners = new String[1];

        firstStage = fillFirstStage(votes);
        secondStage = fillSecondStage(votes);

        winners[0] = Collections.max(secondStage.entrySet(), sortDoubleMap).getKey();
    }

    /**
     * Gets the STAR final winner.
     * @return String for the STAR final winner.
     */
    public String getWinner(){
        return winners[0];
    }

    /**
     * Standard getter for the first stage.
     * @return Map with the party and the added score.
     */
    public Map<String, Integer> getFirstStage() {
        return firstStage;
    }

    /**
     * Standard getter for the first stage.
     * @return Map with the party and percentage of votes.
     */
    public Map<String, Double> getSecondStage() {
        return secondStage;
    }


    /**
     * Auxiliary functions.
     */

    /**
     * Fills the first stage by adding all the scores for each vote.
     * @param votes The list of all the votes.
     * @return A map with the party and the added score.
     */
    private Map<String, Integer> fillFirstStage(List<Vote> votes) {
        Map<String, Integer> firstStar = new HashMap<>();
        votes.forEach(vote -> vote.getScoreMap()
                .forEach((key, val) -> firstStar.merge(key, val, Integer::sum))); // This is for each key-value pair from score map
        return firstStar;
    }

    /**
     * Fills the second stage by counting only for the previous 2 winners.
     * @param votes The list of all the votes
     * @return A map with the party and percentage of votes.
     */
    private Map<String, Double> fillSecondStage(List<Vote> votes) {
        final String[] winners = firstStage.entrySet().stream()
                .sorted(sortIntegerMap)
                .limit(2)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);

        // Only one or no winner cases
        if (winners.length == 1) return Collections.singletonMap(winners[0], 100.0);
        if (winners.length == 0) return Collections.emptyMap();

        Map<String, Integer> points = new HashMap<>();
        String winnerAlpha = winners[0].compareTo(winners[1]) < 0 ? winners[0]:winners[1];

        // TODO: I do not like this, would like something more java 8
        for(Vote vote : votes) {
            int firstVotes = vote.getScoreMap().getOrDefault(winners[0], 0);
            int secondVotes = vote.getScoreMap().getOrDefault(winners[1], 0);

            // Discard if both did not have votes
            if (firstVotes != 0 || secondVotes != 0) {
                if (firstVotes > secondVotes) {
                    points.put(winners[0], points.getOrDefault(winners[0], 0) + 1);
                } else if (firstVotes < secondVotes) {
                    points.put(winners[1], points.getOrDefault(winners[1], 0) + 1);
                } else {
                    points.put(winnerAlpha, points.getOrDefault(winnerAlpha, 0) + 1);
                }
            }
        }

        // Calculate total of votes
        int total = points.get(winners[0]) + points.get(winners[1]);

        // Return created map with percentages of the winners
        Map<String, Double> secondStageResults = new HashMap<>();
        secondStageResults.put(winners[0], points.get(winners[0]).doubleValue() / total * 100);
        secondStageResults.put(winners[1], 100 - secondStageResults.get(winners[0]));
        return secondStageResults;
    }

}
