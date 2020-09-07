package ar.edu.itba.pod.tpe.interfaces;

import ar.edu.itba.pod.tpe.exceptions.AdministrationException;
import ar.edu.itba.pod.tpe.models.Status;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AdministrationService extends Remote {

    /**
     * Opens elections, enables for voting and more.
     * @throws AdministrationException When the poll is closed.
     * @throws RemoteException
     */
    void open() throws RemoteException, AdministrationException;

    /**
     * Close elections, triggers the definitive count of votes.
     * @throws AdministrationException When the poll was not opened.
     * @throws RemoteException
     */
    void close() throws RemoteException, AdministrationException;

    /**
     * Returns the status of the poll, not started, started, and finished.
     * @throws RemoteException
     */
    Status status() throws RemoteException;
}
