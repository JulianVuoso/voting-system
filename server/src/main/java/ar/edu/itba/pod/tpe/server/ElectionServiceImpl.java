package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.ManagementException;
import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.interfaces.*;
import ar.edu.itba.pod.tpe.models.*;
import ar.edu.itba.pod.tpe.server.utils.Pair;
import ar.edu.itba.pod.tpe.server.utils.Votes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElectionServiceImpl implements ManagementService, InspectionService, VotingService, QueryService {

    private static Logger logger = LoggerFactory.getLogger(ElectionServiceImpl.class);

    private Status status;

    private Votes votes;

    private ExecutorService executorService;

    private Map<Pair<String, Integer>, List<VoteAvailableCallbackHandler>> inspectorHandlers;

    private final Object voteLock = "voteLock";

    // TODO: check if can merge with final results using Result (maybe moving logic inside FPTP)
    private FPTP national;
    private Map<String, FPTP> state;
    private Map<Integer, FPTP> table;

    private STAR natStar;
    private Map<String, SPAV> stateSPAV;

    public ElectionServiceImpl() {
        status = Status.REGISTRATION;
        inspectorHandlers = new HashMap<>();
        executorService = Executors.newFixedThreadPool(4);
        votes = new Votes();

        // Initialize partial results for national, state and table
        national = new FPTP();
        state = new HashMap<>();
        table = new HashMap<>();

        // Initialize final results for national and state
        natStar = null;
        stateSPAV = new HashMap<>();
    }


    /**
     * Management Service exposed methods.
     **/

    @Override
    public Status open() throws RemoteException, ManagementException {
        switch (status) {
            case CLOSE: throw new ManagementException("the poll is already closed");
            case OPEN: throw new ManagementException("the poll is already open");
            default: status = Status.OPEN;
        }
        logger.info("Election started");
        return Status.STARTED;
    }

    @Override
    public Status close() throws RemoteException, ManagementException {
        // TODO: ADD SYNC TO SWITCH
        switch (status) {
            case REGISTRATION: throw new ManagementException("the poll has not been opened yet");
            case CLOSE: throw new ManagementException("the poll is already close");
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


    /**
     * ManaInspector Service exposed methods.
     **/

    @Override
    public void inspect(int table, String party, VoteAvailableCallbackHandler handler) throws RemoteException, IllegalElectionStateException {
        if (status != Status.REGISTRATION) {
            throw new IllegalElectionStateException("Solo se puede registrar un fiscal antes de que comience la elección");
        }
        final Pair<String, Integer> keyPair = new Pair<>(party, table);
        // TODO: ADD SYNC HERE
        inspectorHandlers.computeIfAbsent(keyPair, k -> new ArrayList<>()); // If keyPair not present, put(keyPair, new ArrayList()
        inspectorHandlers.get(keyPair).add(handler);
    }


    /**
     * Vote Service exposed methods.
     **/

    @Override
    public void vote(Vote vote) throws RemoteException, IllegalElectionStateException {
        /* TODO: Check Syncro */
        String state = vote.getState();
        Integer table = vote.getTable();

        synchronized (voteLock) {
            if (status != Status.OPEN) {
                throw new IllegalElectionStateException("Solo se puede votar si los comicios están abiertos");
            }
            votes.addVote(vote);
        }

        national.addVote(vote.getWinner());

        this.state.putIfAbsent(vote.getState(), new FPTP());
        this.state.get(vote.getState()).addVote(vote.getWinner());

        this.table.putIfAbsent(vote.getTable(), new FPTP());
        this.table.get(vote.getTable()).addVote(vote.getWinner());

        // Check if there are inspectors registered to that table and FPTP candidate
        final Pair<String, Integer> inspectLocation = new Pair<>(vote.getWinner(), vote.getTable());
        Optional.ofNullable(inspectorHandlers.get(inspectLocation))
                .ifPresent(handlerList -> handlerList.forEach(h -> sendNotificationToInspector(h, inspectLocation)));
    }

    /**
     * Query Service exposed methods.
     **/

    @Override
    public Result askNational() throws RemoteException, QueryException {
        if(status == Status.REGISTRATION)
            throw new QueryException("Polls already closed");
        if(votes.getVoteList().isEmpty()) // TODO: highly Inefficient
            throw new QueryException("No Votes");

        switch (status) {
            case OPEN: return national;
            case CLOSE:
                if (natStar == null)
                    natStar = new STAR(votes.getVoteList());
                return natStar;
            default: return null;
        }
    }

    @Override
    public Result askState(String state) throws RemoteException, QueryException {
        if(status == Status.REGISTRATION)
            throw new QueryException("Polls already closed");
        if(votes.isStateEmpty(state)) // TODO check syncro
            throw new QueryException("No Votes");

        switch (status) {
            case OPEN: return this.state.get(state);
            case CLOSE:
                if (!stateSPAV.containsKey(state))
                    stateSPAV.put(state, new SPAV(votes.getStateVoteList(state)));
                return stateSPAV.get(state);
            default: return null;
        }
    }

    @Override
    public Result askTable(Integer table) throws RemoteException, QueryException {
        if(status == Status.REGISTRATION)
            throw new QueryException("Polls already closed");
        if(votes.isTableEmpty(table)) // TODO: check
            throw new QueryException("No Votes");

        switch (status) {
            case OPEN: return this.table.get(table);
            case CLOSE:
                this.table.get(table).setFinal();         //  finished --> Calculates winner and set as final
                return this.table.get(table);
            default: return null;
        }
    }



    /**
     * Private auxiliary methods.
     **/


    private void sendNotificationToInspector(final VoteAvailableCallbackHandler handler, final Pair<String, Integer> inspectLocation) {
        executorService.submit(() -> {
            try {
                handler.voteRegistered();
            } catch (RemoteException e) {
                logger.error("Could not send notification to Inspector. Removing it...");
                // TODO: ADD inspectorHandlers SYNC HERE
                inspectorHandlers.get(inspectLocation).remove(handler);
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