package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.interfaces.InspectionService;
import ar.edu.itba.pod.tpe.interfaces.ManagementService;
import ar.edu.itba.pod.tpe.interfaces.QueryService;
import ar.edu.itba.pod.tpe.interfaces.VotingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private static final int ERROR_STATUS = 1;

    public static void main(String[] args) {
        try {
            final Remote remote = UnicastRemoteObject.exportObject(new ElectionServiceImpl(), 0);

            final Registry registry = LocateRegistry.getRegistry();
            registry.rebind(InspectionService.class.getName(), remote);
            registry.rebind(ManagementService.class.getName(), remote);
            registry.rebind(VotingService.class.getName(), remote);
            registry.rebind(QueryService.class.getName(), remote);
        } catch (RemoteException e) {
            System.err.println("Remote communication with registry failed.");
            System.exit(ERROR_STATUS);
            return;
        }

        System.out.println("Election Service bound");
    }
}
