package sd2122.aula3.clients;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import sd2122.aula3.api.User;
import sd2122.aula3.api.util.Discovery;
import sd2122.aula3.server.UsersServer;

import java.io.IOException;
import java.net.URI;

public class UpdateUserClient {
    public static void main(String[] args) throws IOException {

        if( args.length != 5) {
            System.err.println( "Use: java sd2122.aula2.clients.UpdateUserClient userId oldpwd fullName email password");
            return;
        }

        String serverUrl = URLConfigurator.getURLFor(Discovery.DISCOVERY_ADDR, UsersServer.SERVICE);
        String userId = args[0];
        String oldpwd = args[1];
        String fullName = args[2];
        String email = args[3];
        String password = args[4];

        User u = new User( userId, fullName, email, password);

        System.out.println("Sending request to server.");

        //TODO complete this client code
        var result = new RestUsersClient(URI.create(serverUrl)).updateUser(userId, oldpwd, u);
        System.out.println("Result: " + result);
    }

}
