package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.interfaces.InspectionService;
import ar.edu.itba.pod.tpe.interfaces.VoteAvailableCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class InspectorClient {
    private static Logger logger = LoggerFactory.getLogger(InspectorClient.class);

    public static void main(String[] args) throws RemoteException, NotBoundException {
        logger.info("tpe1-g6 InspectorClient Starting ...");

        // TODO> AGREGAR MANEJO DE ARGS PARA TOMAR LO SIGUIENTE
        // -DserverAddress=xx.xx.xx.xx:yyyy    --> host:port
        // -Did=​ pollingPlaceNumber            --> tableNumber
        // -Dparty=​ partyName                  --> partyName

        final Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        final InspectionService service = (InspectionService) registry.lookup("inspection-service");

        final int tableNumber = 1001;
        final String partyName = "TIGER";
        VoteAvailableCallbackHandler handler = new VoteAvailableCallbackHandlerImpl(tableNumber, partyName);
        UnicastRemoteObject.exportObject(handler, 0);

        try {
            service.inspect(tableNumber, partyName, handler);
            System.out.println("Fiscal of " + partyName + " registered on polling place " + tableNumber);
        } catch (IllegalElectionStateException e) {
            System.out.println(e.getMessage());
            UnicastRemoteObject.unexportObject(handler, true);
        }
    }
}
