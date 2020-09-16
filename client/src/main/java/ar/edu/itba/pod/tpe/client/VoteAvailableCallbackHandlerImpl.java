package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.interfaces.VoteAvailableCallbackHandler;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class VoteAvailableCallbackHandlerImpl implements VoteAvailableCallbackHandler {
    private int tableNumber;
    private String partyName;

    public VoteAvailableCallbackHandlerImpl(int tableNumber, String partyName) {
        this.tableNumber = tableNumber;
        this.partyName = partyName;
    }

    @Override
    public void voteRegistered() throws RemoteException {
        System.out.println("New vote for " + partyName + " on polling place " + tableNumber);
    }

    @Override
    public void electionFinished() throws RemoteException {
//        System.out.println("Election finished, exiting...");
        UnicastRemoteObject.unexportObject(this, true);
    }
}
