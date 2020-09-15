package ar.edu.itba.pod.tpe.models;

import java.io.Serializable;
import java.util.Map;

public class Vote implements Serializable {

    private static final long serialVersionUID = 2520379198459818215L;

    private int table;
    private String state;
    private Map<String,Integer> scoreMap;
    private String winner;


    /**
     * Constructor
     * @param table Table number
     * @param state State name
     * @param star Map with party name as key, and vote value as value
     * @param winner Name of the winning party
     */
    public Vote(Integer table, String state, Map<String, Integer> star, String winner) {
        this.table = table;
        this.state = state;
        this.scoreMap = star;
        this.winner = winner;
    }

    /**
     * Gets the table where the vote was made.
     * @return Table number.
     */
    public Integer getTable() {
        return table;
    }

    /**
     * Gets the state where the vote was made.
     * @return The state name.
     */
    public String getState() {
        return state;
    }

    /**
     * Gets the winner of the vote.
     * @return The name of the winner party.
     */
    public String getWinner() {
        return winner;
    }

    /**
     * Gets the vote score map.
     * @return A map with the party as key and the score as value.
     */
    public Map<String, Integer> getScoreMap() {
        return scoreMap;
    }

}