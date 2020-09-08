package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.VotingService;
import ar.edu.itba.pod.tpe.Vote;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class VotingServiceImpl implements VotingService {

    private List<Vote> votes = new ArrayList<>();

    @Override
    public void vote(Vote vote) throws RemoteException {

    }
}
