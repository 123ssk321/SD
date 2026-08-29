package tp1.servers.users.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import tp1.api.util.Discovery;
import tp1.servers.directory.rest.DirectoryServer;
import tp1.servers.util.CustomLoggingFilter;
import tp1.servers.util.GenericExceptionMapper;
import tp1.servers.users.rest.resources.UsersResource;
import util.Debug;

public class UsersServer {

	private static final Logger Log = Logger.getLogger(UsersServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
	}
	
	public static final int PORT = 8080;
	public static final String SERVICE = "users";
	private static final String SERVER_URI_FMT = "http://%s:%s/rest";
	
	public static void main(String[] args) {
		try {
			Debug.setLogLevel( Level.INFO, Debug.SD2122 );

			String ip = InetAddress.getLocalHost().getHostAddress();
			String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
			Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
			discovery.announce(SERVICE, serverURI);
			discovery.listener(DirectoryServer.SERVICE);

			ResourceConfig config = new ResourceConfig();
			config.register(new UsersResource(discovery));
			config.register(CustomLoggingFilter.class);
			config.register(GenericExceptionMapper.class);


			JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);
	
			Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));
		
			//More code can be executed here...
		} catch( Exception e) {
			Log.severe(e.getMessage());
		}
	}

}
