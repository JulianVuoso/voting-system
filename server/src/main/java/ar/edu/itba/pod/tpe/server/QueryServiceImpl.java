package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.interfaces.QueryService;
import ar.edu.itba.pod.tpe.models.FPTP;
import ar.edu.itba.pod.tpe.models.Result;
import ar.edu.itba.pod.tpe.models.Type;
import ar.edu.itba.pod.tpe.models.Vote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

public class QueryServiceImpl implements QueryService {

    int status;
    Map<String, Map<Integer, List<Vote>>> votes;

    public QueryServiceImpl() {
        this.status = 0;
        this.votes = new HashMap<>();
    }

    @Override
    public Result askNational() throws RemoteException, QueryException {
        if(status==0) throw new QueryException("Polls already closed");
        return null;
    }

    @Override
    public Result askState(String state) throws RemoteException, QueryException {
        if(status==0) throw new QueryException("Polls already closed");
        return null;
    }

    Map<String, Integer> livefptpVotes = new HashMap<>();
    FPTP liveFptp;

    @Override
    public Result askTable(Integer table) throws RemoteException, QueryException {
        if(status==0) throw new QueryException("Polls already closed");

        if(status==1) {    //      open
            return new FPTP(livefptpVotes, true, Type.FPTP);
        }
        // finished
        return new FPTP(livefptpVotes, false, Type.FPTP);
    }

    @Override
    public void vote(Vote vote) {
        livefptpVotes.putIfAbsent(vote.getVoteFPTP(),0);
        livefptpVotes.put(vote.getVoteFPTP(), livefptpVotes.get(vote.getVoteFPTP())+1);
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