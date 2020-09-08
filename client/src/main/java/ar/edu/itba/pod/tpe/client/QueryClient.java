package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.interfaces.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class QueryClient {
    private static Logger logger = LoggerFactory.getLogger(QueryClient.class);

    public static void main(String[] args) throws RemoteException, NotBoundException {
        logger.info("tpe1-g6 QueryClient Starting ...");

        final Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        final QueryService service = (QueryService) registry.lookup("inspection-service");

        try {

        } catch (QueryException e) {

        }
    }
}
