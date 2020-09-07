package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.interfaces.AdministrationService;
import com.sun.xml.internal.ws.api.config.management.policy.ManagementAssertion;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.Naming;
import java.util.Properties;

public class AdministrationClient {
    private static Logger logger = LoggerFactory.getLogger(AdministrationClient.class);


//  ./run-management -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName
    public static void main(String[] args) throws Exception {
        logger.info("tpe1-g6 Administration Client Starting ...");

        parseArguments(args);




        //final AdministrationService service = (AdministrationService) Naming.lookup("//localhost:1099/service");

    }

    private static void parseArguments(String[] args) throws Exception {
        Options options = new Options();

        Option propertyOption   = Option.builder()
                .longOpt("D")
                .argName("property=value" )
                .hasArgs()
                .valueSeparator()
                .numberOfArgs(2)
                .desc("use value for given properties" )
                .build();

        options.addOption(propertyOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if(cmd.hasOption("D")) {
            Properties properties = cmd.getOptionProperties("D");
            System.out.println("Server Address: " + properties.getProperty("serverAddress"));
            System.out.println("Action: " + properties.getProperty("action"));
        }
    }
}
