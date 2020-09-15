package ar.edu.itba.pod.tpe;

import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.exceptions.ManagementException;
import ar.edu.itba.pod.tpe.exceptions.QueryException;

import ar.edu.itba.pod.tpe.models.*;
import ar.edu.itba.pod.tpe.server.ElectionServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class QueryTest {

    private static final int VOTING_TABLE_1 = 1000, VOTING_TABLE_2 = 500, VOTES_1 = 5, VOTES_2 = 2;
    private static final String VOTING_STATE_1 = "JUNGLE", VOTING_STATE_2 = "SAVANNAH";
    private static final String PARTY_1 = "BUFFALO", PARTY_2 = "OWL", PARTY_3 = "TURTLE", PARTY_4 = "TIGER";

    private ElectionServiceImpl service;

    @Before
    public final void before(){
        service = new ElectionServiceImpl();
    }

    @Test(expected = QueryException.class)
    public final void testClosedAsk() throws QueryException, RemoteException {
        try{
            service.askNational();      // Ask for national results without opening polls
        }
        catch (QueryException qe){
            assertEquals("Polls not open", qe.getMessage());
            throw qe;
        }
    }

    @Test(expected = QueryException.class)
    public final void testEmptyNational() throws QueryException, ManagementException, RemoteException {
        service.open();
        try{
            service.askNational();      // Ask for national results without any voting
        }
        catch (QueryException qe){
            assertEquals("No Votes", qe.getMessage());
            throw qe;
        }
    }

    @Test(expected = QueryException.class)
    public final void testEmptyState() throws QueryException, ManagementException, RemoteException, IllegalElectionStateException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY_1,4);

        service.open();
        service.vote(new Vote(VOTING_TABLE_1, VOTING_STATE_1, testMap, PARTY_1));
        try{
            service.askState(VOTING_STATE_2);       // Ask for SAVANNAH results after voting only on JUNGLE state
        }
        catch (QueryException qe){
            assertEquals("No Votes", qe.getMessage());
            throw qe;
        }
    }

    @Test(expected = QueryException.class)
    public final void testEmptyTable() throws QueryException, ManagementException, RemoteException, IllegalElectionStateException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY_1,4);

        service.open();
        service.vote(new Vote(VOTING_TABLE_1, VOTING_STATE_1, testMap, PARTY_1));
        try{
            service.askTable(VOTING_TABLE_2);       // Ask for SAVANNAH results after voting only on JUNGLE state
        }
        catch (QueryException qe){
            assertEquals("No Votes", qe.getMessage());
            throw qe;
        }
    }

    @Test
    public final void testAskNationalOpen() throws IllegalElectionStateException, RemoteException, ManagementException, QueryException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY_1,4);

        Map<String, Integer> testMap2 = new HashMap<>();
        testMap.put(PARTY_2,2);

        service.open();                                                             // open polls
        service.vote(new Vote(VOTING_TABLE_1,VOTING_STATE_1,testMap,PARTY_1));      // apply 4 BUFFALO votes
        service.vote(new Vote(VOTING_TABLE_1,VOTING_STATE_1,testMap2,PARTY_2));     // apply 2 OWL votes
        FPTP results = (FPTP) service.askNational();
        assertEquals(50, results.getPercentagesMap().get(PARTY_1).intValue());         // verify 50% votes
        assertEquals(50, results.getPercentagesMap().get(PARTY_2).intValue());         // verify 50% votes
        service.close();
    }

    @Test
    public final void testAskNationalClosed() throws IllegalElectionStateException, RemoteException, ManagementException, QueryException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY_1, 4);                                                    // distribute votes type 1
        testMap.put(PARTY_2, 2);
        testMap.put(PARTY_3, 3);

        Map<String, Integer> testMap2 = new HashMap<>();                            // distribute votes type 2
        testMap2.put(PARTY_1, 2);
        testMap2.put(PARTY_2, 5);
        testMap2.put(PARTY_3, 1);

        service.open();                                                             // open polls

        for(int i = 0 ; i < VOTES_1 ; i++)                                          // apply 5 type 1 votes
            service.vote(new Vote(VOTING_TABLE_1, VOTING_STATE_1, testMap, PARTY_1));
        for(int i = 0 ; i < VOTES_2 ; i++)                                          // apply 2 type 2 votes
            service.vote(new Vote(VOTING_TABLE_2, VOTING_STATE_2, testMap2, PARTY_2));

        service.close();

        STAR results = (STAR) service.askNational();
        assertEquals(24, results.getFirstStage().get(PARTY_1).intValue());   // verify 24 total votes on BUFFALO
        assertEquals(20, results.getFirstStage().get(PARTY_2).intValue());  // verify 28 total votes on OWL
        assertEquals(PARTY_1, results.getWinner());                                   // verify final winner -> BUFFALO
    }

    @Test
    public final void testAskStateOpen() throws IllegalElectionStateException, RemoteException, ManagementException, QueryException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY_1,4);

        Map<String, Integer> testMap2 = new HashMap<>();
        testMap.put(PARTY_2,2);

        service.open();
        service.vote(new Vote(VOTING_TABLE_1,VOTING_STATE_1,testMap,PARTY_1));                          // apply votes on state 1
        service.vote(new Vote(VOTING_TABLE_1,VOTING_STATE_2,testMap2,PARTY_2));                         // apply votes on state 2
        FPTP results = (FPTP) service.askState(VOTING_STATE_1);                                         // ask for state 1 votes
        assertEquals(100, results.getPercentagesMap().get(PARTY_1).intValue());                             // verify party 1 get 100% votes
        assertEquals(0, results.getPercentagesMap().getOrDefault(PARTY_2, 0.0).intValue());      // verify party 2 get 0% votes
        service.close();
    }

    @Test
    public final void testAskStateClosed() throws IllegalElectionStateException, RemoteException, ManagementException, QueryException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY_1, 4);
        testMap.put(PARTY_2, 2);
        testMap.put(PARTY_3, 1);
        testMap.put(PARTY_4, 3);

        Map<String, Integer> testMap2 = new HashMap<>();
        testMap2.put(PARTY_1, 2);
        testMap2.put(PARTY_2, 5);
        testMap2.put(PARTY_3, 1);
        testMap2.put(PARTY_4, 2);

        service.open();

        for(int i = 0 ; i < VOTES_1 ; i++)
            service.vote(new Vote(VOTING_TABLE_1, VOTING_STATE_1, testMap, PARTY_1));       // apply votes

        for(int i = 0 ; i < VOTES_2 ; i++)
            service.vote(new Vote(VOTING_TABLE_2, VOTING_STATE_1, testMap2, PARTY_2));

        service.close();

        SPAV results = (SPAV) service.askState(VOTING_STATE_1);
        assertEquals(PARTY_1, results.getWinner()[0]);                                // verify first winner -> BUFFALO
        assertEquals(PARTY_2, results.getWinner()[1]);                                // verify second -> OWL
        assertEquals(PARTY_4, results.getWinner()[2]);                                // verify third -> TIGER

    }

    @Test
    public final void testAskTable() throws IllegalElectionStateException, RemoteException, ManagementException, QueryException {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put(PARTY_1,4);

        Map<String, Integer> testMap2 = new HashMap<>();
        testMap.put(PARTY_2,2);

        service.open();

        service.vote(new Vote(VOTING_TABLE_1,VOTING_STATE_1,testMap,PARTY_1));                          // apply vote 1 on table 1
        service.vote(new Vote(VOTING_TABLE_1,VOTING_STATE_1,testMap,PARTY_1));                          // apply vote 1 on table 1
        service.vote(new Vote(VOTING_TABLE_1,VOTING_STATE_1,testMap2,PARTY_2));                          // apply vote 1 on table 1

        service.vote(new Vote(VOTING_TABLE_2,VOTING_STATE_2,testMap2,PARTY_2));                         // apply vote 2 on table 2
        FPTP results = (FPTP) service.askTable(VOTING_TABLE_1);                                         // ask for table 2
        assertEquals(66, results.getPercentagesMap().getOrDefault(PARTY_1, 0.0).intValue());      // verify no votes for party 1 on table 2
        assertEquals(33, results.getPercentagesMap().getOrDefault(PARTY_2, 0.0).intValue());      // verify 2 votes for party 2 on table 2
        service.close();

        FPTP results2 = (FPTP) service.askTable(VOTING_TABLE_1);                                        // same process after closing polls
        assertEquals(66, results2.getPercentagesMap().getOrDefault(PARTY_1, 0.0).intValue());
        assertEquals(33, results2.getPercentagesMap().getOrDefault(PARTY_2, 0.0).intValue());
    }

}
