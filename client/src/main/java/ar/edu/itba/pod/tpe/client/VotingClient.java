package ar.edu.itba.pod.tpe.client;


import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.interfaces.VotingService;
import ar.edu.itba.pod.tpe.client.exceptions.ArgumentException;
import ar.edu.itba.pod.tpe.client.utils.ClientUtils;
import ar.edu.itba.pod.tpe.models.Vote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class VotingClient {

    private static Logger logger = LoggerFactory.getLogger(VotingClient.class);

    /**
     * Params values and argument error
     */
    private static final String SERVER_ADDRESS_PARAM = "serverAddress";
    private static final String FILE_PATH_PARAM = "votesPath";
    private static final int ERROR_STATUS = 1;

    /**
     * Properties brought from parameters
     */
    private static InetSocketAddress serverAddress;
    private static String path;


    public static void main(String[] args) {
        try {
            argumentParsing();
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
            return;
        }

        try {
            final Registry registry = LocateRegistry.getRegistry(serverAddress.getHostName(), serverAddress.getPort());
            final VotingService service = (VotingService) registry.lookup(VotingService.class.getName());

            List<String> file = Files.readAllLines(Paths.get(path) );
            parseFile(file, service);
        } catch (IllegalElectionStateException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
        } catch (RemoteException e) {
            System.err.println("Remote communication failed.");
            System.exit(ERROR_STATUS);
        } catch (NotBoundException e) {
            System.err.println("Server " + VotingService.class.getName() + " has no associated binding.");
            System.exit(ERROR_STATUS);
        } catch (IOException e ) {
            System.err.println("Error opening the file " + path);
            System.exit(ERROR_STATUS);
        }

    }


    /**
     * Ex.
     * 1000;JUNGLE;TIGER|3,LEOPARD|2,LYNX|1;TIGER
     * 1001;JUNGLE;LYNX|1,TIGER|1,LEOPARD|2;LYNX
     * 1002;SAVANNAH;TIGER|3,LYNX|3,OWL|3,BUFFALO|5;BUFFALO
     *
     * Parses the given file and emits a vote
     * @param file The file to be parsed.
     * @param service Voting services to emmit a new vote.
     * @throws
     **/
    private static void parseFile(List<String> file, VotingService service) throws RemoteException, IllegalElectionStateException {
        for(String line : file ) {
            String[] parse = line.split(";");
            Map<String, Integer> votes = parseVotes(parse[2]);
            service.vote(new Vote(Integer.valueOf(parse[0]), parse[1], votes, parse[3]));
        }

        System.out.println("\n" + file.size() + " votes registered");
    }


    /**
     * Ex.
     * LYNX|1,TIGER|1,LEOPARD|2
     *
     * Parses a line to create a Vote.
     * @param voteLine The vote line to be parsed.
     * @return A map with the parties voted and their rating.
     */
    private static Map<String, Integer> parseVotes(String voteLine) {
        Map<String, Integer> votes = new HashMap<>();
        for (String s : voteLine.split(",")) {
            String[] v = s.split("\\|");
            votes.put(v[0],Integer.valueOf(v[1]));
        }
        return votes;
    }


    /**
     * Ex.
     * -DserverAddress=xx.xx.xx.xx:yyyy        --> host:port
     * -DvotesPath=fileName                    --> file.csve
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

        path = Optional.ofNullable(properties.getProperty(FILE_PATH_PARAM)).orElseThrow(new ArgumentException("Path must be supplied using -DvotesPath"));
    }
}
