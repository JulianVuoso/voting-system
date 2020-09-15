package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.client.exceptions.ArgumentException;
import ar.edu.itba.pod.tpe.client.utils.ClientUtils;
import ar.edu.itba.pod.tpe.client.utils.ThrowableBiConsumer;
import ar.edu.itba.pod.tpe.client.utils.TriConsumer;
import ar.edu.itba.pod.tpe.interfaces.QueryService;
import ar.edu.itba.pod.tpe.models.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

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


    public static void main(String[] args) throws RemoteException, NotBoundException, ArgumentException {
        logger.info("tpe1-g6 Query Client Starting ...");

        try {
            argumentParsing();
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
            return;
        }

        logger.debug("Args: " + serverAddress.getHostName() + " - " + serverAddress.getPort() + " - " + path);


        final Registry registry = LocateRegistry.getRegistry(serverAddress.getHostName(), serverAddress.getPort());
        final QueryService service = (QueryService) registry.lookup(QueryService.class.getName());


    }


    private static TriConsumer<String, Result, ThrowableBiConsumer<Result, CSVPrinter, IOException>> func = (file, result, biConsumer) -> {
        try {
            // Opens csv printer
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file));
            final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.newFormat(';')
                    .withRecordSeparator('\n'));

            // Applies function to result and writes on the csv
            biConsumer.accept(result, csvPrinter);

            // Closes the csv printer
            csvPrinter.flush();
        } catch (IOException e){
            System.err.println("Error while printing CSV file");
        }
    };




    private static void printFPTP(String file, Result result) {
        final ThrowableBiConsumer<Result, CSVPrinter, IOException> print = (res, printer) -> {
            FPTP results = (FPTP) res;

            // Print FPTP header, sort and fill csv with results
            printer.printRecord(PERCENTAGE_HEADER);
            results.getPercentagesMap()
                    .entrySet()
                    .stream()
                    .sorted(Result.sortDoubleMap)
                    .forEach(r -> fillCSVDouble.accept(r, printer, true));

            // Print winner if final result
            if(!results.isPartial()) {
                printer.printRecord("Winner");
                printer.printRecord(results.getWinner());
            }
        };

       func.accept(file, result, print);
    }

    private static void printSTAR(String file, Result result) {
        final ThrowableBiConsumer<Result, CSVPrinter, IOException> print = (res, printer) -> {
            STAR results = (STAR) res;

            // Print STAR score header, sort and fill csv with first round results
            printer.printRecord(SCORE_HEADER);
            results.getFirstRound().entrySet().stream().sorted(Result.sortIntegerMap).forEach(r -> fillCSVInteger.accept(r, printer));

            // Print STAR percentage header, sort and fill csv with second round results
            printer.printRecord(PERCENTAGE_HEADER);
            results.getSecondRound().entrySet().stream().sorted(Result.sortDoubleMap).forEach(r -> fillCSVDouble.accept(r, printer, true));

            // Print winner
            printer.printRecord("Winner");
            printer.printRecord(results.getWinner());

        };

        func.accept(file, result, print);
    }

    private static void printSPAV(String file, Result result) {
        final ThrowableBiConsumer<Result, CSVPrinter, IOException> print = (res, printer) -> {
            SPAV results = (SPAV) res;

            // For each round do
            for (int i = 0; i < SPAV.maxRounds; i++) {

                // Print SPAV approval header
                printer.printRecord("Round " + i);
                printer.printRecord(APPROVAL_HEADER);
                results.getRound(i).entrySet().stream().sorted(Result.sortDoubleMap).forEach(r -> fillCSVDouble.accept(r, printer, false));

                // Print winners
                printer.printRecord("Winners");
                StringBuilder stringBuilder = new StringBuilder(results.getWinner()[0]);
                IntStream.range(1, i).forEach(n -> stringBuilder.append(", ").append(results.getWinner()[n]));
                printer.printRecord(stringBuilder);
            }
        };

        func.accept(file, result, print);
    }


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








    private static final TriConsumer<Map.Entry<String, Double>, CSVPrinter, Boolean> fillCSVDouble = (results, printer, b) -> {
        String party = results.getKey();
        DecimalFormat format = new DecimalFormat("##.00");
        String percent = format.format(results.getValue()) + ((b)? "%" : "");
        try {
            printer.printRecord(percent, party);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


    private static final BiConsumer<Map.Entry<String, Integer>, CSVPrinter> fillCSVInteger = (results, printer) -> {
        String party = results.getKey();
        DecimalFormat format = new DecimalFormat("##");
        String percent = format.format(results.getValue());
        try {
            printer.printRecord(percent, party);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };


}