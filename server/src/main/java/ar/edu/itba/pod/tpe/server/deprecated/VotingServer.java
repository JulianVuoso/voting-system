package ar.edu.itba.pod.tpe.server.deprecated;

import ar.edu.itba.pod.tpe.interfaces.VotingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class VotingServer {
    private static Logger logger = LoggerFactory.getLogger(VotingServer.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("tpe1-g6 VotingServer Starting ...");
        final VotingService is = new VotingServiceImpl();
        final Remote remote = UnicastRemoteObject.exportObject(is, 0);
        final Registry registry = LocateRegistry.getRegistry();
        registry.rebind("voting-service", remote);
        System.out.println("Voting Service bound");
    }

}
