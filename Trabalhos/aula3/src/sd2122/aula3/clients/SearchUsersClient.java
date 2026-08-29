package sd2122.aula3.clients;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

import sd2122.aula3.api.util.Discovery;
import sd2122.aula3.server.UsersServer;
import util.Debug;

public class SearchUsersClient {
	
	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}
	
	public static void main(String[] args) throws IOException {

		Debug.setLogLevel(Level.FINE, Debug.SD2122);
		
		if (args.length != 1) {
			System.err.println("Use: java sd2122.aula3.clients.SearchUsersClient userId ");
			return;
		}

		String serverUrl = URLConfigurator.getURLFor(Discovery.DISCOVERY_ADDR, UsersServer.SERVICE);;
		String userId = args[0];

		System.out.println("Sending request to server.");

		new RestUsersClient(URI.create(serverUrl)).searchUsers(userId);

	}

}
