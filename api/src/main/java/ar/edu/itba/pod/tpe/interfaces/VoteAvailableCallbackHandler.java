package ar.edu.itba.pod.tpe.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VoteAvailableCallbackHandler extends Remote {
    /**
     * Method to be called when a vote is registered if appropriate
     * @throws RemoteException if any communication error occurs.
     */
    void voteRegistered() throws RemoteException;

    /**
     * Method to be called when the election finishes
     * @throws RemoteException if any communication error occurs.
     */
    void electionFinished() throws RemoteException;
}
