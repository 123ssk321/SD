package sd2122.aula9.servers.soap;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import jakarta.xml.ws.Endpoint;

public class SoapUsersServer {

	public static final int PORT = 8080;
	public static final String SERVICE_NAME = "users";
	public static String SERVER_BASE_URI = "https://%s:%s/soap";

	private static Logger Log = Logger.getLogger(SoapUsersServer.class.getName());

	public static void main(String[] args) throws Exception {
		

		Log.setLevel(Level.INFO);

		var ip = InetAddress.getLocalHost().getHostAddress();		
		var configurator = new HttpsConfigurator(SSLContext.getDefault());

		var server = HttpsServer.create(new InetSocketAddress(ip, PORT), 0);

		server.setHttpsConfigurator(configurator);
		
		var endpoint = Endpoint.create(new SoapUsersWebService());		
		endpoint.publish(server.createContext("/soap"));
		
		server.start();

		// Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));
	}
}
