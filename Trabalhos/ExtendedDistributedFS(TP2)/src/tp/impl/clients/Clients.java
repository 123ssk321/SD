package tp.impl.clients;

import tp.api.service.java.Directory;
import tp.api.service.java.Files;
import tp.api.service.java.Users;
import tp.impl.clients.common.RetryDirectoryClient;
import tp.impl.clients.common.RetryFilesClient;
import tp.impl.clients.common.RetryUsersClient;
import tp.impl.clients.rest.RestDirectoryClient;
import tp.impl.clients.rest.RestFilesClient;
import tp.impl.clients.rest.RestUsersClient;
import tp.impl.clients.soap.SoapDirectoryClient;
import tp.impl.clients.soap.SoapFilesClient;
import tp.impl.clients.soap.SoapUsersClient;

public class Clients {

	public static final ClientFactory<Directory> DirectoryClients = new ClientFactory<>(
			Directory.SERVICE_NAME, 
			(u) -> new RetryDirectoryClient( new RestDirectoryClient(u)), 
			(u) -> new RetryDirectoryClient( new SoapDirectoryClient(u))
	);

	public static final ClientFactory<Files> FilesClients = new ClientFactory<>(
			Files.SERVICE_NAME, 
			(u) -> new RetryFilesClient(new RestFilesClient(u)),
			(u) -> new RetryFilesClient(new SoapFilesClient(u))
	);

	public static final ClientFactory<Users> UsersClients = new ClientFactory<>(
			Users.SERVICE_NAME, 
			(u) -> new RetryUsersClient(new RestUsersClient(u)),
			(u) -> new RetryUsersClient(new SoapUsersClient(u))
	);
}
