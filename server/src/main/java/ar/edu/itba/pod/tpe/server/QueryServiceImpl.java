package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.interfaces.QueryService;
import ar.edu.itba.pod.tpe.models.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public class QueryServiceImpl implements QueryService {

    int status;
    Map<String, Map<Integer, List<Vote>>> votes;

    /* Finals */
    STAR natStar;
    SPAV stateSpav;

    /*  Partials */
    FPTP natFptp = new FPTP();
    Map<String, FPTP> stateFptp;
    Map<Integer, FPTP> tableFptp;

    public QueryServiceImpl() {
        this.status = 0;
        this.votes = new HashMap<>();
    }

    @Override
    public Result askNational() throws RemoteException, QueryException {
        if(status==0) throw new QueryException("Polls already closed");
        if(status==1)                                           //  open
            return natFptp;
//        natStar = new STAR(firstSTAR(), secondSTAR());          //  finished
        return new STAR(firstSTAR(), secondSTAR());
    }

    @Override
    public Result askState(String state) throws RemoteException, QueryException {
        if(status==0) throw new QueryException("Polls already closed");
        if(status==1)                                          //  open
            return stateFptp.get(state);

        String[] winners = new String[3];
        Map<String, Double> round1 = spavIterator(state, null);
        winners[0] = Collections.max(round1.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
        Map<String, Double> round2 = spavIterator(state, winners);
        winners[1] = Collections.max(round2.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
        Map<String, Double> round3 = spavIterator(state, winners);
        winners[2] = Collections.max(round3.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();

        return new SPAV(round1, round2, round3, winners);
    }

    private Map<String, Double> spavIterator(String state, String[] winners){
        List<Vote> stateVotes = new ArrayList<>();
        for(List<Vote> list : votes.get(state).values())        // me quedo con todos los votos del state
            stateVotes.addAll(list);

        Map<String, Double> spavRound = new HashMap<>();
        for(Vote vote : stateVotes){                                // por cada voto
            Map<String, Double> mapVote = vote.getSPAV(winners);    // obtengo su party -> puntaje
            Set<String> parties = mapVote.keySet();                 // y por cada party
            for(String party : parties){
                spavRound.putIfAbsent(party, 0.0);                  // le sumo al map general tal puntaje
                spavRound.put(party, spavRound.get(party) + mapVote.get(party));
            }
        }
        return spavRound;
    }


    @Override
    public Result askTable(Integer table) throws RemoteException, QueryException {
        if(status==0)
            throw new QueryException("Polls already closed");

        if(status==1)                               //  open
            return tableFptp.get(table);

        tableFptp.get(table).setPartial(false);     //  finished
        tableFptp.get(table).obtainWinner();
        return tableFptp.get(table);
    }

    @Override
    public void vote(Vote vote) {        // comento en español para que me sigan + facil despues lo traduzco
        natFptp.getMap().putIfAbsent(vote.getVoteFPTP(),0);                                     // NACIONAL: voto que entra, voto que se suma al mapa general FPTP
        natFptp.getMap().put(vote.getVoteFPTP(), natFptp.getMap().get(vote.getVoteFPTP()+1));

        stateFptp.putIfAbsent(vote.getState(), new FPTP());                                     // STATE: si es el primer voto de esa provincia le agrego un FPTP
        stateFptp.get(vote.getState()).getMap().put(vote.getVoteFPTP(), stateFptp.get(vote.getState()).getMap().get(vote.getVoteFPTP()+1));
                                                                                                // Luego obtengo ese FPTP y le meto en key Party 1 voto mas
        tableFptp.putIfAbsent(vote.getTable(), new FPTP());                                     // TABLE: same a state
        tableFptp.get(vote.getTable()).getMap().put(vote.getVoteFPTP(), tableFptp.get(vote.getTable()).getMap().get(vote.getVoteFPTP()+1));
    }

    private List<Vote> allVotes(){
        List<Vote> totalVotes = new ArrayList<>();
        for(Map<Integer, List<Vote>> vote : votes.values()){
            for(List<Vote> list : vote.values())
                totalVotes.addAll(list);
        }
        return totalVotes;
    }

    private Map<String, Integer> firstSTAR() {
//        List<Vote> totalVotes = allVotes();
        Map<String, Integer> firstStar = new HashMap<>();
        for(Vote vote : allVotes()){
            for(String party : vote.getSPAV(null).keySet()){
                firstStar.putIfAbsent(party, 0);
                firstStar.put(party, firstStar.get(party) + vote.getSPAV(null).get(party).intValue());
            }
        }
        return firstStar;
    }

    private Map<String, Double> secondSTAR() {
        Map<String, Integer> aux = natFptp.getMap();                // TODO: ver una forma mejor
        Map<String, Double> secondStar = new HashMap<>();
        Map<String, Integer> points = new HashMap<>();

        String winner1 = Collections.max(aux.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        aux.put(winner1, -1);
        String winner2 = Collections.max(aux.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();

        // winner 1 -> %
        // winner 2 -> %
        String winnerAlpha = winner1.compareTo(winner2)<0 ? winner1:winner2;
        for(Vote vote : allVotes()){
            if(vote.getSPAV(null).get(winner1)>vote.getSPAV(null).get(winner2)){    // si en el voto w1 > w2
                points.putIfAbsent(winner1, 0);                               // le sumo uno a w1 en el map
                points.put(winner1, points.get(winner1)+1);
            }
            else{
                if(vote.getSPAV(null).get(winner1)<vote.getSPAV(null).get(winner2)){
                    points.putIfAbsent(winner2, 0);
                    points.put(winner2, points.get(winner2)+1);
                }
                else{
                    points.putIfAbsent(winnerAlpha, 0);                       // si son iguales se lo sumo
                    points.put(winnerAlpha, points.get(winnerAlpha)+1);       // al menor alfabeticamente
                }
            }
        }
        int total = points.get(winner1) + points.get(winner2);
        secondStar.put(winner1, points.get(winner1).doubleValue() / total * 100);
        secondStar.put(winner2, 100 - points.get(winner1).doubleValue());

        return secondStar;
    }
}

  /*  for(Map<Integer, List<Vote>> vote : tabledVotes){
        for(List<Vote> list : vote.values())
            totalVotes.addAll(list);
    }*/