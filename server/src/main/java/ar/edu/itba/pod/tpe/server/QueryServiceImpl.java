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
    Map<String, SPAV> stateSpav;

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
        if(status==1)                           //  open
            return natFptp;
        return null;
    }

    @Override
    public Result askState(String state) throws RemoteException, QueryException {
        if(status==0) throw new QueryException("Polls already closed");
        if(status==1)                           //  open
            return stateFptp.get(state);

        //finished
        return null;
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
        stateFptp.get(vote.getState()).getMap().put(vote.getVoteFPTP(), stateFptp.get(vote.getState()).getMap().get(vote.getVoteFPTP()+1)); // Luego obtengo ese FPTP y le meto en key Party 1 voto mas

        tableFptp.putIfAbsent(vote.getTable(), new FPTP());                                     // TABLE: same a state
        tableFptp.get(vote.getTable()).getMap().put(vote.getVoteFPTP(), tableFptp.get(vote.getTable()).getMap().get(vote.getVoteFPTP()+1));
    }

}

/*
Map<String, Integer> getFinalNational();
Map<String, Double> getFinalState(String state);
Map<String, Double> getTable(Integer table);
Map<String, Double> getPartialNational();
Map<String, Double> getPartialState(String state);
* */


  /*  for(Map<Integer, List<Vote>> vote : tabledVotes){
        for(List<Vote> list : vote.values())
            totalVotes.addAll(list);
    }*/




 /*   private Result finalTable(Integer table) throws RemoteException, QueryException{
        Map<String, Double> fptp2 = new HashMap<>();
        Map<String, Integer> fptpVotes = new HashMap<>();
        List<Vote> totalVotes = new ArrayList<>();
        int total = 0;

        for(Map<Integer, List<Vote>> vote : votes.values())
            totalVotes.addAll(vote.get(table));

        for(Vote vote : totalVotes){
            fptpVotes.putIfAbsent(vote.getVoteFPTP(),0);
            fptpVotes.put(vote.getVoteFPTP(), fptpVotes.get(vote.getVoteFPTP())+1);
            total++;
        }

        for(String party : fptpVotes.keySet()){
            fptp2.put(party, fptpVotes.get(party).doubleValue() / total * 100);
        }

        return new FPTP(fptp2, true, Type.FPTP);
    }

    private Map<String, Integer> partialFtpt(Integer table){

    }*/