package tp1.servers.directory.soap;

import jakarta.xml.ws.Endpoint;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import tp1.api.util.Discovery;
import tp1.servers.files.soap.SoapFilesServer;
import tp1.servers.users.soap.SoapUsersServer;


public class SoapDirectoryServer {

    public static final int PORT = 8080;
    public static final String SERVICE_NAME = "directory";
    public static String SERVER_BASE_URI = "http://%s:%s/soap";

    private static final Logger Log = Logger.getLogger(SoapDirectoryServer.class.getName());

    public static void main(String[] args) throws Exception {
        System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
        System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");

        Log.setLevel(Level.INFO);

        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(SERVER_BASE_URI, ip, PORT);
        Discovery discovery = new Discovery(Discovery.DISCOVERY_ADDR, SERVICE_NAME, serverURI);
        discovery.announce(SERVICE_NAME, serverURI);
        discovery.listener(SoapFilesServer.SERVICE_NAME);
        discovery.listener(SoapUsersServer.SERVICE_NAME);

        Endpoint.publish(serverURI.replace(ip, "0.0.0.0"), new SoapDirectoryWebService(discovery));

        Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));
    }

}
