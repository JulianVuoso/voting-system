package ar.edu.itba.pod.tpe.client;

import org.apache.commons.cli.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class ClientUtils {
    public static Properties getDProperties(String[] args) throws ParseException {

        Option propertyOption = Option.builder()
                .longOpt("D")
                .argName("property=value")
                .valueSeparator()
                .numberOfArgs(2)
                .desc("use value for given properties")
                .build();

        Options options = new Options().addOption(propertyOption);
        CommandLineParser argsParser = new DefaultParser();
        CommandLine cmd = argsParser.parse(options, args);
        return cmd.getOptionProperties("D");
    }

    public static InetSocketAddress getInetAddress(String hostPort) throws URISyntaxException {
        if (hostPort == null) throw new URISyntaxException("", "URI can't be null");
        URI uri = new URI("my://" + hostPort);
        if (uri.getHost() == null || uri.getPort() == -1) {
            throw new URISyntaxException(uri.toString(), "URI must have host and port parts");
        }
        return new InetSocketAddress(uri.getHost(), uri.getPort());
    }
}
