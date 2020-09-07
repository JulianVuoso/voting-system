package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.AdministrationException;
import ar.edu.itba.pod.tpe.interfaces.AdministrationService;
import ar.edu.itba.pod.tpe.models.PollStatus;

import java.rmi.RemoteException;

public class AdministrationServiceImpl implements AdministrationService {

    private PollStatus pollStatus;

    public AdministrationServiceImpl() {
        pollStatus = PollStatus.UNDEFINED;
    }

    @Override
    public void open() throws RemoteException, AdministrationException {
        switch (pollStatus) {
            case CLOSE: throw new AdministrationException("The poll is already closed");
            default: pollStatus = PollStatus.OPEN;
        }
    }

    @Override
    public void close() throws RemoteException, AdministrationException {
        switch (pollStatus) {
            case UNDEFINED: throw new AdministrationException("The poll has not been opened yet");
            default: pollStatus = PollStatus.CLOSE;
        }
    }

    @Override
    public PollStatus status() throws RemoteException {
        return pollStatus;
    }
}
