package ar.edu.itba.pod.tpe.interfaces;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.models.Vote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VotingService extends Remote {

    void vote ( Vote vote ) throws RemoteException, IllegalElectionStateException;

}
