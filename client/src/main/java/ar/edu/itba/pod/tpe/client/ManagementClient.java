package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.client.exceptions.ArgumentException;
import ar.edu.itba.pod.tpe.client.utils.ClientUtils;
import ar.edu.itba.pod.tpe.exceptions.ManagementException;
import ar.edu.itba.pod.tpe.interfaces.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;
import java.util.Properties;

public class ManagementClient {

    private static Logger logger = LoggerFactory.getLogger(ManagementClient.class);

    private static final int ERROR_STATUS = 1;

    private static final String SERVER_ADDRESS_PARAM = "serverAddress";
    private static final String ACTION_PARAM = "action";
    private static String SERVICE_NAME = "service";

    private static InetSocketAddress serverAddress;
    private static String action;

    public static void main(String[] args) throws Exception {
        logger.info("tpe1-g6 Administration Client Starting ...");

        try {
            argumentParsing();
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
            return;
        }

        logger.debug("Args: " + serverAddress.getHostName() + " - " + serverAddress.getPort() + " - " + action);

        final Registry registry = LocateRegistry.getRegistry(serverAddress.getHostName(), serverAddress.getPort());
        final ManagementService service = (ManagementService) registry.lookup(SERVICE_NAME);


        try {
            switch (action) {
                case "open":
                    System.out.println("Election " + service.open().toString().toLowerCase());
                    break;
                case "close":
                    System.out.println("Election " + service.close().toString().toLowerCase());
                    break;
                case "state":
                    System.out.println("Election is " + service.status().toString().toLowerCase());
                    break;
                default:
                    System.err.println("Invalid action name, possible options: open, close, state");
            }
        } catch (ManagementException e) {
            System.err.println("Error trying to " + action + ", " + e.getMessage());
        }
    }

    private static void argumentParsing() throws ArgumentException {
        // -DserverAddress=xx.xx.xx.xx:yyyy     --> host:port
        // -Daction=action                      --> action

        Properties properties = System.getProperties();

        try {
            serverAddress = ClientUtils.getInetAddress(properties.getProperty(SERVER_ADDRESS_PARAM));
        } catch (URISyntaxException e) {
            throw new ArgumentException("Server Address must be supplied using -DserverAddress and its format must be xx.xx.xx.xx:yyyy");
        }

        action = Optional.ofNullable(properties.getProperty(ACTION_PARAM)).orElseThrow(new ArgumentException("Party Name must be supplied using -Daction"));
    }
}
