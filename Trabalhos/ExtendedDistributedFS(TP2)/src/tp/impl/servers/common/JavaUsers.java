package tp.impl.servers.common;

import static tp.api.service.java.Result.error;
import static tp.api.service.java.Result.ok;
import static tp.api.service.java.Result.ErrorCode.BAD_REQUEST;
import static tp.api.service.java.Result.ErrorCode.CONFLICT;
import static tp.api.service.java.Result.ErrorCode.FORBIDDEN;
import static tp.api.service.java.Result.ErrorCode.NOT_FOUND;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tp.api.User;
import tp.api.service.java.Directory;
import tp.api.service.java.Files;
import tp.api.service.java.Result;
import tp.api.service.java.Users;
import util.Hash;
import util.Secrets;
import util.kafka.KafkaPublisher;

public class JavaUsers implements Users {
	public static final String RECORD_DELIMITER = "###";
	static final String DELETE_USER_TOPIC = "DELETE_USER_FILES";
	static final String KAFKA_BROKERS = "kafka:9092";
	private static final String SECRETS_PROP = "USERS_EXTRA_ARGS";


	private static final String filesSecret = Secrets.secretFrom(Files.SERVICE_NAME, SECRETS_PROP);
	private static final String directorySecret = Secrets.secretFrom(Directory.SERVICE_NAME, SECRETS_PROP);

	final protected Map<String, User> users = new ConcurrentHashMap<>();
	final ExecutorService executor = Executors.newCachedThreadPool();

	private final KafkaPublisher publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);

	@Override
	public Result<String> createUser(User user) {
		if( badUser(user ))
			return error( BAD_REQUEST );

		var userId = user.getUserId();
		var res = users.putIfAbsent(userId, user);

		if (res != null)
			return error(CONFLICT);
		else
			return ok(userId);
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		if (badParam(userId) )
			return error(BAD_REQUEST);

		var user = users.get(userId);

		if (user == null)
			return error(NOT_FOUND);

		if (badParam(password) || wrongPassword(user, password))
			return error(FORBIDDEN);
		else
			return ok(user);
	}

	@Override
	public Result<User> updateUser(String userId, String password, User data) {

		var user = users.get(userId);

		if (user == null)
			return error(NOT_FOUND);

		if (badParam(password) || wrongPassword(user, password))
			return error(FORBIDDEN);
		else {
			user.updateUser(data);
			return ok(user);
		}
	}

	@Override
	public Result<User> deleteUser(String userId, String password) {

		var user = users.get(userId);

		if (user == null)
			return error(NOT_FOUND);

		if (badParam(password) || wrongPassword(user, password))
			return error(FORBIDDEN);
		else {
			users.remove(userId);

			publisher.publish(DELETE_USER_TOPIC, userId
							+RECORD_DELIMITER+password
							+RECORD_DELIMITER+Hash.of(directorySecret)
							+RECORD_DELIMITER+Hash.of(filesSecret));

			return ok(user);
		}
	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		if( badParam( pattern))
			return error(BAD_REQUEST);

		var hits = users.values()
				.stream()
				.filter( u -> u.getFullName().toLowerCase().contains(pattern.toLowerCase()) )
				.map( User::secureCopy )
				.toList();

		return ok(hits);
	}

	private boolean badParam( String str ) {
		return str == null;
	}

	private boolean badUser( User user ) {
		return user == null || badParam(user.getEmail()) || badParam(user.getFullName()) || badParam( user.getPassword());
	}

	private boolean wrongPassword(User user, String password) {
		return !user.getPassword().equals(password);
	}
}
