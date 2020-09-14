/*
package ar.edu.itba.pod.tpe.client.tests;

import ar.edu.itba.pod.tpe.interfaces.InspectionService;
import ar.edu.itba.pod.tpe.stub.InspectionVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class TestInspectorClient {
    private static Logger logger = LoggerFactory.getLogger(TestInspectorClient.class);

    public static void main(String[] args) throws RemoteException, NotBoundException {
        logger.info("tpe1-g6 TestInspectorClient Starting ...");

        final Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1099);
        final InspectionService service = (InspectionService) registry.lookup("inspection-service");

        final Scanner scanner = new Scanner(System.in);
        String[] split;
        do {
            System.out.println("Enter table-province-FPTP Vote");
            split = scanner.nextLine().split("-");
            if (split.length >= 3) {
                InspectionVote vote = new InspectionVote(Integer.valueOf(split[0]), split[1], split[2]);
                service.vote(vote);
            } else {
                System.out.println("Invalid entry or exit");
                split = new String[0];
            }
        } while (split.length > 0 && !split[0].equals("exit"));

        System.out.println("Sending finish to election");
//        service.finishElection(); TODO fix here
    }
}
*/
