package ar.edu.itba.pod.tpe.server.deprecated;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.interfaces.InspectionService;
import ar.edu.itba.pod.tpe.interfaces.VoteAvailableCallbackHandler;
import ar.edu.itba.pod.tpe.server.utils.Pair;
import ar.edu.itba.pod.tpe.stub.InspectionVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InspectionServiceImpl implements InspectionService {
    private static Logger logger = LoggerFactory.getLogger(InspectionServiceImpl.class);

    private boolean started; // Puede ser un Enum Status

    private Map<Pair<String, Integer>, List<VoteAvailableCallbackHandler>> inspectorHandlers = new HashMap<>();

    // TODO: DEFINIR TIPO DE THREAD POOL Y SI TIENE LIMITE DE CANTIDAD
    private ExecutorService executorService = Executors.newCachedThreadPool();

//    @Override
    public void vote(InspectionVote vote) throws RemoteException {
        if (!started) started = true;

        final Pair<String, Integer> inspectLocation = new Pair<>(vote.getFptpVote(), vote.getTableNumber());
        Optional.ofNullable(inspectorHandlers.get(inspectLocation))
                .ifPresent(handlerList -> handlerList.forEach(this::sendNotificationToInspector));

        // REGISTER VOTE
        System.out.println("Vote registered: " + vote);
    }

//    @Override
    public void finishElection() throws RemoteException {
        started = false;
        System.out.println("Election finished");
        inspectorHandlers.values().forEach(handlerList -> handlerList.forEach(this::sendElectionFinishedToInspector));
        inspectorHandlers.clear();
    }

    // TODO: VER SI TIENE QUE SER SYNC CON ALGO
    @Override
    public void inspect(int table, String party, VoteAvailableCallbackHandler handler) throws RemoteException, IllegalElectionStateException {
        if (started) {
            throw new IllegalElectionStateException("Solo se puede registrar un fiscal antes de que comience la elección");
        }
        final Pair<String, Integer> keyPair = new Pair<>(party, table);
        inspectorHandlers.computeIfAbsent(keyPair, k -> new ArrayList<>()); // If keyPair not present, put(keyPair, new ArrayList()
        inspectorHandlers.get(keyPair).add(handler);
    }

    private void sendNotificationToInspector(final VoteAvailableCallbackHandler handler) {
        executorService.submit(() -> {
            try {
                handler.voteRegistered();
            } catch (RemoteException e) {
                logger.error("Could not send notification to Inspector");
                // FIXME: DEBERIA DESREGISTRARLO?? --> Preguntar
            }
        });
    }

    private void sendElectionFinishedToInspector(final VoteAvailableCallbackHandler handler) {
        executorService.submit(() -> {
            try {
                handler.electionFinished();
            } catch (RemoteException e) {
                // Do nothing
            }
        });
    }
}
