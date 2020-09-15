package ar.edu.itba.pod.tpe.server.utils;

import ar.edu.itba.pod.tpe.models.Vote;

import java.util.*;

public class Votes {

    private Map<String, Map<Integer, List<Vote>>> votes;

    /**
     * Constructor creates Map of maps.
     */
    public Votes() {
        votes = new HashMap<>();
    }

    /**
     * Adds a new vote to the map.
     * @param vote The vote to be added.
     */
    public void addVote(Vote vote) {
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

    /**
     * Gets a list of all the votes received.
     * @return The list of votes.
     */
    public List<Vote> getVoteList() {
        List<Vote> totalVotes = new ArrayList<>();
        votes.values().forEach(vote -> vote.values().forEach(totalVotes::addAll));
        return totalVotes;
    }

    /**
     * Gets a list of all the votes received for a given state.
     * @param state The state to get all the votes.
     * @return The list of votes.
     */
    public List<Vote> getStateVoteList(String state) {
        List<Vote> stateVotes = new ArrayList<>();
        votes.get(state).values().forEach(stateVotes::addAll);
        return stateVotes;
    }

    /**
     * Gets a list of all the votes received in the given table.
     * @param table The table to get the list of votes.
     * @return The list of votes.
     */
    public List<Vote> getTableVoteList(int table) {
        Optional<Map<Integer, List<Vote>>> optionalMap =votes.values()
                .stream()
                .filter(map -> map.containsKey(table))
                .findFirst();
        if (optionalMap.isPresent())
            return optionalMap.get().get(table);
        return Collections.emptyList();
    }

    /**
     * Checks if there is any votes at all.
     * @return True if its empty, false otherwise.
     */
    public boolean isEmpty() {
        return votes.isEmpty();
    }

    /**
     * Checks if there is any votes to the given state.
     * @param state The state to check.
     * @return True if its empty, false otherwise.
     */
    public boolean isStateEmpty(String state) {
        return !votes.containsKey(state);
    }

    /**
     * Checks on all the states if there is a matching table.
     * @param table The table number to check.
     * @return True if there is no table with votes, false otherwise.
     */
    public boolean isTableEmpty(int table) {
        return votes.values().stream().noneMatch(maps -> maps.containsKey(table));
    }

}
