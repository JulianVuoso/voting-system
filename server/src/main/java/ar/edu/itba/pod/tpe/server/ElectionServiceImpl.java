package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.ManagementException;
import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.interfaces.*;
import ar.edu.itba.pod.tpe.models.*;
import ar.edu.itba.pod.tpe.server.utils.Pair;
import ar.edu.itba.pod.tpe.stub.InspectionVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

    // TODO: VER SI SE PUEDE JUNTAR CON LOS RESULTADOS FINALES
    // TODO: AGREGAR RES FINALES A VARIABLES
    private FPTP natFptp = new FPTP();
    private Map<String, FPTP> stateFptp = new HashMap<>();
    private Map<Integer, FPTP> tableFptp = new HashMap<>();

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

        // NACIONAL: voto que entra, voto que se suma al mapa general FPTP
        natFptp.getMap().put(vote.getVoteFPTP(), natFptp.getMap().getOrDefault(vote.getVoteFPTP(), 0) + 1);

        stateFptp.putIfAbsent(vote.getState(), new FPTP());                                     // STATE: si es el primer voto de esa provincia le agrego un FPTP
        stateFptp.get(vote.getState()).getMap().put(vote.getVoteFPTP(), stateFptp.get(vote.getState()).getMap().getOrDefault(vote.getVoteFPTP(), 0) + 1);

        // Luego obtengo ese FPTP y le meto en key Party 1 voto mas
        tableFptp.putIfAbsent(vote.getTable(), new FPTP());                                     // TABLE: same a state
        tableFptp.get(vote.getTable()).getMap().put(vote.getVoteFPTP(), tableFptp.get(vote.getTable()).getMap().getOrDefault(vote.getVoteFPTP(), 0) + 1);
    }

    /**       *************************************         **********************************           **/

    @Override
    public Result askNational() throws RemoteException, QueryException {
        switch (status) {
            case UNDEFINED: throw new QueryException("Polls already closed");
            case OPEN: return natFptp;
            case CLOSE: return new STAR(allVotes());
            default: return null;
        }
    }

    @Override
    public Result askState(String state) throws RemoteException, QueryException {
        switch (status) {
            case UNDEFINED: throw new QueryException("Polls already closed");
            case OPEN: return stateFptp.get(state);
            case CLOSE:
                return new SPAV(stateVotes(state));
            default: return null;
        }
    }

    @Override
    public Result askTable(Integer table) throws RemoteException, QueryException {
        switch (status) {
            case UNDEFINED: throw new QueryException("Polls already closed");
            case OPEN: return tableFptp.get(table);
            case CLOSE:
                tableFptp.get(table).setPartial(false);     //  finished --> Calculates winner
//                tableFptp.get(table).obtainWinner();
                return tableFptp.get(table);
            default: return null;
        }
    }

    private List<Vote> allVotes(){
        List<Vote> totalVotes = new ArrayList<>();
        for(Map<Integer, List<Vote>> vote : votes.values()){
            for(List<Vote> list : vote.values())
                totalVotes.addAll(list);
        }
        return totalVotes;
    }

    private List<Vote> stateVotes(String state) {
        List<Vote> stateVotes = new ArrayList<>();
        for(List<Vote> list : votes.get(state).values())        // me quedo con todos los votos del state
            stateVotes.addAll(list);
        return stateVotes;
    }
}

/*
    firstStar.putIfAbsent(party, 0);
    firstStar.put(party, firstStar.get(party) + vote.getSTAR().get(party));

    es equivalente a lo siguiente en todos los putIfAbsent
        firstStar.put(party, firstStar.getOrDefault(party, 0) + vote.getSTAR().get(party));

* */