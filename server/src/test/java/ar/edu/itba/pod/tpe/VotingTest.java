package ar.edu.itba.pod.tpe;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.interfaces.VoteAvailableCallbackHandler;
import ar.edu.itba.pod.tpe.models.Vote;
import ar.edu.itba.pod.tpe.server.ElectionServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VotingTest {

    private static final int VOTING_TABLE = 1000;
    private static final String VOTING_STATE = "JUNGLE";
    private static final String PARTY = "BUFFALO";
    private static final int VOTES = 100;
    private static final int SLEEP_TIME = 1000;


    private ElectionServiceImpl service;
    private VoteAvailableCallbackHandler handler;

    private AtomicInteger registerCounter;
    private boolean electionFinished;

    @Before
    public final void before(){
        service = new ElectionServiceImpl();

        handler = new VoteAvailableCallbackHandler() {
            @Override
            public void voteRegistered() throws RemoteException {
                registerCounter.incrementAndGet();
            }

            @Override
            public void electionFinished() throws RemoteException {
                electionFinished = true;
            }
        };
        registerCounter = new AtomicInteger(0);
        electionFinished = false;
    }

    @Test(expected = IllegalElectionStateException.class)
    public final void testVoteWhenClose() throws IllegalElectionStateException, RemoteException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY,4);

        service.vote(new Vote(VOTING_TABLE,VOTING_STATE,testMap,PARTY));
    }

    @Test(expected = IllegalElectionStateException.class)
    public final void testVoteWhenElectionFinish() throws IllegalElectionStateException, RemoteException{
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY,4);

        service.open();
        service.close();
        service.vote(new Vote(VOTING_TABLE,VOTING_STATE,testMap,PARTY));
    }

    @Test
    public final void testVote() throws IllegalElectionStateException, RemoteException, InterruptedException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY,4);

        service.inspect(VOTING_TABLE,PARTY,handler);

        service.open();
        service.vote(new Vote(VOTING_TABLE,VOTING_STATE,testMap,PARTY));

        service.close();
        Thread.sleep(SLEEP_TIME);

        assertEquals(1, registerCounter.get());
        assertTrue(electionFinished);
    }

    @Test
    public final void testMultipleVotes() throws IllegalElectionStateException, RemoteException, InterruptedException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY,4);

        service.inspect(VOTING_TABLE,PARTY,handler);

        service.open();

        for(int i = 0 ; i < VOTES ; i++) {
            service.vote(new Vote(VOTING_TABLE, VOTING_STATE, testMap, PARTY));
        }

        service.close();
        Thread.sleep(SLEEP_TIME);

        assertEquals(VOTES, registerCounter.get());
        assertTrue(electionFinished);

    }
}
