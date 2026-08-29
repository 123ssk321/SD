package tp1.servers.directory.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import tp1.api.util.Discovery;

import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import tp1.servers.directory.rest.resources.DirectoryResource;
import tp1.servers.files.rest.FilesServer;
import tp1.servers.users.rest.UsersServer;
import util.Debug;


public class DirectoryServer {

    private static final Logger Log = Logger.getLogger(DirectoryServer.class.getName());

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public static final int PORT = 8080;
    public static final String SERVICE = "directory";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {
            Debug.setLogLevel( Level.INFO, Debug.SD2122 );

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE, serverURI);
            discovery.announce(SERVICE, serverURI);
            discovery.listener(UsersServer.SERVICE);
            discovery.listener(FilesServer.SERVICE);

            ResourceConfig config = new ResourceConfig();
            config.register(new DirectoryResource(discovery));
            config.register(tp1.servers.util.CustomLoggingFilter.class);
            config.register(tp1.servers.util.GenericExceptionMapper.class);

            JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);

            Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));

            //More code can be executed here...
        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
    }

}
