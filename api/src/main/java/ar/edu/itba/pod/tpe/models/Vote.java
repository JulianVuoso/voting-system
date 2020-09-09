package ar.edu.itba.pod.tpe.models;

import java.io.Serializable;
import java.util.Map;

public class Vote implements Serializable {

    private Integer table;
    private String state;
    private Map<String,Integer> party; // ==>String = partido ; Integer = Cant de puntos que le asigno
    private String voteFPTP;

    /**
     * table => numero de mesa
     * state => provincia
     * party => Mapa con Key: nombre de partido, value: valor del voto
     *          si agarro el keyset me sirve para SPAV, y key-value me sirve para STAR
     * voteFPTP => String con nombre del partido ganador
     */

    public Vote(Integer table, String state, Map<String, Integer> party, String voteFPTP) {
        this.table = table;
        this.state = state;
        this.party = party;
        this.voteFPTP = voteFPTP;
    }

    public Integer getTable() {
        return table;
    }

    public String getState() {
        return state;
    }

    public Map<String, Integer> getParty() {
        return party;
    }

    public String getVoteFPTP() {
        return voteFPTP;
    }
}