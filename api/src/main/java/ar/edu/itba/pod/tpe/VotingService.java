package ar.edu.itba.pod.tpe;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VotingService extends Remote {


    void vote ( Vote vote ) throws RemoteException;

}
