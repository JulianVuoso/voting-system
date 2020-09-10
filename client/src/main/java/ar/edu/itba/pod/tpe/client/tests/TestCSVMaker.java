package ar.edu.itba.pod.tpe.client.tests;

import ar.edu.itba.pod.tpe.client.exceptions.ArgumentException;
import ar.edu.itba.pod.tpe.client.utils.ClientUtils;
import ar.edu.itba.pod.tpe.interfaces.QueryService;
import ar.edu.itba.pod.tpe.interfaces.VotingService;
import ar.edu.itba.pod.tpe.models.*;
import org.apache.commons.cli.ParseException;
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

        FPTP result = (FPTP) service.askState("JUNGLE");

        //if(!((FPTP) service.askNational()).getPartial()){
            printFPTP("out.csv",result.getMap());
        //}

        /*try {
            //CAMBIAR ESTE POR EL QUE VIENE EN ARGS
            //CAMBIAR RESULTS POR EL QUE MANDA JV
            writeCSV("out.csv", result);
        }
        catch (IOException e){
            e.printStackTrace();
        }*/

    }

   /* private static void writeCSV(String file, Result results) throws  IOException {
        if(results){
            printFPTP(file,results);
            return;
        }
        else{
            switch (){
                case Type.FPTP:
                    printFPTP(file,results);
                    break;
                case Type.SPAV:
                    printSPAV(file,(SPAV) results);
                    break;
                case Type.STAR:
                    printSTAR(file,(STAR) results);
                    printFPTP(file,results);
                    break;
            }
        }

    }*/

    private static void printFPTP(String file, Map<String,Integer> results){
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file));
            final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.newFormat(';')
                    .withHeader(PERCENTAGE_HEADER)
                    .withRecordSeparator('\n'));

            for(String party : results.keySet()){
                System.out.println(results.get(party));
               /* DecimalFormat format = new DecimalFormat("##.00");*/
               /* String percent = format.format(results.get(party) * 100) + "%";*/
                try {
                    csvPrinter.printRecord(100,party);
                } catch (IOException e) {                                       
                    e.printStackTrace();
                }
            }
            /*results.entrySet().stream().sorted((o1, o2) -> {
                if (!o1.getValue().equals(o2.getValue()))
                    return Double.compare(o2.getValue(), o1.getValue());
                return o1.getKey().compareTo(o2.getKey());
            }).forEach( r -> {
                String party = r.getKey();
                DecimalFormat format = new DecimalFormat("##.00");
                String percent = format.format(r.getValue() * 100) + "%";
                try {
                    csvPrinter.printRecord(percent, party);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });*/
            csvPrinter.flush();
        }
        catch (IOException e){
            System.err.println("Error while printing CSV file");
        }
       /* if(!partial){
            csvPrinter.printRecord("Winner");

        }*/
    }

    private static void printSTAR(String file, STAR results) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file));
            final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.newFormat(';')
                    .withHeader(SCORE_HEADER)
                    .withRecordSeparator('\n'));

            results.getFirstRound().entrySet().stream().sorted((o1, o2) -> {
                if (!o1.getValue().equals(o2.getValue()))
                    return Double.compare(o2.getValue(), o1.getValue());
                return o1.getKey().compareTo(o2.getKey());
            }).forEach(result -> {
                String party = result.getKey();
                DecimalFormat format = new DecimalFormat("##.00");
                String percent = format.format(result.getValue() * 100) + "%";
                try {
                    csvPrinter.printRecord(percent, party);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
        }
        catch (IOException e){
            System.err.println("Error while printing CSV file");
        }
    }

    private static void printSPAV(String file, SPAV results) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(file));
            final CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.newFormat(';')
                    .withHeader(APPROVAL_HEADER)
                    .withRecordSeparator('\n'));

            results.getRound1().entrySet().stream().sorted((o1, o2) -> {
                if (!o1.getValue().equals(o2.getValue()))
                    return Double.compare(o2.getValue(), o1.getValue());
                return o1.getKey().compareTo(o2.getKey());
            }).forEach(result -> {
                String party = result.getKey();
                DecimalFormat format = new DecimalFormat("##.00");
                String percent = format.format(result.getValue() * 100) + "%";
                try {
                    csvPrinter.printRecord(percent, party);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            csvPrinter.flush();
        }
        catch (IOException e){
            System.err.println("Error while printing CSV file");
        }
    }

/*    private static void argumentParsing(String[] args) throws ArgumentException {
        // -DserverAddress=xx.xx.xx.xx:yyyy        --> host:port
        // -Dstate=stateName       (not required)  --> state
        // -Did=pollingPlaceNumber (not required)  --> type of query
        // -DoutPath=fileName                      --> fileName.csv (out)

        Properties properties;
        try {
            properties = ClientUtils.getDProperties(args);
        } catch (ParseException e) {
            throw new ArgumentException("Params format must be -Dproperty=value");
        }

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