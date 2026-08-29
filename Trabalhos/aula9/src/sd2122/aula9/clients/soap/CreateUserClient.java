package sd2122.aula9.clients.soap;

import java.io.IOException;
import java.net.URL;

import com.sun.xml.ws.client.BindingProviderProperties;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import sd2122.aula9.api.User;
import sd2122.aula9.api.service.soap.SoapUsers;
import sd2122.aula9.api.service.soap.UsersException;
import sd2122.aula9.tls.InsecureHostnameVerifier;

public class CreateUserClient {


	public final static String USERS_WSDL = "/users/?wsdl";
	
	public final static int MAX_RETRIES = 3;
	public final static long RETRY_PERIOD = 1000;
	public final static int CONNECTION_TIMEOUT = 1000;
	public final static int REPLY_TIMEOUT = 600;

	public static void main(String[] args) throws IOException {

		if( args.length != 5) {
			System.err.println( "Use: java sd2021.aula2.clients.CreateUserClient url userId fullName email password");
			return;
		}

		String serverUrl = args[0];
		String userId = args[1];
		String fullName = args[2];
		String email = args[3];
		String password = args[4];

		User u = new User( userId, fullName, email, password);
		
		//Obtaining s stub for the remote soap service
		
		SoapUsers users;
		
		//This allows client code executed by this process to ignore hostname verification
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());
		
		try {
			QName QNAME = new QName(SoapUsers.NAMESPACE, SoapUsers.NAME);
			Service service = Service.create( new URL(serverUrl + USERS_WSDL), QNAME );
			users = service.getPort( SoapUsers.class );
			
			((BindingProvider) users).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, CONNECTION_TIMEOUT);
			((BindingProvider) users).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, REPLY_TIMEOUT);
			
		} catch ( WebServiceException e) {
			System.err.println("Could not contact the server: " + e.getMessage());
			throw e;
		}
		
		
		System.out.println("Sending request to server.");

		
		short retries = 0;
		boolean success = false;

		String id;
		while(!success && retries < MAX_RETRIES) {

			try {
				id = users.createUser(u);
				System.out.println("Created user with id: " + id);
				success = true;
			} catch (UsersException e) {
				System.out.println("Cound not create user: " + e.getMessage());
				success = true;
			} catch (WebServiceException wse) {
				System.out.println("Communication error.");
				wse.printStackTrace();
				retries++;
				try { Thread.sleep( RETRY_PERIOD ); } catch (InterruptedException e) {
					//nothing to be done here, if this happens we will just retry sooner.
				}
				System.out.println("Retrying to execute request.");
			}
		}
	}

}