package ar.edu.itba.pod.tpe.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server {
    private static Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("tpe1-g6 Server Starting ...");

        final Remote remote = UnicastRemoteObject.exportObject(new ElectionServiceImpl(), 0);

        final Registry registry = LocateRegistry.getRegistry();
        // TODO: RENAME IN CLIENT AND REMOVE service FROM HERE
        registry.rebind("service", remote);
        registry.rebind("inspection-service", remote);
        registry.rebind("management-service", remote);
        registry.rebind("voting-service", remote);
        registry.rebind("query-service", remote);

        logger.info("Election Service bound");
    }
}
