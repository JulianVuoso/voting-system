package ar.edu.itba.pod.tpe.interfaces;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.models.Status;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ManagementService extends Remote {

    /**
     * Opens elections, enables for voting and more.
     * @throws IllegalElectionStateException When the poll is closed.
     * @throws RemoteException
     */
    Status open() throws RemoteException, IllegalElectionStateException;

    /**
     * Close elections, triggers the definitive count of votes.
     * @throws IllegalElectionStateException When the poll was not opened.
     * @throws RemoteException
     */
    Status close() throws RemoteException, IllegalElectionStateException;

    /**
     * Returns the status of the poll, not started, started, and finished.
     * @throws RemoteException
     */
    Status status() throws RemoteException;
}
