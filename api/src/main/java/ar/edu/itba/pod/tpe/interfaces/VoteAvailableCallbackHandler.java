package ar.edu.itba.pod.tpe.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VoteAvailableCallbackHandler extends Remote {
    /**
     * Method to be called when a vote is registered if appropriate
     * @throws RemoteException if any communication error occurs.
     */
    // TODO: Este metodo recibe el Vote? Como que capaz tiene info de mas
    void voteRegistered() throws RemoteException;

    void electionFinished() throws RemoteException;
}
