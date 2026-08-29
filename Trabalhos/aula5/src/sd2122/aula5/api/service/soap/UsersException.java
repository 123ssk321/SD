package sd2122.aula5.api.service.soap;

import jakarta.xml.ws.WebFault;

@WebFault
public class UsersException extends Exception {


	public UsersException() {
		super("");
	}

	public UsersException(String errorMessage ) {
		super(errorMessage);
	}
	
	private static final long serialVersionUID = 1L;
}
