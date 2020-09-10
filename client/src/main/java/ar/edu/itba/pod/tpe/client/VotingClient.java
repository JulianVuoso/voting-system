package ar.edu.itba.pod.tpe.client;


import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.stub.Vote;
import ar.edu.itba.pod.tpe.interfaces.VotingService;
import ar.edu.itba.pod.tpe.client.exceptions.ArgumentException;
import ar.edu.itba.pod.tpe.client.utils.ClientUtils;
import org.apache.commons.cli.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class VotingClient {
    private static Logger logger = LoggerFactory.getLogger(VotingClient.class);
    private static final int ERROR_STATUS = 1;

    private static final String SERVER_ADDRESS_PARAM = "serverAddress";
    private static final String FILE_PATH_PARAM = "votesPath";

    private static final String VOTING_SERVICE_NAME = "service";

    private static InetSocketAddress serverAddress;
    private static String path;


    public static void main(String[] args) throws RemoteException, NotBoundException, IllegalElectionStateException {
        logger.info("tpe1-g6 Voting Client Starting ...");

        try {
            argumentParsing(args);
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
            return;
        }

        final Registry registry = LocateRegistry.getRegistry(serverAddress.getHostName(), serverAddress.getPort());
        final VotingService service = (VotingService) registry.lookup(VOTING_SERVICE_NAME);

        try {
            List<String> file = Files.readAllLines(Paths.get(path) );
            parseFile(file, service);
        }
        catch (IOException e ){
            e.printStackTrace();
        }

    }

    /**
     *   1000;JUNGLE;TIGER|3,LEOPARD|2,LYNX|1;TIGER
     *   1001;JUNGLE;LYNX|1,TIGER|1,LEOPARD|2;LYNX
     *   1002;SAVANNAH;TIGER|3,LYNX|3,OWL|3,BUFFALO|5;BUFFALO
     **/
    private static void parseFile(List<String> file, VotingService service) throws RemoteException, IllegalElectionStateException {
        for(String line : file ){
            String[] parse = line.split(";");
            Map<String, Integer> votes = parseVotes(parse[2]);
            service.vote(new Vote(Integer.valueOf(parse[0]), parse[1], votes, parse[3]));
        }

        System.out.println("\n" + file.size() + " votes registered");;
    }


    /**
     * Aca recibo algo como:
     *              LYNX|1,TIGER|1,LEOPARD|2
     */
    private static Map<String, Integer> parseVotes(String voteLine) {
        Map<String, Integer> votes = new HashMap<>();
        for (String s : voteLine.split(",")) {
            String[] v = s.split("\\|");
            votes.put(v[0],Integer.valueOf(v[1]));
        }
        return votes;
    }


    private static void argumentParsing(String[] args) throws ArgumentException {
        // -DserverAddress=xx.xx.xx.xx:yyyy        --> host:port
        // -DvotesPath=fileName                    --> file.csv

        Properties properties;
        try {
            properties = ClientUtils.getDProperties(args);
        } catch (ParseException e) {
            throw new ArgumentException("Params format must be -Dproperty=value");
        }

        try {
            serverAddress = ClientUtils.getInetAddress(properties.getProperty(SERVER_ADDRESS_PARAM));
        } catch (URISyntaxException e) {
            throw new ArgumentException("Server Address must be supplied using -DserverAddress and its format must be xx.xx.xx.xx:yyyy");
        }

        path = properties.getProperty(FILE_PATH_PARAM);
        if (path == null) {
            throw new ArgumentException("Path must be supplied using -DvotesPath");
        }
    }



}
