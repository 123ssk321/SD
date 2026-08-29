package tp.impl.servers.common;

import static tp.api.service.java.Result.ErrorCode.*;
import static tp.api.service.java.Result.error;
import static tp.api.service.java.Result.ok;
import static tp.api.service.java.Result.redirect;
import static tp.impl.clients.Clients.FilesClients;
import static tp.impl.clients.Clients.UsersClients;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import tp.api.FileInfo;
import tp.api.User;
import tp.api.service.java.Directory;
import tp.api.service.java.Files;
import tp.api.service.java.Result;
import tp.api.service.java.Result.ErrorCode;
import util.Hash;
import util.Secrets;
import util.Token;
import util.kafka.KafkaPublisher;
import util.kafka.KafkaSubscriber;


public class JavaDirectory implements Directory {

	private static final String SECRETS_PROP = "DIR_EXTRA_ARGS";
	static final String DELETE_FILE_TOPIC = "DELETE_FILE";
	private static final String FROM_BEGINNING = "earliest";
	static final long USER_CACHE_EXPIRATION = 3000;

	final LoadingCache<UserInfo, Result<User>> users = CacheBuilder.newBuilder()
			.expireAfterWrite( Duration.ofMillis(USER_CACHE_EXPIRATION))
			.build(new CacheLoader<>() {
				@Override
				public Result<User> load(UserInfo info) throws Exception {
					var res = UsersClients.get().getUser( info.userId(), info.password());
					if( res.error() == ErrorCode.TIMEOUT)
						return error(BAD_REQUEST);
					else
						return res;
				}
			});

	final static Logger Log = Logger.getLogger(JavaDirectory.class.getName());

	final Map<String, ExtendedFileInfo> files = new ConcurrentHashMap<>();
	final Map<String, UserFiles> userFiles = new ConcurrentHashMap<>();
	final Map<URI, FileCounts> fileCounts = new ConcurrentHashMap<>();

	private static String filesSecret = Secrets.secretFrom(Files.SERVICE_NAME, SECRETS_PROP);
	private static String usersSecret = Secrets.secretFrom(JavaUsers.SERVICE_NAME, SECRETS_PROP);

	private final KafkaPublisher publisher;

