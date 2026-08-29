package util;

import tp.impl.servers.common.JavaFiles;

import java.time.LocalDateTime;

public class Token {

	public static final String DELIMITER = ">>>";

	//fileID+data:hora+mysecret ler do trab.props
	private static String token;
	
	public static void set(String t) {
		token = t;
	}
	
	public static String get() {
		return token == null ? "" : token ;
	}
	
	public boolean matches(String t) {
		return token != null && token.equals( t );
	}

	public static String generateToken(String id, String secret){
		LocalDateTime localDateTime = LocalDateTime.now();
		return Hash.of(id, localDateTime, secret) + DELIMITER + localDateTime;
	}


}
