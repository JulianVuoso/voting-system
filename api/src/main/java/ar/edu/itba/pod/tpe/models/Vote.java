package ar.edu.itba.pod.tpe.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Vote implements Serializable {

    private Integer table;
    private String state;
    private Map<String,Integer> star;
    private String voteFPTP;

    /**
     * table => número de mesa
     * state => provincia
     * star => Mapa con Key: nombre de partido, value: valor del voto
     *          si agarro el keyset me sirve para SPAV, y key-value me sirve para STAR
     * voteFPTP => String con nombre del partido ganador
     */

    /**
     *
     * @param table Table number
     * @param state State name
     * @param star Map with party name as key, and vote value as value
     * @param voteFPTP Name of the winning party
     */
    public Vote(Integer table, String state, Map<String, Integer> star, String voteFPTP) {
        this.table = table;
        this.state = state;
        this.star = star;
        this.voteFPTP = voteFPTP;
    }

    public Integer getTable() {
        return table;
    }

    public String getState() {
        return state;
    }

    public Map<String, Integer> getSTAR() {
        return star;
    }

    public String getVoteFPTP() {
        return voteFPTP;
    }

    public Map<String, Double> getSPAV(String[] winners){
        Map<String, Double> results = new HashMap<>();
        if(winners == null || winners.length == 0){
            //si no hay ganadores anteriores => en el mapa van todos los de la boleta con valor 1
            for(String party : star.keySet() ){
                results.put(party, 1d);
            }
        }
        else{
            Set<String> parties = new HashSet<>(star.keySet());
            // ya hubo ganadores en las rondas anteriores
            int winnersMatching = 1;
            for(String w : winners){
                //me fijo cuantos de los ganadores hay en la boleta y los sumo en winnersMatching
                if(star.containsKey(w)){
                    winnersMatching++;
                    parties.remove(w);
                }
            }
            // Agrego los partidos y respectivos puntajes al nuevo mapa
            // sin tener en cuenta el/los ganador/es anterior/es
            for( String party : parties){
                results.put(party, 1.0 / winnersMatching);
            }
        }
        return results;
    }
}