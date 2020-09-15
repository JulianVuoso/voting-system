package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.exceptions.NoVotesException;
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

    private STAR nationalCount;
    private Map<String, SPAV> stateMapCount;
    private Map<Integer, FPTP> tableMapCount;

    public ElectionServiceImpl() {
        status = Status.REGISTRATION;
        inspectorHandlers = new HashMap<>();
        executorService = Executors.newFixedThreadPool(4);
        votes = new Votes();

        // Initialize results for national, state and table
        nationalCount = new STAR();
        stateMapCount = new HashMap<>();
        tableMapCount = new HashMap<>();
    }


    /**
     * Management Service exposed methods.
     **/

    @Override
    public Status open() throws RemoteException, IllegalElectionStateException {
        switch (status) {
            case CLOSE: throw new IllegalElectionStateException("the poll is already closed");
            case OPEN: throw new IllegalElectionStateException("the poll is already open");
            default: status = Status.OPEN;
        }
        logger.info("Election started");
        return Status.STARTED;
    }

    @Override
    public Status close() throws RemoteException, IllegalElectionStateException {
        // TODO: REVISAR COMO voteLock puede ser FIFO
        synchronized (voteLock) {
            switch (status) {
                case REGISTRATION: throw new IllegalElectionStateException("the poll has not been opened yet");
                case CLOSE: throw new IllegalElectionStateException("the poll is already close");
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
     * Inspector Service exposed methods.
     **/

    @Override
    public void inspect(int table, String party, VoteAvailableCallbackHandler handler) throws RemoteException, IllegalElectionStateException {
        if (handler == null) throw new IllegalArgumentException("Handler cannot be null");

        if (status != Status.REGISTRATION) {
            throw new IllegalElectionStateException("It is only possible to register an inspector before the voting starts");
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
        if (vote == null) throw new IllegalArgumentException("Vote cannot be null");

        synchronized (voteLock) {
            if (status != Status.OPEN) {
                throw new IllegalElectionStateException("Voting is only permitted while the election is open");
            }
            votes.addVote(vote);

            // Check if there are inspectors registered to that table and FPTP candidate
            final Pair<String, Integer> inspectLocation = new Pair<>(vote.getWinner(), vote.getTable());
            Optional.ofNullable(inspectorHandlers.get(inspectLocation))
                    .ifPresent(handlerList -> handlerList.forEach(h -> sendNotificationToInspector(h, inspectLocation)));
        }

        synchronized (absentLock) {
            stateMapCount.putIfAbsent(vote.getState(), new SPAV());
            tableMapCount.putIfAbsent(vote.getTable(), new FPTP());
        }

        nationalCount.addPartialVote(vote);
        stateMapCount.get(vote.getState()).addPartialVote(vote);
        tableMapCount.get(vote.getTable()).addPartialVote(vote);
    }

    /**
     * Query Service exposed methods.
     **/

    @Override
    public Result askNational() throws RemoteException, NoVotesException, IllegalElectionStateException {
        if(status == Status.REGISTRATION)
            throw new IllegalElectionStateException("Polls not open");

        switch (status) {
            case OPEN:
                if (nationalCount.isPartialEmpty())
                    throw new NoVotesException("No Votes");
                return nationalCount.getPartialResult();
            case CLOSE:
                synchronized (voteLock) {
                    if (votes.isEmpty())
                        throw new NoVotesException("No Votes");
                    nationalCount.setFinal(votes.getVoteList());
                }
                return nationalCount;
            default: return null;
        }
    }

    @Override
    public Result askState(String state) throws RemoteException, NoVotesException, IllegalElectionStateException {
        if(status == Status.REGISTRATION)
            throw new IllegalElectionStateException("Polls not open");

        switch (status) {
            case OPEN:
                if (!stateMapCount.containsKey(state) || stateMapCount.get(state).isPartialEmpty())
                    throw new NoVotesException("No Votes");
                return stateMapCount.get(state).getPartialResult();
            case CLOSE:
                synchronized (voteLock) {
                    if (votes.isStateEmpty(state))
                        throw new NoVotesException("No Votes");
                    stateMapCount.get(state).setFinal(votes.getStateVoteList(state));
                }
                return stateMapCount.get(state);
            default: return null;
        }
    }

    @Override
    public Result askTable(int table) throws RemoteException, NoVotesException, IllegalElectionStateException {
        if(status == Status.REGISTRATION)
            throw new IllegalElectionStateException("Polls not open");

        switch (status) {
            case OPEN:
                if (!tableMapCount.containsKey(table) || tableMapCount.get(table).isEmpty())
                    throw new NoVotesException("No Votes");
                return tableMapCount.get(table);
            case CLOSE:
                synchronized (voteLock) {
                    if (votes.isTableEmpty(table))
                        throw new NoVotesException("No Votes");
                    tableMapCount.get(table).setFinal(votes.getTableVoteList(table));
                }
                return tableMapCount.get(table);
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