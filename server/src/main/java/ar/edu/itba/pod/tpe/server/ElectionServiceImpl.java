package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.AdministrationException;
import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.interfaces.AdministrationService;
import ar.edu.itba.pod.tpe.interfaces.InspectionService;
import ar.edu.itba.pod.tpe.interfaces.VoteAvailableCallbackHandler;
import ar.edu.itba.pod.tpe.models.Status;
import ar.edu.itba.pod.tpe.server.utils.Pair;
import ar.edu.itba.pod.tpe.stub.InspectionVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElectionServiceImpl implements AdministrationService, InspectionService {
    private static Logger logger = LoggerFactory.getLogger(ElectionServiceImpl.class);


    private Status status;

    private Map<Pair<String, Integer>, List<VoteAvailableCallbackHandler>> inspectorHandlers = new HashMap<>();

    // TODO: DEFINIR TIPO DE THREAD POOL Y SI TIENE LIMITE DE CANTIDAD
    private ExecutorService executorService = Executors.newCachedThreadPool();

    public ElectionServiceImpl() {
        status = Status.UNDEFINED;
    }

    @Override
    public Status open() throws RemoteException, AdministrationException {
        switch (status) {
            case CLOSE: throw new AdministrationException("the poll is already closed");
            case OPEN: throw new AdministrationException("the poll is already open");
            default: status = Status.OPEN;
        }
        logger.info("Election started");
        return Status.STARTED;
    }

    @Override
    public Status close() throws RemoteException, AdministrationException {
        switch (status) {
            case UNDEFINED: throw new AdministrationException("the poll has not been opened yet");
            case CLOSE: throw new AdministrationException("the poll is already close");
            default: status = Status.CLOSE;
        }

        // Kill all handlers
        inspectorHandlers.values().forEach(handlerList -> handlerList.forEach(this::sendElectionFinishedToInspector));
        inspectorHandlers.clear();
        logger.info("Election finished and all handlers killed");
        return Status.ENDED;
    }

    @Override
    public Status status() throws RemoteException {
        return status;
    }

    @Override
    public void vote(InspectionVote vote) throws RemoteException {
        if (status != Status.OPEN) status = Status.OPEN;

        final Pair<String, Integer> inspectLocation = new Pair<>(vote.getFptpVote(), vote.getTableNumber());
        Optional.ofNullable(inspectorHandlers.get(inspectLocation))
                .ifPresent(handlerList -> handlerList.forEach(this::sendNotificationToInspector));

        // REGISTER VOTE
        System.out.println("Vote registered: " + vote);
    }

    // TODO: VER SI TIENE QUE SER SYNC CON ALGO
    @Override
    public void inspect(int table, String party, VoteAvailableCallbackHandler handler) throws RemoteException, IllegalElectionStateException {
        if (status != Status.UNDEFINED) {
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
