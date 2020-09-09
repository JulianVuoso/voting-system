package ar.edu.itba.pod.tpe.server.deprecated;

import ar.edu.itba.pod.tpe.exceptions.AdministrationException;
import ar.edu.itba.pod.tpe.interfaces.AdministrationService;
import ar.edu.itba.pod.tpe.models.Status;

import java.rmi.RemoteException;

public class AdministrationServiceImpl implements AdministrationService {

    private Status status;

    public AdministrationServiceImpl() {
        status = Status.UNDEFINED;
    }

    @Override
    public Status open() throws RemoteException, AdministrationException {
        switch (status) {
            case CLOSE: throw new AdministrationException("the poll is already closed");
            case OPEN: throw new AdministrationException("the poll is already open");
            default: status = Status.OPEN;
        }
        return Status.STARTED;
    }

    @Override
    public Status close() throws RemoteException, AdministrationException {
        switch (status) {
            case UNDEFINED: throw new AdministrationException("the poll has not been opened yet");
            case CLOSE: throw new AdministrationException("the poll is already close");
            default: status = Status.CLOSE;
        }
        return Status.ENDED;
    }

    @Override
    public Status status() throws RemoteException {
        return status;
    }
}
