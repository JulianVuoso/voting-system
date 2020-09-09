package ar.edu.itba.pod.tpe.server.deprecated;

import ar.edu.itba.pod.tpe.interfaces.InspectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class InspectionServer {
    private static Logger logger = LoggerFactory.getLogger(InspectionServer.class);

    public static void main(String[] args) throws RemoteException {
        logger.info("tpe1-g6 InspectionServer Starting ...");
        final InspectionService is = new InspectionServiceImpl();
        final Remote remote = UnicastRemoteObject.exportObject(is, 0);
        final Registry registry = LocateRegistry.getRegistry();
        registry.rebind("inspection-service", remote);
        System.out.println("Inspection Service bound");
    }
}
