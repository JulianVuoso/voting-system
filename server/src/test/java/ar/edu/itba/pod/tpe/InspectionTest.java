package ar.edu.itba.pod.tpe;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.exceptions.ManagementException;
import ar.edu.itba.pod.tpe.interfaces.VoteAvailableCallbackHandler;
import ar.edu.itba.pod.tpe.models.Vote;
import ar.edu.itba.pod.tpe.server.ElectionServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class InspectionTest {
    private static final int INSPECTION_TABLE = 1000;
    private static final int OTHER_TABLE = 2222;
    private static final String INSPECTION_PARTY = "TIGER";
    private static final String OTHER_PARTY = "LYNX";
    private static final String STATE_NAME = "JUNGLE";
    private static final int VOTE_COUNT = 10;
    private static final int SLEEP_TIME = 1000;

    private ElectionServiceImpl service;
    private VoteAvailableCallbackHandler handler;

    private AtomicInteger registerCounter;
    private boolean electionFinished;

    private static final int THREAD_COUNT = 2000;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_COUNT);
    private final Runnable inspectRunnable = () -> {
        try {
            service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);
        } catch (RemoteException | IllegalElectionStateException e) {
            fail();
        }
    };

    @Before
    public final void before() {
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
    public final void testInspectAfterOpen() throws RemoteException, ManagementException, IllegalElectionStateException {
        // Open Elections
        service.open();

        // Try to inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);
    }

    @Test(expected = IllegalElectionStateException.class)
    public final void testInspectAfterClose() throws RemoteException, ManagementException, IllegalElectionStateException {
        // Open Elections
        service.open();
        // Close Elections
        service.close();

        // Try to inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);
    }

    @Test
    public final void testInspectBeforeOpen() throws RemoteException, ManagementException, IllegalElectionStateException {
        // Inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);

        // Open Elections
        service.open();
    }

    @Test
    public final void testElectionFinished() throws RemoteException, ManagementException, IllegalElectionStateException, InterruptedException {
        // Inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);

        // Open Elections
        service.open();

        // Close Elections
        service.close();

        // Wait for executorPool
        Thread.sleep(SLEEP_TIME);

        assertTrue(electionFinished);
    }

    @Test
    public final void testVoteType() throws RemoteException, ManagementException, IllegalElectionStateException, InterruptedException {
        final Map<String, Integer> starMap = new HashMap<>();
        starMap.put(INSPECTION_PARTY, 2);

        // Inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);

        // Open Elections
        service.open();

        for (int i = 0; i < VOTE_COUNT; i++) {
            service.vote(new Vote(INSPECTION_TABLE, STATE_NAME, starMap, OTHER_PARTY));
        }

        // Close Elections
        service.close();

        // Wait for executorPool
        Thread.sleep(SLEEP_TIME);

        // Inspection only triggers if FPTP vote goes to INSPECTION_PARTY
        assertEquals(0, registerCounter.get());
        assertTrue(electionFinished);
    }

    @Test
    public final void testVoteCount() throws RemoteException, ManagementException, IllegalElectionStateException, InterruptedException {
        final Map<String, Integer> starMap = new HashMap<>();
        starMap.put(OTHER_PARTY, 2);

        // Inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);

        // Open Elections
        service.open();

        for (int i = 0; i < VOTE_COUNT; i++) {
            service.vote(new Vote(INSPECTION_TABLE, STATE_NAME, starMap, INSPECTION_PARTY));
        }

        // Close Elections
        service.close();

        // Wait for executorPool
        Thread.sleep(SLEEP_TIME);

        assertEquals(VOTE_COUNT, registerCounter.get());
        assertTrue(electionFinished);
    }

    @Test
    public final void testVoteParty() throws RemoteException, ManagementException, IllegalElectionStateException, InterruptedException {
        final Map<String, Integer> starMap = new HashMap<>();
        starMap.put(OTHER_PARTY, 2);

        // Inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);

        // Open Elections
        service.open();

        for (int i = 0; i < VOTE_COUNT; i++) {
            service.vote(new Vote(INSPECTION_TABLE, STATE_NAME, starMap, INSPECTION_PARTY));
        }
        for (int i = 0; i < VOTE_COUNT; i++) {
            service.vote(new Vote(INSPECTION_TABLE, STATE_NAME, starMap, OTHER_PARTY));
        }

        // Close Elections
        service.close();

        // Wait for executorPool
        Thread.sleep(SLEEP_TIME);

        assertEquals(VOTE_COUNT, registerCounter.get());
        assertTrue(electionFinished);
    }

    @Test
    public final void testVoteTable() throws RemoteException, ManagementException, IllegalElectionStateException, InterruptedException {
        final Map<String, Integer> starMap = new HashMap<>();
        starMap.put(OTHER_PARTY, 2);

        // Inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);

        // Open Elections
        service.open();

        for (int i = 0; i < VOTE_COUNT; i++) {
            service.vote(new Vote(INSPECTION_TABLE, STATE_NAME, starMap, INSPECTION_PARTY));
        }
        for (int i = 0; i < VOTE_COUNT; i++) {
            service.vote(new Vote(OTHER_TABLE, STATE_NAME, starMap, OTHER_PARTY));
        }

        // Close Elections
        service.close();

        // Wait for executorPool
        Thread.sleep(SLEEP_TIME);

        assertEquals(VOTE_COUNT, registerCounter.get());
        assertTrue(electionFinished);
    }

    @Test
    public final void testMultipleInspectorsSameParams() throws RemoteException, ManagementException, IllegalElectionStateException, InterruptedException {
        final Map<String, Integer> starMap = new HashMap<>();
        starMap.put(OTHER_PARTY, 2);

        // Inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);

        // Open Elections
        service.open();

        for (int i = 0; i < VOTE_COUNT; i++) {
            service.vote(new Vote(INSPECTION_TABLE, STATE_NAME, starMap, INSPECTION_PARTY));
        }

        // Close Elections
        service.close();

        // Wait for executorPool
        Thread.sleep(SLEEP_TIME);

        assertEquals(2 * VOTE_COUNT, registerCounter.get());
        assertTrue(electionFinished);
    }

    @Test
    public final void testMultipleInspectorsDifferentParams() throws RemoteException, ManagementException, IllegalElectionStateException, InterruptedException {
        final Map<String, Integer> starMap = new HashMap<>();
        starMap.put(OTHER_PARTY, 2);

        // Inspect
        service.inspect(INSPECTION_TABLE, INSPECTION_PARTY, handler);
        service.inspect(INSPECTION_TABLE, OTHER_PARTY, handler);

        // Open Elections
        service.open();

        for (int i = 0; i < VOTE_COUNT; i++) {
            service.vote(new Vote(INSPECTION_TABLE, STATE_NAME, starMap, INSPECTION_PARTY));
        }

        // Close Elections
        service.close();

        // Wait for executorPool
        Thread.sleep(SLEEP_TIME);

        assertEquals(VOTE_COUNT, registerCounter.get());
        assertTrue(electionFinished);
    }

    // TODO: UNCOMMENT WHEN SYNC FINISHED
    // The following test is not 100% fail proof, but should fail sometimes if no synchronization is implemented
    /*@Test
    public final void testInspectSync() throws RemoteException, ManagementException, IllegalElectionStateException, InterruptedException {
        final Map<String, Integer> starMap = new HashMap<>();
        starMap.put(OTHER_PARTY, 2);

        for (int i = 0; i < THREAD_COUNT; i++) {
            threadPool.submit(inspectRunnable);
        }
        threadPool.shutdown();

        // Open Elections
        service.open();

        for (int i = 0; i < VOTE_COUNT; i++) {
            service.vote(new Vote(INSPECTION_TABLE, STATE_NAME, starMap, INSPECTION_PARTY));
        }

        // Close Elections
        service.close();

        // Wait for executorPool
        Thread.sleep(SLEEP_TIME);

        assertEquals(VOTE_COUNT * THREAD_COUNT, registerCounter.get());
        assertTrue(electionFinished);
    }*/
}
