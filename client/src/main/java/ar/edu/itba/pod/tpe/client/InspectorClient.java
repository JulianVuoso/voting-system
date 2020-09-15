package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.client.exceptions.ArgumentException;
import ar.edu.itba.pod.tpe.client.utils.ClientUtils;
import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.interfaces.InspectionService;
import ar.edu.itba.pod.tpe.interfaces.VoteAvailableCallbackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Optional;
import java.util.Properties;

public class InspectorClient {
    private static Logger logger = LoggerFactory.getLogger(InspectorClient.class);

    /**
     * Params values and argument error
     */
    private static final String SERVER_ADDRESS_PARAM = "serverAddress";
    private static final String ID_PARAM = "id";
    private static final String PARTY_PARAM = "party";
    private static final int ERROR_STATUS = 1;

    /**
     * Properties brought from parameters
     */
    private static InetSocketAddress serverAddress;
    private static int tableNumber;
    private static String partyName;


    public static void main(String[] args) {
        logger.info("tpe1-g6 InspectorClient Starting ...");

        try {
            argumentParsing();
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
            return;
        }

        logger.debug("Args: " + serverAddress.getHostName() + " - " + serverAddress.getPort() + " - " + tableNumber + " - " + partyName);

        try {
            clientInspect();
        } catch (RemoteException e) {
            System.err.println("Remote communication failed.");
            System.exit(ERROR_STATUS);
        } catch (NotBoundException e) {
            System.err.println("Server " + InspectionService.class.getName() + " has no associated binding.");
            System.exit(ERROR_STATUS);
        }
    }


    /**
     * Adds the inspector to the corresponding state and table
     * @throws RemoteException
     * @throws NotBoundException
     */
    private static void clientInspect() throws RemoteException, NotBoundException {
        final Registry registry = LocateRegistry.getRegistry(serverAddress.getHostName(), serverAddress.getPort());
        final InspectionService service = (InspectionService) registry.lookup(InspectionService.class.getName());

        VoteAvailableCallbackHandler handler = new VoteAvailableCallbackHandlerImpl(tableNumber, partyName);
        UnicastRemoteObject.exportObject(handler, 0);

        try {
            service.inspect(tableNumber, partyName, handler);
            System.out.println("Fiscal of " + partyName + " registered on polling place " + tableNumber);
        } catch (IllegalElectionStateException e) {
            System.err.println(e.getMessage());
            UnicastRemoteObject.unexportObject(handler, true);
        }
    }


    /**
     * Ex.
     * -DserverAddress=xx.xx.xx.xx:yyyy    --> host:port
     * -Did=​ pollingPlaceNumber            --> tableNumber
     * -Dparty=​ partyName                  --> partyName
     *
     * Parses arguments from terminal
     * @throws ArgumentException
     */
    private static void argumentParsing() throws ArgumentException {
        Properties properties = System.getProperties();

        try {
            serverAddress = ClientUtils.getInetAddress(properties.getProperty(SERVER_ADDRESS_PARAM));
        } catch (URISyntaxException e) {
            throw new ArgumentException("Server Address must be supplied using -DserverAddress and its format must be xx.xx.xx.xx:yyyy");
        }

        try {
            tableNumber = Integer.parseInt(properties.getProperty(ID_PARAM));
        } catch (NumberFormatException e) {
            throw new ArgumentException("Table ID must be supplied using -Did and it must be a number");
        }

        partyName = Optional.ofNullable(properties.getProperty(PARTY_PARAM)).orElseThrow(new ArgumentException("Party name must be supplied using -Daction"));
    }
}
