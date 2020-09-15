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

    private final Object voteLock = "voteLock", inspectorsLock = "inspectorLock", absentLock = "absentLock";

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
        // TODO: REVISAR COMO voteLock puede ser FIFO
        synchronized (voteLock) {
            switch (status) {
                case REGISTRATION: throw new ManagementException("the poll has not been opened yet");
                case CLOSE: throw new ManagementException("the poll is already close");
                default: status = Status.CLOSE;
            }
        }

        // Kill all handlers
        synchronized (inspectorsLock) {
            inspectorHandlers.values().forEach(handlerList -> handlerList.forEach(this::sendElectionFinishedToInspector));
            inspectorHandlers.clear();
        }
        // Previously submitted tasks are executed, but no new tasks will be accepted
        executorService.shutdown();
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
        synchronized (inspectorsLock) {
            inspectorHandlers.computeIfAbsent(keyPair, k -> new ArrayList<>()); // If keyPair not present, put(keyPair, new ArrayList()
            inspectorHandlers.get(keyPair).add(handler);
        }
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

            // Check if there are inspectors registered to that table and FPTP candidate
            final Pair<String, Integer> inspectLocation = new Pair<>(vote.getWinner(), vote.getTable());
            synchronized (inspectorsLock) {
                Optional.ofNullable(inspectorHandlers.get(inspectLocation))
                        .ifPresent(handlerList -> handlerList.forEach(h -> sendNotificationToInspector(h, inspectLocation)));
            }
        }

        synchronized (absentLock) {
            this.state.putIfAbsent(vote.getState(), new FPTP());
            this.table.putIfAbsent(vote.getTable(), new FPTP());
        }

        national.addVote(vote.getWinner());
        this.state.get(vote.getState()).addVote(vote.getWinner());
        this.table.get(vote.getTable()).addVote(vote.getWinner());
    }

    /**
     * Query Service exposed methods.
     **/

    @Override
    public Result askNational() throws RemoteException, QueryException {
        if(status == Status.REGISTRATION)
            throw new QueryException("Polls not open");

        switch (status) {
            case OPEN:
                if (national.isEmpty())
                    throw new QueryException("No Votes");
                return national;
            case CLOSE:
                synchronized (voteLock) {
                    if (votes.isEmpty())
                        throw new QueryException("No Votes");
                    if (natStar == null)
                        natStar = new STAR(votes.getVoteList());
                }
                return natStar;
            default: return null;
        }
    }

    @Override
    public Result askState(String state) throws RemoteException, QueryException {
        if(status == Status.REGISTRATION)
            throw new QueryException("Polls already closed");

        switch (status) {
            case OPEN:
                if (this.state.get(state).isEmpty())
                    throw new QueryException("No Votes");
                return this.state.get(state);
            case CLOSE:
                synchronized (voteLock) {
                    if (votes.isStateEmpty(state))
                        throw new QueryException("No Votes");
                    if (!stateSPAV.containsKey(state))
                        stateSPAV.put(state, new SPAV(votes.getStateVoteList(state)));
                }
                return stateSPAV.get(state);
            default: return null;
        }
    }

    @Override
    public Result askTable(Integer table) throws RemoteException, QueryException {
        if(status == Status.REGISTRATION)
            throw new QueryException("Polls already closed");

        switch (status) {
            case OPEN:
                if (this.table.get(table).isEmpty())
                    throw new QueryException("No Votes");
                return this.table.get(table);
            case CLOSE:
                synchronized (voteLock) {
                    if (votes.isTableEmpty(table))
                        throw new QueryException("No Votes");
                    // TODO Llamar a votes.getTableVotes(table)
                    this.table.get(table).setFinal(votes.getVoteList());         //  finished --> Calculates winner and set as final
                }
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
                synchronized (inspectorsLock) {
                    if (inspectorHandlers.containsKey(inspectLocation))
                        inspectorHandlers.get(inspectLocation).remove(handler);
                }
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