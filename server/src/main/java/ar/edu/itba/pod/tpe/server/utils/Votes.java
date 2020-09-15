package ar.edu.itba.pod.tpe.server.utils;

import ar.edu.itba.pod.tpe.models.Vote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Votes {

    private Map<String, Map<Integer, List<Vote>>> votes;

    public Votes() {
        votes = new HashMap<>();
    }

    public synchronized void addVote(Vote vote) {
        String state = vote.getState();
        Integer table = vote.getTable();

        if (!votes.containsKey(state)) {
            votes.put(state, new HashMap<>());
        }

        if (!votes.get(state).containsKey(vote.getTable())) {
            votes.get(state).put(table, new ArrayList<>());
        }

        votes.get(state).get(table).add(vote);
    }

    public List<Vote> getVoteList() {
        List<Vote> totalVotes = new ArrayList<>();
        votes.values().forEach(vote -> vote.values().forEach(totalVotes::addAll));
        return totalVotes;
    }

    public List<Vote> getStateVoteList(String state) {
        List<Vote> stateVotes = new ArrayList<>();
        votes.get(state).values().forEach(stateVotes::addAll);
        return stateVotes;
    }

    public boolean isStateEmpty(String state) { // TODO: should be fixed
        return votes.get(state).values().isEmpty();
    }

    public boolean isTableEmpty(Integer table) { // TODO: fix
        for(Map<Integer, List<Vote>> maps : votes.values()){
            if(!maps.get(table).isEmpty())
                return false;
        }
        return true;
    }

}
