package ar.edu.itba.pod.tpe.models;

import java.util.*;

public class STAR extends Result {
    private static final long serialVersionUID = 6273175734504242179L;

    private Map<String, Integer> firstRound;
    private Map<String, Double> secondRound;


    public STAR(List<Vote> voteList) {
        this.firstRound = firstSTAR(voteList);
        this.secondRound = secondSTAR(voteList);
        this.winners[0] = Collections.max(secondRound.entrySet(), sortDoubleMap).getKey();
        this.partial= false;
        this.type = Type.STAR;
        winners = new String[1];
    }

    public String getWinner(){
        return winners[0];
    }

     public boolean getPartial() {
        return partial;
     }

    public Map<String, Integer> getFirstRound() {
        return firstRound;
    }

    public Map<String, Double> getSecondRound() {
        return secondRound;
    }


    private Map<String, Integer> firstSTAR(List<Vote> voteList) {
        Map<String, Integer> firstStar = new HashMap<>();
        for(Vote vote : voteList){
            for(String party : vote.getScoreMap().keySet()){
                firstStar.put(party, firstStar.getOrDefault(party, 0) + vote.getScoreMap().get(party));
            }
        }
        return firstStar;
    }

    private Map<String, Double> secondSTAR(List<Vote> voteList) {
        Map<String, Double> secondStar = new HashMap<>();
        Map<String, Integer> points = new HashMap<>();

        final String[] winners = firstRound.entrySet().stream()
                .sorted(sortIntegerMap)
                .limit(2)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);

        if (winners.length < 2) {
            for (String winner : winners) {
                secondStar.put(winner, 100.0);
            }
            return secondStar;
        }

        String winnerAlpha = winners[0].compareTo(winners[1]) < 0 ? winners[0]:winners[1];
        for(Vote vote : voteList) {
            int firstVotes = vote.getScoreMap().getOrDefault(winners[0], 0),
                    secondVotes = vote.getScoreMap().getOrDefault(winners[1], 0);

            // Si ambos sumaron 0, se descarta la boleta
            if (firstVotes != 0 || secondVotes != 0) {
                if (firstVotes > secondVotes) { // Voto w1 > voto w2
                    points.put(winners[0], points.getOrDefault(winners[0], 0) + 1);
                } else if (firstVotes < secondVotes) { // Voto w1 < voto w2
                    points.put(winners[1], points.getOrDefault(winners[1], 0) + 1);
                } else { // Son iguales, sumo al menor alfabeticamente
                    points.put(winnerAlpha, points.getOrDefault(winnerAlpha, 0) + 1);
                }
            }
        }
        int total = points.get(winners[0]) + points.get(winners[1]);
        secondStar.put(winners[0], points.get(winners[0]).doubleValue() / total * 100);
        secondStar.put(winners[1], 100 - secondStar.get(winners[0]));

        return secondStar;
    }
}
