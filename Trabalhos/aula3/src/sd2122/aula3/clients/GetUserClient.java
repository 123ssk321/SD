package sd2122.aula3.clients;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import sd2122.aula3.api.util.Discovery;
import sd2122.aula3.server.UsersServer;

import java.io.IOException;
import java.net.URI;

public class GetUserClient {

    public static void main(String[] args) throws IOException {

        if( args.length != 2) {
            System.err.println( "Use: java sd2122.aula2.clients.GetUserClient userId password");
            return;
        }

        String serverUrl = URLConfigurator.getURLFor(Discovery.DISCOVERY_ADDR, UsersServer.SERVICE);
        String userId = args[0];
        String password = args[1];

        System.out.println("Sending request to server.");

        var result = new RestUsersClient(URI.create(serverUrl)).getUser(userId, password);
        System.out.println("Result: " + result);
    }

}