	public JavaDirectory(){
		var subscriber = KafkaSubscriber.createSubscriber(JavaUsers.KAFKA_BROKERS, List.of(JavaUsers.DELETE_USER_TOPIC), FROM_BEGINNING);

		subscriber.start(false, (r) -> {
			processDeleteUserRecord(r);
		});

		publisher = KafkaPublisher.createPublisher(JavaUsers.KAFKA_BROKERS);
	}

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {

		if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var user = getUser(userId, password, this.users);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userId, (k) -> new UserFiles());
		synchronized(uf) {
			var fileId = fileId(filename, userId);
			var file = files.get(fileId);
			var info = file != null ? file.info() : new FileInfo();
			var candidateFileServers = orderCandidateFileServers(file, fileCounts);
			int numCandidateFileServers = candidateFileServers.size();
			Queue<String> uris = new LinkedList<>();
			while (uris.size() < 2 && !candidateFileServers.isEmpty()){
				var uri = candidateFileServers.remove();
				var result = FilesClients.get(uri).writeFile(fileId, data, generateTokenForFiles(fileId));
				if (result.isOK()) {
					String fileURL = String.format("%s/files/%s", uri, fileId);
					if(uris.isEmpty()){
						info.setOwner(userId);
						info.setFilename(filename);
						info.setFileURL(fileURL);
					}
					if( uf.owned().add(fileId))
						getFileCounts(uri, true, this.fileCounts).numFiles().incrementAndGet();
					uris.add(fileURL);
				} else {
					Log.info(String.format("Files.writeFile(...) to %s failed with: %s \n", uri, result));
				}
			}
			int numURIs = uris.size();

			if((numCandidateFileServers >= 2  && numURIs == 2) || (numURIs == 1 && numCandidateFileServers == numURIs)){
				files.put(fileId, file = new ExtendedFileInfo(uris, fileId, info));
				Log.info(String.format("File written in: %s \n", uris));
				return ok(file.info());
			}

			return error(BAD_REQUEST);
		}
	}


	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {
		if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null)
			return error(NOT_FOUND);

		var user = getUser(userId, password, this.users);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.getOrDefault(userId, new UserFiles());
		synchronized (uf) {
			var info = files.remove(fileId);
			uf.owned().remove(fileId);
			this.removeSharesOfFile(info, this.userFiles);

			String kafkaMsg = fileId + JavaUsers.RECORD_DELIMITER + generateTokenForFiles(fileId);
			for(String fileURL : file.uris()){
				getFileCounts(serverURIFrom(fileURL), false, this.fileCounts).numFiles().decrementAndGet();
				kafkaMsg += JavaUsers.RECORD_DELIMITER + serverURIFrom(fileURL);
			}

			publisher.publish(DELETE_FILE_TOPIC, kafkaMsg);
		}
		return ok();
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "", this.users).error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password, this.users);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().add(fileId);
			file.info().getSharedWith().add(userIdShare);
		}

		return ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "", this.users).error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password, this.users);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().remove(fileId);
			file.info().getSharedWith().remove(userIdShare);
		}

		return ok();
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
		if (badParam(filename))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);
		var file = files.get(fileId);
		if (file == null)
			return error(NOT_FOUND);

		var user = getUser(accUserId, password, this.users);
		if (!user.isOK())
			return error(user.error());

		if (!file.info().hasAccess(accUserId))
			return error(FORBIDDEN);

		return redirect( file.uris() );
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		if (badParam(userId))
			return error(BAD_REQUEST);

		var user = getUser(userId, password, this.users);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.getOrDefault(userId, new UserFiles());
		synchronized (uf) {
			var infos = Stream.concat(uf.owned().stream(), uf.shared().stream()).map(f -> files.get(f).info())
					.collect(Collectors.toSet());

			return ok(new ArrayList<>(infos));
		}
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String password, String token) {
		if(isValid(token, usersSecret)) {
			users.invalidate(new UserInfo(userId, password));

			var fileIds = userFiles.remove(userId);
			if (fileIds != null)
				for (var id : fileIds.owned()) {
					var file = files.remove(id);
					removeSharesOfFile(file, this.userFiles);
					for (String fileURL : file.uris()) {
						getFileCounts(serverURIFrom(fileURL), false, this.fileCounts).numFiles().decrementAndGet();
					}
				}
			return ok();
		}else
			return error(BAD_REQUEST);
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}

	protected static boolean badParam(String str) {
		return str == null || str.length() == 0;
	}

	protected static Result<User> getUser(String userId, String password,  LoadingCache<UserInfo, Result<User>> users) {
		try {
			return users.get(new UserInfo(userId, password));
		} catch (Exception x) {
			x.printStackTrace();
			return error(ErrorCode.INTERNAL_ERROR);
		}
	}

	public static String generateTokenForFiles(String fileId){
		return Token.generateToken(fileId, filesSecret);
	}

	protected static boolean isValid(String token, String usersSecret){
		return token.equals(Hash.of(usersSecret));
	}

	protected static void removeSharesOfFile(ExtendedFileInfo file, Map<String, UserFiles> userFiles) {
		for (var userId : file.info().getSharedWith())
			userFiles.getOrDefault(userId, new UserFiles()).shared().remove(file.fileId());
	}

	protected static Queue<URI> orderCandidateFileServers(ExtendedFileInfo file, Map<URI, FileCounts> fileCountsByURI) {
		int MAX_SIZE=3;
		Queue<URI> result = new ArrayDeque<>();

		if( file != null )
			result.addAll(file.uris().stream().map(JavaDirectory::serverURIFrom).toList());

		FilesClients.all()
				.stream()
				.filter( u -> ! result.contains(u))
				.map(u -> getFileCounts(u, false, fileCountsByURI))
				.sorted( FileCounts::ascending )
				.map(FileCounts::uri)
				.limit(MAX_SIZE)
				.forEach( result::add );

		Log.info("Candidate files servers: " + result+ "\n");
		return result;
	}

	protected static FileCounts getFileCounts( URI uri, boolean create, Map<URI, JavaDirectory.FileCounts> fileCounts) {
		if( create )
			return fileCounts.computeIfAbsent(uri,  FileCounts::new);
		else
			return fileCounts.getOrDefault( uri, new FileCounts(uri) );
	}

	protected static URI serverURIFrom(String fileURL){
		var i = fileURL.indexOf(Files.SERVICE_NAME);
		return URI.create( fileURL.substring(0, i-1) );
	}

	private void processDeleteUserRecord(ConsumerRecord<String, String> r) {
		String[] args = r.value().split(JavaUsers.RECORD_DELIMITER);

		deleteUserFiles(args[0], args[1], args[2]);
	}

	static record ExtendedFileInfo(Queue<String> uris, String fileId, FileInfo info) {
	}

	static record UserFiles(Set<String> owned, Set<String> shared) {

		UserFiles() {
			this(ConcurrentHashMap.newKeySet(), ConcurrentHashMap.newKeySet());
		}
	}

	static record FileCounts(URI uri, AtomicLong numFiles) {
		FileCounts( URI uri) {
			this(uri, new AtomicLong(0L) );
		}

		static int ascending(FileCounts a, FileCounts b) {
			return Long.compare( a.numFiles().get(), b.numFiles().get());
		}
	}

	static record UserInfo(String userId, String password) {
	}
}