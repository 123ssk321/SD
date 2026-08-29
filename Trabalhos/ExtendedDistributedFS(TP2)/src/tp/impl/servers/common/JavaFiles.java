package tp.impl.servers.common;

import static tp.api.service.java.Result.ErrorCode.*;
import static tp.api.service.java.Result.error;
import static tp.api.service.java.Result.ok;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import tp.api.service.java.Directory;
import tp.api.service.java.Files;
import tp.api.service.java.Result;
import tp.api.service.java.Users;
import tp.impl.servers.rest.AbstractRestServer;
import tp.impl.servers.rest.FilesRestServer;
import util.*;
import util.kafka.KafkaSubscriber;

public class JavaFiles implements Files {

	public static final String DELIMITER = "$$$";
	private static final String ROOT = "/tmp/";
	private static final String SECRETS_PROP = "FILES_EXTRA_ARGS";
	public static final int VALID_SECONDS = 10;
	private static final String FROM_BEGINNING = "earliest";


	private String usersSecret;
	private String directorySecret;


	public JavaFiles() {
		new File( ROOT ).mkdirs();
		usersSecret = Secrets.secretFrom(Users.SERVICE_NAME, SECRETS_PROP);
		directorySecret = Secrets.secretFrom(Directory.SERVICE_NAME, SECRETS_PROP);

		var subscriber = KafkaSubscriber.createSubscriber(JavaUsers.KAFKA_BROKERS, List.of(JavaUsers.DELETE_USER_TOPIC, JavaDirectory.DELETE_FILE_TOPIC), FROM_BEGINNING);

		subscriber.start(false, (r) -> {
			processRecord(r);
		});
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		if(isValid(fileId, token)) {
			fileId = fileId.replace(DELIMITER, "/");
			byte[] data = IO.read(new File(ROOT + fileId));
			return data != null ? ok(data) : error(NOT_FOUND);
		} else
			return error(BAD_REQUEST);

	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		if(isValid(fileId, token)) {
			fileId = fileId.replace(DELIMITER, "/");
			boolean res = IO.delete(new File(ROOT + fileId));
			return res ? ok() : error(NOT_FOUND);
		}else
			return error(BAD_REQUEST);
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		if(isValid(fileId, token)) {
			fileId = fileId.replace(DELIMITER, "/");
			File file = new File(ROOT + fileId);
			file.getParentFile().mkdirs();
			IO.write(file, data);
			return ok();
		}else
			return error(BAD_REQUEST);
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String token) {
		if(isValid(token)) {
			File file = new File(ROOT + userId);
			try {
				java.nio.file.Files.walk(file.toPath())
						.sorted(Comparator.reverseOrder())
						.map(Path::toFile)
						.forEach(File::delete);
			} catch (IOException e) {
				e.printStackTrace();
				return error(INTERNAL_ERROR);
			}
			return ok();
		}else
			return error(BAD_REQUEST);
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}

	private boolean isValid(String id, String token){
		String[] meta = token.split(Token.DELIMITER);
		String tkn1 = meta[0];
		LocalDateTime sentDateTime = LocalDateTime.parse(meta[1]);
		if(ChronoUnit.SECONDS.between(sentDateTime, LocalDateTime.now()) > VALID_SECONDS)
			return false;

		String tkn2 = Hash.of(id, sentDateTime, directorySecret);
		return tkn1.equals(tkn2);
	}

	private boolean isValid(String token){
		return token.equals(Hash.of(usersSecret));
	}

	private void processRecord(ConsumerRecord<String, String> r) {
		String[] args = r.value().split(JavaUsers.RECORD_DELIMITER);

		switch(r.topic()){
			case JavaUsers.DELETE_USER_TOPIC:
				deleteUserFiles(args[0], args[3]);
				break;
			case JavaDirectory.DELETE_FILE_TOPIC:
				String serverURI = String.format(AbstractRestServer.SERVER_BASE_URI, IP.hostAddress(), FilesRestServer.PORT);
				for(int i=2; i< args.length; i++){
					if(args[i].equals(serverURI)){
						deleteFile(args[0], args[1]);
					}
				}
				break;
		}
	}
}
