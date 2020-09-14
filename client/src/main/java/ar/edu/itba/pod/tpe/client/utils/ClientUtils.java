package ar.edu.itba.pod.tpe.client.utils;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class ClientUtils {
    public static InetSocketAddress getInetAddress(String hostPort) throws URISyntaxException {
        if (hostPort == null) throw new URISyntaxException("", "URI can't be null");
        URI uri = new URI("my://" + hostPort);
        if (uri.getHost() == null || uri.getPort() == -1) {
            throw new URISyntaxException(uri.toString(), "URI must have host and port parts");
        }
        return new InetSocketAddress(uri.getHost(), uri.getPort());
    }
}
