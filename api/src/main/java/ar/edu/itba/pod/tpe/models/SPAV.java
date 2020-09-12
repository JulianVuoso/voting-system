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

    public SPAV(Map<String, Double> round1, Map<String, Double> round2, Map<String, Double> round3, String[] winners) {
        this.round1 = round1;
        this.round2 = round2;
        this.round3 = round3;
        this.winner = winners;
        this.partial = false;
        this.type = Type.SPAV;
    }

    public SPAV(List<Vote> voteList) {
        this.round1 = spavIterator(voteList, this.winner);
        this.winner[0] = getWinner(this.round1);

        this.round2 = spavIterator(voteList, this.winner);
        this.winner[1] = getWinner(this.round2);

        this.round3 = spavIterator(voteList, this.winner);
        this.winner[2] = getWinner(this.round3);

        this.partial = false;
        this.type = Type.SPAV;
    }

    public boolean getPartial(){
        return partial;
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

    public String[] getWinner(){
        return winner;
    }

    private Map<String, Double> spavIterator(List<Vote> voteList, String[] winners){
        Map<String, Double> spavRound = new HashMap<>();
        for(Vote vote : voteList){                                // por cada voto
            Map<String, Double> mapVote = vote.getSPAV(winners);    // obtengo su party -> puntaje
            for(String party : mapVote.keySet()){
                spavRound.put(party, spavRound.getOrDefault(party, 0.0) + mapVote.get(party));
            }
        }
        return spavRound;
    }

    private String getWinner(Map<String, Double> round) {
        return Collections.max(round.entrySet(),
                (o1, o2) -> o1.getValue() > o2.getValue()?
                        1:(o1.getValue().equals(o2.getValue())?
                        (o2.getKey().compareTo(o1.getKey())):-1)).getKey();
    }
}
