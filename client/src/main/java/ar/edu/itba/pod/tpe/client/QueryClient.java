package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.interfaces.QueryService;
import ar.edu.itba.pod.tpe.models.FPTP;
import ar.edu.itba.pod.tpe.models.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class QueryClient {
    private static Logger logger = LoggerFactory.getLogger(QueryClient.class);

    public static void main(String[] args) throws RemoteException, NotBoundException {
        logger.info("tpe1-g6 QueryClient Starting ...");

        final Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        final QueryService service = (QueryService) registry.lookup("inspection-service");

        try {
            FPTP fptp = (FPTP) service.askTable(5);
            Map<String, Double> fptpPerc = new HashMap<>();
            int total=0;
            for(Integer partyVotes : fptp.getVotes().values())
                total+=partyVotes;
            for(String party : fptp.getVotes().keySet())
                fptpPerc.put(party, fptp.getVotes().get(party).doubleValue() / total * 100);

        } catch (QueryException e) {

        }
    }
}
