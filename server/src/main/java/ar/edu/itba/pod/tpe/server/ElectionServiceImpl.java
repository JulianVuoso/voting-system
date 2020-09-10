package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.ManagementException;
import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.interfaces.*;
import ar.edu.itba.pod.tpe.models.*;
import ar.edu.itba.pod.tpe.server.utils.Pair;
import ar.edu.itba.pod.tpe.stub.InspectionVote;
import ar.edu.itba.pod.tpe.stub.Vote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ElectionServiceImpl implements ManagementService,
                                            InspectionService,
                                                VotingService,
                                                QueryService {

    private static Logger logger = LoggerFactory.getLogger(ElectionServiceImpl.class);

    private Status status;
    private Map<Pair<String, Integer>, List<VoteAvailableCallbackHandler>> inspectorHandlers = new HashMap<>();

    // TODO: DEFINIR TIPO DE THREAD POOL Y SI TIENE LIMITE DE CANTIDAD
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private Map<String, Map<Integer, List<Vote>>> votes = new HashMap<>();
    private final Object voteLock = "voteLock";

    FPTP natFptp = new FPTP();
    Map<String, FPTP> stateFptp = new HashMap<>();
    Map<Integer, FPTP> tableFptp = new HashMap<>();

    public ElectionServiceImpl() {
        status = Status.UNDEFINED;
    }

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
        switch (status) {
            case UNDEFINED: throw new ManagementException("the poll has not been opened yet");
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

    @Override
    public void vote(Vote vote) throws RemoteException, IllegalElectionStateException {
        /* TODO: Check Syncro */
        if (status != Status.OPEN) {
            throw new IllegalElectionStateException("Solo se puede votar si los comicios están abiertos");
        }
        String state = vote.getState();
        Integer table = vote.getTable();
        synchronized (voteLock){
            if(!votes.containsKey(state)){
                votes.put(state,new HashMap<>());
            }
            if(!votes.get(state).containsKey(table)){
                votes.get(state).put(table,new ArrayList<>());
            }
            votes.get(state).get(table).add(vote);
        }

        natFptp.getMap().putIfAbsent(vote.getVoteFPTP(),0);                                     // NACIONAL: voto que entra, voto que se suma al mapa general FPTP
        natFptp.getMap().put(vote.getVoteFPTP(), natFptp.getMap().get(vote.getVoteFPTP())+1);


        stateFptp.putIfAbsent(vote.getState(), new FPTP());                                     // STATE: si es el primer voto de esa provincia le agrego un FPTP
        stateFptp.get(vote.getState()).getMap().putIfAbsent(vote.getVoteFPTP(),0);
        stateFptp.get(vote.getState()).getMap().put(vote.getVoteFPTP(), stateFptp.get(vote.getState()).getMap().get(vote.getVoteFPTP())+1);

        // Luego obtengo ese FPTP y le meto en key Party 1 voto mas
        tableFptp.putIfAbsent(vote.getTable(), new FPTP());                                     // TABLE: same a state
        tableFptp.get(vote.getTable()).getMap().putIfAbsent(vote.getVoteFPTP(),0);
        tableFptp.get(vote.getTable()).getMap().put(vote.getVoteFPTP(), tableFptp.get(vote.getTable()).getMap().get(vote.getVoteFPTP())+1);

    }

    /**       *************************************         **********************************           **/

    @Override
    public Result askNational() throws RemoteException, QueryException {
        switch (status) {
            case CLOSE: throw new QueryException("Polls already closed");
            case OPEN: return natFptp;
            default: return new STAR(firstSTAR(), secondSTAR());
        }
    }

    @Override
    public Result askState(String state) throws RemoteException, QueryException {
        switch (status) {
            case CLOSE: throw new QueryException("Polls already closed");
            case OPEN: return stateFptp.get(state);
            default:
                String[] winners = new String[3];
                Map<String, Double> round1 = spavIterator(state, null);
                winners[0] = Collections.max(round1.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
                Map<String, Double> round2 = spavIterator(state, winners);
                winners[1] = Collections.max(round2.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();
                Map<String, Double> round3 = spavIterator(state, winners);
                winners[2] = Collections.max(round3.entrySet(), Comparator.comparingDouble(Map.Entry::getValue)).getKey();

                return new SPAV(round1, round2, round3, winners);
        }
    }

    @Override
    public Result askTable(Integer table) throws RemoteException, QueryException {
        switch (status) {
            case CLOSE: throw new QueryException("Polls already closed");
            case OPEN: return tableFptp.get(table);
            default: tableFptp.get(table).setPartial(false);     //  finished
                tableFptp.get(table).obtainWinner();
                return tableFptp.get(table);
        }
    }

    private Map<String, Double> spavIterator(String state, String[] winners){
        List<Vote> stateVotes = new ArrayList<>();
        for(List<Vote> list : votes.get(state).values())        // me quedo con todos los votos del state
            stateVotes.addAll(list);

        Map<String, Double> spavRound = new HashMap<>();
        for(Vote vote : stateVotes){                                // por cada voto
            Map<String, Double> mapVote = vote.getSPAV(winners);    // obtengo su party -> puntaje
            Set<String> parties = mapVote.keySet();                 // y por cada party
            for(String party : parties){
                spavRound.putIfAbsent(party, 0.0);                  // le sumo al map general tal puntaje
                spavRound.put(party, spavRound.get(party) + mapVote.get(party));
            }
        }
        return spavRound;
    }

    private List<Vote> allVotes(){
        List<Vote> totalVotes = new ArrayList<>();
        for(Map<Integer, List<Vote>> vote : votes.values()){
            for(List<Vote> list : vote.values())
                totalVotes.addAll(list);
        }
        return totalVotes;
    }

    private Map<String, Integer> firstSTAR() {
//        List<Vote> totalVotes = allVotes();
        Map<String, Integer> firstStar = new HashMap<>();
        for(Vote vote : allVotes()){
            for(String party : vote.getSPAV(null).keySet()){
                firstStar.putIfAbsent(party, 0);
                firstStar.put(party, firstStar.get(party) + vote.getSPAV(null).get(party).intValue());
            }
        }
        return firstStar;
    }

    private Map<String, Double> secondSTAR() {
        Map<String, Integer> aux = natFptp.getMap();                // TODO: ver una forma mejor
        Map<String, Double> secondStar = new HashMap<>();
        Map<String, Integer> points = new HashMap<>();

        String winner1 = Collections.max(aux.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        aux.put(winner1, -1);
        String winner2 = Collections.max(aux.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();

        // winner 1 -> %
        // winner 2 -> %
        String winnerAlpha = winner1.compareTo(winner2)<0 ? winner1:winner2;
        for(Vote vote : allVotes()){
            if(vote.getSPAV(null).get(winner1) > vote.getSPAV(null).get(winner2)){    // si en el voto w1 > w2
                points.putIfAbsent(winner1, 0);                               // le sumo uno a w1 en el map
                points.put(winner1, points.get(winner1)+1);
            }
            else{
                if(vote.getSPAV(null).get(winner1) < vote.getSPAV(null).get(winner2)){
                    points.putIfAbsent(winner2, 0);
                    points.put(winner2, points.get(winner2)+1);
                }
                else{
                    points.putIfAbsent(winnerAlpha, 0);                       // si son iguales se lo sumo
                    points.put(winnerAlpha, points.get(winnerAlpha)+1);       // al menor alfabeticamente
                }
            }
        }
        int total = points.get(winner1) + points.get(winner2);
        secondStar.put(winner1, points.get(winner1).doubleValue() / total * 100);
        secondStar.put(winner2, 100 - points.get(winner1).doubleValue());

        return secondStar;
    }
}
