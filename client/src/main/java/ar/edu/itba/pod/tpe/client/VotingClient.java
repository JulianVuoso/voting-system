package ar.edu.itba.pod.tpe.client;


import ar.edu.itba.pod.tpe.Vote;
import ar.edu.itba.pod.tpe.VotingService;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VotingClient {
    private static Logger logger = LoggerFactory.getLogger(VotingClient.class);


    public static void main(String[] args) throws RemoteException, NotBoundException {
       /* TODO: Cuando juntemos todo en un solo service
                chequear si ya estan abiertos los comicios => sino ERROR */
        logger.info("tpe1-g6 Voting Client Starting ...");

        /* TODO: recibir IP - puerto - csv por linea de comandos */
        final Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        final VotingService service = (VotingService) registry.lookup("voting-service");

        try {
            List<String> file = Files.readAllLines(Paths.get("/Users/nicolas/Downloads/test.csv") );
            parseFile(file, service);
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     *   1000;JUNGLE;TIGER|3,LEOPARD|2,LYNX|1;TIGER
     *   1001;JUNGLE;LYNX|1,TIGER|1,LEOPARD|2;LYNX
     *   1002;SAVANNAH;TIGER|3,LYNX|3,OWL|3,BUFFALO|5;BUFFALO
     **/
    private static void parseFile(List<String> file, VotingService service) throws RemoteException{
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



}
