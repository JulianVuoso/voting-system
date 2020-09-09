package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.VotingService;
import ar.edu.itba.pod.tpe.Vote;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VotingServiceImpl implements VotingService {

    /* Map<String = state, Map<Integer = table, List<Votes>>> */
    private Map<String, Map<Integer, List<Vote>>> votes = new HashMap<>();
    private final Object voteLock = "voteLock";

    @Override
    public void vote(Vote vote) throws RemoteException {
        /* TODO: Check Syncro */
        String state = vote.getState();
        Integer table = vote.getTable();
        synchronized (voteLock){
            if(!votes.containsKey(state)){
                votes.put(state,new HashMap<>());
            }
            if(!votes.get(state).containsKey(table)){
                votes.get(state).put(table,new ArrayList<>());
            }
            votes.get(state).get(table).add(vote);
        }
    }

}
