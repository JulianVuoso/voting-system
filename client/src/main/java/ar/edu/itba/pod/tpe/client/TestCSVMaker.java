package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.client.exceptions.ArgumentException;
import ar.edu.itba.pod.tpe.client.utils.ClientUtils;
import ar.edu.itba.pod.tpe.interfaces.QueryService;
import ar.edu.itba.pod.tpe.interfaces.VotingService;
import ar.edu.itba.pod.tpe.models.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class TestCSVMaker {

    private static final String SERVER_ADDRESS_PARAM = "serverAddress";
    private static final String FILE_PATH_PARAM = "outPath";
    private static final String ID_PARAM = "id";
    private static final String STATE_PARAM = "state";


    private static InetSocketAddress serverAddress;
    private static String path;

    private static final String PERCENTAGE_HEADER = "Percentage;Party";
    private static final String APPROVAL_HEADER = "Approval;Party";
    private static final String SCORE_HEADER = "Score;Party";


    public static void main(String[] args) throws RemoteException, NotBoundException {

        final Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        final QueryService service = (QueryService) registry.lookup("service");

        FPTP result = (FPTP) service.askTable(1001);

        printFPTP("outspav4.csv",result);


    }


    private static void printFPTP(String file, Result result){
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file));
            final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.newFormat(';')
                    .withHeader(PERCENTAGE_HEADER)
                    .withRecordSeparator('\n'));

            FPTP results = (FPTP) result;

            /** Armo el mapa con los porcentajes para despues ordenarlos y llenar el csv **/
            Map<String, Double> fptpPerc = new HashMap<>();
            int total=0;
            for(Integer partyVotes : results.getMap().values())
                total+=partyVotes;
            for(String p : results.getMap().keySet())
                fptpPerc.put(p, results.getMap().get(p).doubleValue() / total * 100);

            /** Ordeno **/
            fptpPerc.entrySet().stream().sorted((o1, o2) -> {
                if (!o1.getValue().equals(o2.getValue()))
                    return Double.compare(o2.getValue(), o1.getValue());
                return o1.getKey().compareTo(o2.getKey());
            }).forEach( r -> {
                /** Lleno el csv **/
                String party = r.getKey();
                DecimalFormat format = new DecimalFormat("##.00");
                String percent = format.format(r.getValue()) + "%";
                try {
                    csvPrinter.printRecord(percent, party);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            /** si ya esta terminada la votacion, imprimo el ganador **/
            if(!results.getPartial()){
                csvPrinter.printRecord("Winner");
                csvPrinter.printRecord(results.getWinner());
            }
            csvPrinter.flush();
        }
        catch (IOException e){
            System.err.println("Error while printing CSV file");
        }

    }

    private static void printSTAR(String file, Result result) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file));
            final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.newFormat(';')
                    .withHeader(SCORE_HEADER)
                    .withRecordSeparator('\n'));

            STAR results = (STAR) result;

            /* Ordeno el mapa del primer round */
            results.getFirstRound().entrySet().stream().sorted((o1, o2) -> {
                if (!o1.getValue().equals(o2.getValue()))
                    return Double.compare(o2.getValue(), o1.getValue());
                return o1.getKey().compareTo(o2.getKey());
            }).forEach(r -> {
                String party = r.getKey();
                DecimalFormat format = new DecimalFormat("##");
                String percent = format.format(r.getValue());
                try {
                    csvPrinter.printRecord(percent, party);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            csvPrinter.printRecord(PERCENTAGE_HEADER);
            results.getSecondRound().entrySet().stream().sorted((o1, o2) -> {
                if (!o1.getValue().equals(o2.getValue()))
                    return Double.compare(o2.getValue(), o1.getValue());
                return o1.getKey().compareTo(o2.getKey());
            }).forEach( r -> {
                /** Lleno el csv **/
                String party = r.getKey();
                DecimalFormat format = new DecimalFormat("##.00");
                String percent = format.format(r.getValue()) + "%";
                try {
                    csvPrinter.printRecord(percent, party);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.printRecord("Winner");
            csvPrinter.printRecord(results.getWinner());

            csvPrinter.flush();
        }
        catch (IOException e){
            System.err.println("Error while printing CSV file");
        }
    }

    private static void printSPAVRound(CSVPrinter csvPrinter, Map<String,Double> results, int round , String[] winners){
        try {
            csvPrinter.printRecord(APPROVAL_HEADER);
            results.entrySet().stream().sorted((o1, o2) -> {
                if (!o1.getValue().equals(o2.getValue()))
                    return Double.compare(o2.getValue(), o1.getValue());
                return o1.getKey().compareTo(o2.getKey());
            }).forEach(result -> {
                String party = result.getKey();
                DecimalFormat format = new DecimalFormat("##.00");
                String points = format.format(result.getValue());
                try {
                    csvPrinter.printRecord(points, party);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.printRecord("Winners");
            StringBuilder stringBuilder = new StringBuilder(winners[0]);
            for(int i = 1 ; i<round ; i++){
                stringBuilder.append(", ").append(winners[i]);
            }
            csvPrinter.printRecord(stringBuilder);

        }
        catch (IOException e){
            System.err.println("Error while printing CSV file");
        }


    }

    private static void printSPAV(String file, SPAV results) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file));
            final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.newFormat(';')
                    .withRecordSeparator('\n'));

            csvPrinter.printRecord("Round 1");
            printSPAVRound(csvPrinter,results.getRound1(),1, results.getWinner());
            csvPrinter.printRecord("Round 2");
            printSPAVRound(csvPrinter,results.getRound2(),2, results.getWinner());
            csvPrinter.printRecord("Round 3");
            printSPAVRound(csvPrinter,results.getRound3(),3, results.getWinner());

            csvPrinter.flush();
        }
        catch (IOException e){
            System.err.println("Error while printing CSV file");
        }
    }

/*    private static void argumentParsing() throws ArgumentException {
        // -DserverAddress=xx.xx.xx.xx:yyyy        --> host:port
        // -Dstate=stateName       (not required)  --> state
        // -Did=pollingPlaceNumber (not required)  --> type of query
        // -DoutPath=fileName                      --> fileName.csv (out)

        Properties properties = System.getProperties();

        try {
            serverAddress = ClientUtils.getInetAddress(properties.getProperty(SERVER_ADDRESS_PARAM));
        } catch (URISyntaxException e) {
            throw new ArgumentException("Server Address must be supplied using -DserverAddress and its format must be xx.xx.xx.xx:yyyy");
        }

        path = properties.getProperty(FILE_PATH_PARAM);
        if (path == null) {
            throw new ArgumentException("Path must be supplied using -DoutPath");
        }
    }*/


}