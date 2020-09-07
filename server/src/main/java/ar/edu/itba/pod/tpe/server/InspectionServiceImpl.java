package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.interfaces.InspectionService;
import ar.edu.itba.pod.tpe.interfaces.VoteAvailableCallbackHandler;
import ar.edu.itba.pod.tpe.stub.InspectionVote;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class InspectionServiceImpl implements InspectionService {
    private static Logger logger = LoggerFactory.getLogger(InspectionServiceImpl.class);

    private boolean started; // Puede ser un Enum Status
    // TODO: Ver si puede haber mas de un inspector para una mesa y un partido
    private Map<Pair<String, Integer>, VoteAvailableCallbackHandler> inspectorHandlers = new HashMap<>();

    private ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public void vote(InspectionVote vote) throws RemoteException {
        if (!started) started = true;

        final Pair<String, Integer> inspectLocation = new Pair<>(vote.getFptpVote(), vote.getTableNumber());
        Optional.ofNullable(inspectorHandlers.get(inspectLocation)).ifPresent(this::sendNotificationToInspector);

        // REGISTER VOTE
        System.out.println("Vote registered: " + vote);
    }

    @Override
    public void finishElection() throws RemoteException {
        started = false;
        System.out.println("Election finished");
        inspectorHandlers.values().forEach(handler -> {
            try {
                handler.electionFinished();
            } catch (RemoteException e) {
                // Do nothing
            }
        });
        inspectorHandlers.clear();
    }

    // TODO: VER SI TIENE QUE SER SYNC CON ALGO
    @Override
    public void inspect(int table, String party, VoteAvailableCallbackHandler handler) throws RemoteException, IllegalElectionStateException {
        if (started) {
            throw new IllegalElectionStateException("Solo se puede registrar un fiscal antes de que comience la elección");
        }
        final Pair<String, Integer> keyPair = new Pair<>(party, table);
//        Si puede haber mas de uno, aca me traeria la lista o la crearia
//        inspectorHandlers.get(new Pair<>(party, table));
//        TODO: SINO, TIRO ERROR O LO PISO O QUE HACEMOS???
        inspectorHandlers.put(keyPair, handler);
    }

    private void sendNotificationToInspector(final VoteAvailableCallbackHandler handler) {
        executorService.submit(() -> {
            try {
                handler.voteRegistered();
            } catch (RemoteException e) {
                logger.error("Could not send notification to Inspector");
                // FIXME: DEBERIA DESREGISTRARLO??
            }
        });
    }
}
