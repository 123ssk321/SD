package sd2122.aula2.clients;

import java.io.IOException;
import java.net.URI;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import sd2122.aula2.api.User;
import sd2122.aula2.api.service.RestUsers;
import sd2122.aula2.api.util.Discovery;
import sd2122.aula2.server.UsersServer;

public class UpdateUserClient {

	public static void main(String[] args) throws IOException {
		
		if( args.length != 5) {
			System.err.println( "Use: java sd2122.aula2.clients.UpdateUserClient userId oldpwd fullName email password");
			return;
		}

		String serverUrl = URLConfigurator.getURLFor(Discovery.DISCOVERY_ADDR, UsersServer.SERVICE, 7);
		String userId = args[0];
		String oldpwd = args[1];
		String fullName = args[2];
		String email = args[3];
		String password = args[4];
		
		User u = new User( userId, fullName, email, password);
		
		System.out.println("Sending request to server.");
		
		//TODO complete this client code
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);

		WebTarget target = client.target( serverUrl ).path( RestUsers.PATH );

		Response r = target.path( userId )
				.queryParam(RestUsers.PASSWORD, oldpwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(u, MediaType.APPLICATION_JSON));

		if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() )
			System.out.println("Success, updated user to: " + r.readEntity(String.class) );
		else
			System.out.println("Error, HTTP error status: " + r.getStatus() );

	}
	
}
