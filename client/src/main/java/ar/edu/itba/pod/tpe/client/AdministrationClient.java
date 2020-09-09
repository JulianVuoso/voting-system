package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.exceptions.AdministrationException;
import ar.edu.itba.pod.tpe.interfaces.AdministrationService;
import ar.edu.itba.pod.tpe.models.Status;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.util.Properties;

public class AdministrationClient {

    private static Logger logger = LoggerFactory.getLogger(AdministrationClient.class);
    private static String SERVICE_NAME = "service";

    public static void main(String[] args) throws Exception {
        logger.info("tpe1-g6 Administration Client Starting ...");

        Option propertyOption = Option.builder()
                .longOpt("D")
                .argName("property=value")
                .valueSeparator()
                .numberOfArgs(2)
                .desc("use value for given properties")
                .build();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(new Options().addOption(propertyOption), args);

        if (!cmd.hasOption("D")) {
            System.err.println("Should enter arguments");
            return;
        }

        Properties properties = cmd.getOptionProperties("D");
        String address = properties.getProperty("serverAddress");
        String action = properties.getProperty("action");
        if (address == null || action == null) {
            System.err.println("Should run with these arguments: -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName");
            return;
        }

        final AdministrationService service = (AdministrationService) Naming.lookup(address + SERVICE_NAME);

        try {
            switch (action) {
                case "open":
                    System.out.println(service.open());
                    break;
                case "close":
                    System.out.println(service.close());
                    break;
                case "status":
                    System.out.println(service.status());
                    break;
                default:
                    System.err.println("Invalid action name, possible options: open, close, status");
            }
        } catch (AdministrationException e) {
            System.err.println("Error trying to " + action + ", " + e.getMessage());
        }
        return;
    }
}
