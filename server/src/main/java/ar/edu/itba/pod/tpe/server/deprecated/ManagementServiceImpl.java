package ar.edu.itba.pod.tpe.server.deprecated;

import ar.edu.itba.pod.tpe.exceptions.ManagementException;
import ar.edu.itba.pod.tpe.interfaces.ManagementService;
import ar.edu.itba.pod.tpe.models.Status;

import java.rmi.RemoteException;

public class ManagementServiceImpl implements ManagementService {

    private Status status;

    public ManagementServiceImpl() {
        status = Status.UNDEFINED;
    }

    @Override
    public Status open() throws RemoteException, ManagementException {
        switch (status) {
            case CLOSE: throw new ManagementException("the poll is already closed");
            case OPEN: throw new ManagementException("the poll is already open");
            default: status = Status.OPEN;
        }
        return Status.STARTED;
    }

    @Override
    public Status close() throws RemoteException, ManagementException {
        switch (status) {
            case UNDEFINED: throw new ManagementException("the poll has not been opened yet");
            case CLOSE: throw new ManagementException("the poll is already close");
            default: status = Status.CLOSE;
        }
        return Status.ENDED;
    }

    @Override
    public Status status() throws RemoteException {
        return status;
    }
}
