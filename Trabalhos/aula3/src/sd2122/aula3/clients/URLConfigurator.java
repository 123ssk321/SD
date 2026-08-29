package sd2122.aula3.clients;

import sd2122.aula3.api.util.Discovery;
import sd2122.aula3.server.UsersServer;

import java.net.InetSocketAddress;
import java.net.URI;

public class URLConfigurator {

    public static String getURLFor(InetSocketAddress discoveryAddress, String serviceName){
        Discovery discovery = new Discovery(discoveryAddress, null, null);

        URI[] serverUri = null;
        discovery.listener(serviceName);
        while (serverUri == null){
            serverUri = discovery.knownUrisOf(UsersServer.SERVICE);
        }
        return serverUri[0].toString();
    }

}
