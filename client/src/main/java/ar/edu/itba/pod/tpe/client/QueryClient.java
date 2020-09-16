package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.client.exceptions.ArgumentException;
import ar.edu.itba.pod.tpe.client.utils.ClientUtils;
import ar.edu.itba.pod.tpe.client.utils.QuadConsumer;
import ar.edu.itba.pod.tpe.client.utils.ThrowableBiConsumer;
import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.exceptions.NoVotesException;
import ar.edu.itba.pod.tpe.interfaces.QueryService;
import ar.edu.itba.pod.tpe.models.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

public class QueryClient {

    private static Logger logger = LoggerFactory.getLogger(QueryClient.class);

    /**
     * Params values and argument error
     */
    private static final String SERVER_ADDRESS_PARAM = "serverAddress";
    private static final String FILE_PATH_PARAM = "outPath";
    private static final String ID_PARAM = "id";
    private static final String STATE_PARAM = "state";
    private static final int ERROR_STATUS = 1;

    /**
     * Properties brought from parameters
     */
    private static InetSocketAddress serverAddress;
    private static String path;
    private static Optional<String> state;
    private static Optional<Integer> table;

    private static final String PERCENTAGE_HEADER = "Percentage;Party";
    private static final String APPROVAL_HEADER = "Approval;Party";
    private static final String SCORE_HEADER = "Score;Party";


    public static void main(String[] args) {
        logger.info("tpe1-g6 Query Client Starting ...");

        try {
            argumentParsing();
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
            return;
        }

        logger.debug("Args: " + serverAddress.getHostName() + " - " + serverAddress.getPort() + " - " + path);

        try {
            final Registry registry = LocateRegistry.getRegistry(serverAddress.getHostName(), serverAddress.getPort());
            final QueryService service = (QueryService) registry.lookup(QueryService.class.getName());

            if (state.isPresent())
                genericFunction(path, service.askState(state.get()), printSPAV);
            else if (table.isPresent())
                genericFunction(path, service.askTable(table.get()), printFPTP);
            else genericFunction(path, service.askNational(), printSTAR);

        } catch (RemoteException e) {
            System.err.println("Remote communication failed.");
            System.exit(ERROR_STATUS);
        } catch (NotBoundException e) {
            System.err.println("Server " + QueryService.class.getName() + " has no associated binding.");
            System.exit(ERROR_STATUS);
        } catch (NoVotesException | IllegalElectionStateException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
        }

    }


    /**
     * Opens the csv printer, executes the printing function, and closes the printer.
     * @param file File path for the csv printer.
     * @param result Result from the service response.
     * @param printFunction Print function to be applied.
     */
    private static void genericFunction(String file, Result result, ThrowableBiConsumer<Result, CSVPrinter, IOException> printFunction) {
        try (final CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file), CSVFormat.newFormat(';')
                .withRecordSeparator('\n'))) {

            // Applies function to result and writes on the csv
            if (result.isPartial())
               printFPTP.accept(result, csvPrinter);
            else printFunction.accept(result, csvPrinter);

            // Closes the csv printer
            csvPrinter.flush();
        } catch (IOException e){
            System.err.println("Error while printing CSV file");
        }
    }


    /**
     * Consumer prints with the csv printer the formatted output.
     */
    private static final QuadConsumer<Map.Entry<String, ? extends Number>, CSVPrinter, String, String> fillCSV = (results, printer, decFormat, symbol) -> {
        String party = results.getKey();
        DecimalFormat format = new DecimalFormat(decFormat, new DecimalFormatSymbols(Locale.ENGLISH));
        String value = format.format(results.getValue()) + symbol;
        try {
            printer.printRecord(value, party);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


    /**
     * Throwable consumer, prints the result as a FPTP
     */
    private static final ThrowableBiConsumer<Result, CSVPrinter, IOException> printFPTP = (res, printer) -> {
        FPTP results = (FPTP) res;

        // Print FPTP header, sort and fill csv with results
        printer.printRecord(PERCENTAGE_HEADER);
        results.getPercentagesMap()
                .entrySet()
                .stream()
                .sorted(Result.sortDoubleMap)
                .forEach(r -> fillCSV.accept(r, printer, "##.00", "%"));

        // Print winner if final result
        if(!results.isPartial()) {
            printer.printRecord("Winner");
            printer.printRecord(results.getWinner());
        }
    };


    /**
     * Throwable consumer, prints the result as a STAR
     */
    private static final ThrowableBiConsumer<Result, CSVPrinter, IOException> printSTAR = (res, printer) -> {
        STAR results = (STAR) res;

        // Print STAR score header, sort and fill csv with first round results
        printer.printRecord(SCORE_HEADER);
        results.getFirstStage().entrySet().stream().sorted(Result.sortIntegerMap).forEach(r -> fillCSV.accept(r, printer, "##", ""));

        // Print STAR percentage header, sort and fill csv with second round results
        printer.printRecord(PERCENTAGE_HEADER);
        results.getSecondStage().entrySet().stream().sorted(Result.sortDoubleMap).forEach(r -> fillCSV.accept(r, printer, "##.00", "%"));

        // Print winner
        printer.printRecord("Winner");
        printer.printRecord(results.getWinner());

    };


    /**
     * Throwable consumer, prints the result as a SPAV
     */
    private static final ThrowableBiConsumer<Result, CSVPrinter, IOException> printSPAV = (res, printer) -> {
        SPAV results = (SPAV) res;

        // For each round do
        for (int i = 0; i < SPAV.maxRounds; i++) {

            // Print SPAV approval header
            printer.printRecord("Round " + (i + 1));
            printer.printRecord(APPROVAL_HEADER);
            results.getRound(i).entrySet().stream().sorted(Result.sortDoubleMap).forEach(r -> fillCSV.accept(r, printer, "##.00", ""));

            // Print winners
            printer.printRecord("Winners");
            printer.printRecord(Arrays.stream(results.getWinner()).limit(i + 1).collect(Collectors.joining(", ")));
        }
    };


    /**
     * Ex.
     * -DserverAddress=xx.xx.xx.xx:yyyy        --> host:port
     * -Dstate=stateName       (not required)  --> state
     * -Did=pollingPlaceNumber (not required)  --> type of query
     * -DoutPath=fileName                      --> fileName.csv (out)
     *
     * Parses arguments from terminal
     * @throws ArgumentException
     */
    private static void argumentParsing() throws ArgumentException {
        Properties properties = System.getProperties();

        try {
            serverAddress = ClientUtils.getInetAddress(properties.getProperty(SERVER_ADDRESS_PARAM));
        } catch (URISyntaxException e) {
            throw new ArgumentException("Server Address must be supplied using -DserverAddress and its format must be xx.xx.xx.xx:yyyy");
        }

        path = Optional.ofNullable(properties.getProperty(FILE_PATH_PARAM)).orElseThrow(new ArgumentException("Path must be supplied using -DoutPath"));
        state = Optional.ofNullable(properties.getProperty(STATE_PARAM));

        String aux = properties.getProperty(ID_PARAM);
        if (aux != null) {
            try {
                table = Optional.of(Integer.parseInt(aux));
            } catch (NumberFormatException e) {
                throw new ArgumentException("Invalid -Did. Id is not a valid number");
            }
        } else table = Optional.empty();

        if (state.isPresent() && table.isPresent()) {
            throw new ArgumentException("Specify only a state through -Dstate or a table number through -Did. Do not specify both");
        }
    }

}