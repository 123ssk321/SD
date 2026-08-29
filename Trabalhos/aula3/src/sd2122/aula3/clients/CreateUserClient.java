package sd2122.aula3.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import sd2122.aula3.api.User;
import sd2122.aula3.api.util.Discovery;
import sd2122.aula3.server.UsersServer;
import util.Debug;

public class CreateUserClient {
	
	private static Logger Log = Logger.getLogger(CreateUserClient.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}
	
	public static void main(String[] args) throws IOException {
		
		Debug.setLogLevel( Level.FINE, Debug.SD2122 );
		
		if (args.length != 4) {
			System.err.println("Use: java sd2122.aula3.clients.CreateUserClient userId fullName email password");
			return;
		}

		String serverUrl = URLConfigurator.getURLFor(Discovery.DISCOVERY_ADDR, UsersServer.SERVICE);
		String userId = args[0];
		String fullName = args[1];
		String email = args[2];
		String password = args[3];

		User u = new User(userId, fullName, email, password);

		Log.info("Sending request to server.");

		var result = new RestUsersClient(URI.create(serverUrl)).createUser(u);
		System.out.println("Result: " + result);
	}

}
