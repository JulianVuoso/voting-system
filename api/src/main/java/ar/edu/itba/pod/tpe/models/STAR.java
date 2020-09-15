package ar.edu.itba.pod.tpe.models;

import java.util.*;

public class STAR extends Result {
    private static final long serialVersionUID = 6273175734504242179L;

    private Map<String, Integer> firstRound;
    private Map<String, Double> secondRound;

    public STAR(Map<String, Integer> firstRound, Map<String, Double> secondRound) {
        this.firstRound = firstRound;
        this.secondRound = secondRound;
        this.winner[0] = Collections.max(secondRound.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
        this.partial= false;
        this.type = Type.STAR;
    }

    public STAR(List<Vote> voteList) {
        this.firstRound = firstSTAR(voteList);
        this.secondRound = secondSTAR(voteList);
        this.winner[0] = Collections.max(secondRound.entrySet(),
                (o1, o2) -> {
                    int compare = Double.compare(o1.getValue(), o2.getValue());
                    return (compare != 0) ? compare : o2.getKey().compareTo(o1.getKey());
                }).getKey();
        this.partial= false;
        this.type = Type.STAR;
    }

    public String getWinner(){
        return winner[0];
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
            for(String party : vote.getSTAR().keySet()){
                firstStar.put(party, firstStar.getOrDefault(party, 0) + vote.getSTAR().get(party));
            }
        }
        return firstStar;
    }

    private Map<String, Double> secondSTAR(List<Vote> voteList) {
        Map<String, Double> secondStar = new HashMap<>();
        Map<String, Integer> points = new HashMap<>();

        final String[] winners = firstRound.entrySet().stream()
                .sorted((o1, o2) -> {
                    int compare = Integer.compare(o2.getValue(), o1.getValue());
                    return (compare != 0) ? compare : o1.getKey().compareTo(o2.getKey());
                })
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
            int firstVotes = vote.getSTAR().getOrDefault(winners[0], 0),
                    secondVotes = vote.getSTAR().getOrDefault(winners[1], 0);

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
        int total = points.getOrDefault(winners[0], 0) + points.getOrDefault(winners[1], 0);
        secondStar.put(winners[0], points.get(winners[0]).doubleValue() / total * 100);
        secondStar.put(winners[1], 100 - secondStar.get(winners[0]));

        return secondStar;
    }
}
