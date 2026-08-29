package sd2122.aula9.servers.rest;



import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import sd2122.aula9.tls.InsecureHostnameVerifier;



public class RestUsersServer {

	private static Logger Log = Logger.getLogger(RestUsersServer.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	public static final String SERVICE = "UsersService";
	
	public static final int PORT = 8080;
	private static final CharSequence INADDR_ANY = "0.0.0.0";
	private static final String SERVER_URI_FMT = "https://%s:%s/rest";
	
	public static void main(String[] args) {
		try {
			
		ResourceConfig config = new ResourceConfig();
		config.register(UsersResource.class);

		String ip = InetAddress.getLocalHost().getHostAddress();
		String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
		//This allows client code executed by this server to ignore hostname verification
			
		
		JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config, SSLContext.getDefault());
			
		Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));

		
		//More code can be executed here...
		} catch( Exception e) {
			e.printStackTrace();
		}
	}	
}
