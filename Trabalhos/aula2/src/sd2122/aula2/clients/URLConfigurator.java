package sd2122.aula2.clients;

import sd2122.aula2.api.util.Discovery;
import sd2122.aula2.server.UsersServer;

import java.net.InetSocketAddress;
import java.net.URI;

public class URLConfigurator {

    public static String getURLFor(InetSocketAddress discoveryAddress, String serviceName, int minRepliesNeeded){
        Discovery discovery = new Discovery(discoveryAddress, null, null);

        URI[] serverUri = null;
        discovery.listener(serviceName, minRepliesNeeded);
        while (serverUri == null){
            serverUri = discovery.knownUrisOf(UsersServer.SERVICE);
        }
        return serverUri[0].toString();
    }
    
}
