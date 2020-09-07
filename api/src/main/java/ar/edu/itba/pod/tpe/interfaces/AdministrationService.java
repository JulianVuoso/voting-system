package ar.edu.itba.pod.tpe.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AdministrationService extends Remote {

    /**
     * Opens elections, enables for voting and more.
     * TODO: throws exception when already closed
     * @throws RemoteException
     */
    void open() throws RemoteException;

    /**
     * Close elections, triggers the definitive count of votes.
     * TODO: throws exception when not opened
     * @throws RemoteException
     */
    void close() throws RemoteException;

    String status() throws RemoteException;
}
