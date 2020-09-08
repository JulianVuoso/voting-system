package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.interfaces.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class QueryServer {
    private static Logger logger = LoggerFactory.getLogger(QueryServer.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("tpe1-g6 QueryServer Starting ...");
        final QueryService qs = new QueryServiceImpl();
        final Remote remote = UnicastRemoteObject.exportObject(qs, 0);
        final Registry registry = LocateRegistry.getRegistry();
        registry.rebind("inspection-service", remote);
        System.out.println("Inspection Service bound");
    }
}
