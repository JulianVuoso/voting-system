package ar.edu.itba.pod.tpe.server;

import ar.edu.itba.pod.tpe.exceptions.ManagementException;
import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.exceptions.QueryException;
import ar.edu.itba.pod.tpe.interfaces.*;
import ar.edu.itba.pod.tpe.models.*;
import ar.edu.itba.pod.tpe.server.utils.Pair;
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

    // TODO: DEFINIR TIPO DE THREAD POOL Y SI TIENE LIMITE DE CANTIDAD --> 10, ver que espere y no lo rebote
    //  Ver los otros tipos de pool, el fixed tiene para limite
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private Map<String, Map<Integer, List<Vote>>> votes = new HashMap<>();
    private final Object voteLock = "voteLock";

    // TODO: VER SI SE PUEDE JUNTAR CON LOS RESULTADOS FINALES usando Result
    //  Ver si moviendo la logica dentro de FPTP se puede
    private FPTP natFptp = new FPTP();
    private Map<String, FPTP> stateFptp = new HashMap<>();
    private Map<Integer, FPTP> tableFptp = new HashMap<>();

    private STAR natStar = null;
    private Map<String, SPAV> stateSPAV = new HashMap<>();

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
        // TODO: ADD SYNC TO SWITCH
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

    // TODO: VER SI TIENE QUE SER SYNC CON ALGO
    @Override
    public void inspect(int table, String party, VoteAvailableCallbackHandler handler) throws RemoteException, IllegalElectionStateException {
        if (status != Status.UNDEFINED) {
            throw new IllegalElectionStateException("Solo se puede registrar un fiscal antes de que comience la elección");
        }
        final Pair<String, Integer> keyPair = new Pair<>(party, table);
        // TODO: ADD SYNC HERE
        inspectorHandlers.computeIfAbsent(keyPair, k -> new ArrayList<>()); // If keyPair not present, put(keyPair, new ArrayList()
        inspectorHandlers.get(keyPair).add(handler);
    }

    private void sendNotificationToInspector(final VoteAvailableCallbackHandler handler) {
        executorService.submit(() -> {
            try {
                handler.voteRegistered();
            } catch (RemoteException e) {
                logger.error("Could not send notification to Inspector");
                // FIXME: DEBERIA DESREGISTRARLO?? --> Preguntar --> Sleep, reintento, mato
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
        String state = vote.getState();
        Integer table = vote.getTable();

        synchronized (voteLock) {
            if (status != Status.OPEN) {
                throw new IllegalElectionStateException("Solo se puede votar si los comicios están abiertos");
            }

            if (!votes.containsKey(state)) {
                votes.put(state, new HashMap<>());
            }
            if (!votes.get(state).containsKey(table)) {
                votes.get(state).put(table, new ArrayList<>());
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

        // Check if there are inspectors registered to that table and FPTP candidate
        final Pair<String, Integer> inspectLocation = new Pair<>(vote.getVoteFPTP(), vote.getTable());
        Optional.ofNullable(inspectorHandlers.get(inspectLocation))
                .ifPresent(handlerList -> handlerList.forEach(this::sendNotificationToInspector));
    }


    @Override
    public Result askNational() throws RemoteException, QueryException {
        if(status == Status.UNDEFINED)
            throw new QueryException("Polls already closed");
        if(allVotes().isEmpty())
            throw new QueryException("No Votes");

        switch (status) {
            case OPEN: return natFptp;
            case CLOSE:
                if (natStar == null)
                    natStar = new STAR(allVotes());
                return natStar;
            default: return null;
        }
    }

    @Override
    public Result askState(String state) throws RemoteException, QueryException {
        if(status == Status.UNDEFINED)
            throw new QueryException("Polls already closed");
        if(votes.get(state).values().isEmpty())
            throw new QueryException("No Votes");

        switch (status) {
            case OPEN: return stateFptp.get(state);
            case CLOSE:
                if (!stateSPAV.containsKey(state))
                    stateSPAV.put(state, new SPAV(stateVotes(state)));
                return stateSPAV.get(state);
            default: return null;
        }
    }

    @Override
    public Result askTable(Integer table) throws RemoteException, QueryException {
        if(status == Status.UNDEFINED)
            throw new QueryException("Polls already closed");
        if(emptyTable(table))
            throw new QueryException("No Votes");

        switch (status) {
            case OPEN: return tableFptp.get(table);
            case CLOSE:
                tableFptp.get(table).setPartial(false);         //  finished --> Calculates winner and set as final
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
        for(List<Vote> list : votes.get(state).values())        // save every state votes and return list
            stateVotes.addAll(list);
        return stateVotes;
    }

    private boolean emptyTable(Integer table){
        boolean emptyTable = true;
        for(Map<Integer, List<Vote>> maps : votes.values()){
            if(!maps.get(table).isEmpty())
                emptyTable = false;
        }
        return emptyTable;
    }
}