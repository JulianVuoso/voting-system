package ar.edu.itba.pod.tpe.interfaces;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InspectionService extends Remote {
    /**
     * Register inspector for a certain party and a table number
     * @param table table number
     * @param party party name to inspect
     * @param handler handler to call when a vote is registered
     * @throws RemoteException if any communication error occurs.
     */
    void inspect(int table, String party, VoteAvailableCallbackHandler handler) throws RemoteException, IllegalElectionStateException;
}
