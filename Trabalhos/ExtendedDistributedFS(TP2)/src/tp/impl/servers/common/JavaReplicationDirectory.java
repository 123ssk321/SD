package tp.impl.servers.common;

import static tp.api.service.java.Result.ErrorCode.*;
import static tp.api.service.java.Result.error;
import static tp.api.service.java.Result.ok;
import static tp.api.service.java.Result.redirect;
import static tp.impl.clients.Clients.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
import util.Secrets;
import util.Token;
import util.kafka.KafkaPublisher;
import util.kafka.KafkaSubscriber;
import util.kafka.sync.SyncPoint;
import util.replication.Operation;
import util.replication.ReplicationManager;
import util.replication.operations.*;

public class JavaReplicationDirectory implements Directory {

    private static final String SECRETS_PROP = "DIR_REP_EXTRA_ARGS_FIRST";
    private static final String FROM_BEGINNING = "earliest";

    static final long USER_CACHE_EXPIRATION = 3000;
    static final String DELETE_FILE_TOPIC = "DELETE_FILE";
    static final String EXECUTE_OPERATION_TOPIC = "EXECUTE_OP";

    public static final String WRITE_FILE_OP = "WRITE_FILE_OP";
    public static final String DELETE_FILE_OP = "DELETE_FILE_OP";
    public static final String SHARE_FILE_OP = "SHARE_FILE_OP";
    public static final String UNSHARE_FILE_OP = "UNSHARE_FILE_OP";
    public static final String DELETE_USER_OP = "DELETE_USER_OP";


    final LoadingCache<JavaDirectory.UserInfo, Result<User>> users = CacheBuilder.newBuilder()
            .expireAfterWrite( Duration.ofMillis(USER_CACHE_EXPIRATION))
            .build(new CacheLoader<>() {
                @Override
                public Result<User> load(JavaDirectory.UserInfo info) throws Exception {
                    var res = UsersClients.get().getUser( info.userId(), info.password());
                    if( res.error() == ErrorCode.TIMEOUT)
                        return error(BAD_REQUEST);
                    else
                        return res;
                }
            });

    static final  Logger Log = Logger.getLogger(JavaReplicationDirectory.class.getName());

    final Map<String, JavaDirectory.ExtendedFileInfo> files = new ConcurrentHashMap<>();
    final Map<String, JavaDirectory.UserFiles> userFiles = new ConcurrentHashMap<>();
    final Map<URI, JavaDirectory.FileCounts> fileCounts = new ConcurrentHashMap<>();

    private static final String filesSecret = Secrets.secretFrom(Files.SERVICE_NAME, SECRETS_PROP);
    private static final String usersSecret = Secrets.secretFrom(JavaUsers.SERVICE_NAME, SECRETS_PROP);

    private final KafkaPublisher publisher;

    private final SyncPoint<ErrorCode> sync;
    private final ReplicationManager repManager;

    public JavaReplicationDirectory(SyncPoint<ErrorCode> sync, ReplicationManager repManager){
        var subscriber = KafkaSubscriber.createSubscriber(JavaUsers.KAFKA_BROKERS, List.of(JavaUsers.DELETE_USER_TOPIC, EXECUTE_OPERATION_TOPIC), FROM_BEGINNING);

        subscriber.start(false, (r) -> {
            processRecord(r);
        });

        publisher = KafkaPublisher.createPublisher(JavaUsers.KAFKA_BROKERS);
        this.sync = sync;
        this.repManager = repManager;
    }

    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        if (JavaDirectory.badParam(filename) || JavaDirectory.badParam(userId))
            return error(BAD_REQUEST);

        var user = JavaDirectory.getUser(userId, password, this.users);
        if (!user.isOK())
            return error(user.error());

        int numCandidateFileServers;
        Queue<String> uris = new LinkedList<>();
        FileInfo info;
        synchronized(files) {
            var fileId = JavaDirectory.fileId(filename, userId);
            var file = files.get(fileId);
            info = file != null ? file.info() : new FileInfo();
            var candidateFileServers = JavaDirectory.orderCandidateFileServers(file, this.fileCounts);
            numCandidateFileServers = candidateFileServers.size();
            while (uris.size() < 2 && !candidateFileServers.isEmpty()) {
                var uri = candidateFileServers.remove();
                var result = FilesClients.get(uri).writeFile(fileId, data, generateTokenForFiles(fileId));
                if (result.isOK()) {
                    String fileURL = String.format("%s/files/%s", uri, fileId);
                    if(uris.isEmpty()){
                        info.setOwner(userId);
                        info.setFilename(filename);
                        info.setFileURL(fileURL);
                    }
                    uris.add(String.format("%s/files/%s", uri, fileId));
                } else {
                    Log.info(String.format("Files.writeFile(...) to %s failed with: %s \n", uri, result));
                }
            }
        }
        int numURIs = uris.size();

        if((numCandidateFileServers >= 2  && numURIs == 2) || (numURIs == 1 && numCandidateFileServers == numURIs)){
            var writeFile = new WriteFile(userId, filename, uris);
            Log.info("Publishing event: " + writeFile);
            var version = publisher.publish(EXECUTE_OPERATION_TOPIC, "rep", writeFile.toString());
            sync.waitForResult(version);
            return ok(info);
        }
        return error(BAD_REQUEST);
    }


    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        if (JavaDirectory.badParam(filename) || JavaDirectory.badParam(userId))
            return error(BAD_REQUEST);

        var fileId = JavaDirectory.fileId(filename, userId);

        var file = files.get(fileId);
        if (file == null)
            return error(NOT_FOUND);

        var user = JavaDirectory.getUser(userId, password, this.users);
        if (!user.isOK())
            return error(user.error());

        var deleteFile = new DeleteFile(userId, fileId);
        Log.info("Publishing event: " + deleteFile);
        var version = publisher.publish(EXECUTE_OPERATION_TOPIC, "rep", deleteFile.toString());
        sync.waitForResult(version);

        String kafkaMsg = fileId + JavaUsers.RECORD_DELIMITER + generateTokenForFiles(fileId);
        for(String fileURL : file.uris()){
            kafkaMsg += JavaUsers.RECORD_DELIMITER + JavaDirectory.serverURIFrom(fileURL);
        }
        publisher.publish(DELETE_FILE_TOPIC, kafkaMsg);

        return ok();
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        if (JavaDirectory.badParam(filename) || JavaDirectory.badParam(userId) || JavaDirectory.badParam(userIdShare))
            return error(BAD_REQUEST);

        var fileId = JavaDirectory.fileId(filename, userId);
        var file = files.get(fileId);
        if (file == null || JavaDirectory.getUser(userIdShare, "", this.users).error() == NOT_FOUND)
            return error(NOT_FOUND);

        var user = JavaDirectory.getUser(userId, password, this.users);
        if (!user.isOK())
            return error(user.error());

        var shareFile = new ShareFile(fileId, userIdShare);
        Log.info("Publishing event: " + shareFile);
        var version = publisher.publish(EXECUTE_OPERATION_TOPIC, "rep", shareFile.toString());
        sync.waitForResult(version);

        return ok();
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
        if (JavaDirectory.badParam(filename) || JavaDirectory.badParam(userId) || JavaDirectory.badParam(userIdShare))
            return error(BAD_REQUEST);

        var fileId = JavaDirectory.fileId(filename, userId);

        var file = files.get(fileId);
        if (file == null || JavaDirectory.getUser(userIdShare, "", this.users).error() == NOT_FOUND)
            return error(NOT_FOUND);

        var user = JavaDirectory.getUser(userId, password, this.users);
        if (!user.isOK())
            return error(user.error());

        var unshareFile = new UnshareFile(fileId, userIdShare);
        Log.info("Publishing event: " + unshareFile);
        var version = publisher.publish(EXECUTE_OPERATION_TOPIC, "rep", unshareFile.toString());
        sync.waitForResult(version);

        return ok();
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
        if (JavaDirectory.badParam(filename))
            return error(BAD_REQUEST);

        var fileId = JavaDirectory.fileId(filename, userId);
        var file = files.get(fileId);
        if (file == null)
            return error(NOT_FOUND);

        var user = JavaDirectory.getUser(accUserId, password, this.users);
        if (!user.isOK())
            return error(user.error());

        if (!file.info().hasAccess(accUserId))
            return error(FORBIDDEN);

        return redirect( file.uris() );
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
        if (JavaDirectory.badParam(userId))
            return error(BAD_REQUEST);

        var user = JavaDirectory.getUser(userId, password, this.users);
        if (!user.isOK())
            return error(user.error());

        var uf = userFiles.getOrDefault(userId, new JavaDirectory.UserFiles());
        synchronized (uf) {
            var infos = Stream.concat(uf.owned().stream(), uf.shared().stream()).map(f -> files.get(f).info())
                    .collect(Collectors.toSet());

            return ok(new ArrayList<>(infos));
        }
    }

    @Override
    public Result<Void> deleteUserFiles(String userId, String password, String token) {
        if(JavaDirectory.isValid(token, usersSecret)) {
            users.invalidate(new JavaDirectory.UserInfo(userId, password));

            var fileIds = userFiles.remove(userId);
            if (fileIds != null) {
                for (var id : fileIds.owned()) {
                    var file = files.remove(id);
                    JavaDirectory.removeSharesOfFile(file, this.userFiles);
                    for (String fileURL : file.uris()) {
                        JavaDirectory.getFileCounts(JavaDirectory.serverURIFrom(fileURL), false, this.fileCounts).numFiles().decrementAndGet();
                    }
                }
            }

            return ok();
        } else
            return error(BAD_REQUEST);
    }

    public static String generateTokenForFiles(String fileId){
        return Token.generateToken(fileId, filesSecret);
    }

    private void processRecord(ConsumerRecord<String, String> r) {
        String[] args = r.value().split(JavaUsers.RECORD_DELIMITER);

        switch(r.topic()){
            case JavaUsers.DELETE_USER_TOPIC:
                deleteUserFiles(args[0], args[1], args[2]);
                repManager.setCurrentVersion(r.offset());
                break;
            case EXECUTE_OPERATION_TOPIC:
                executeOperation(r.offset(), args[0], args[1]);
                break;
        }
    }

    private void executeOperation(long version, String op, String opValue){
        switch (op){
            case WRITE_FILE_OP:
                var writeFile = WriteFile.getOperationFrom(opValue);
                writeFileOp(version, writeFile.getUserId(), writeFile.getFilename(), writeFile.getFileURLs());
                break;
            case DELETE_FILE_OP:
                var deleteFile = DeleteFile.getOperationFrom(opValue);
                deleteFileOp(version, deleteFile.getUserId(), deleteFile.getFileId());
                break;
            case SHARE_FILE_OP:
                var shareFile = ShareFile.getOperationFrom(opValue);
                shareFileOp(version, shareFile.getFileId(), shareFile.getUserIdShare());
                break;
            case UNSHARE_FILE_OP:
                var unshareFile = UnshareFile.getOperationFrom(opValue);
                unshareFileOp(version, unshareFile.getFileId(), unshareFile.getUserIdShare());
                break;
        }
    }

    private void writeFileOp(long version, String userId, String filename, Queue<String> fileURLs){
        Log.info("Executing operation: WriteFile version = " + version);

        var uf = userFiles.computeIfAbsent(userId, (k) -> new JavaDirectory.UserFiles());
        boolean first = true;
        synchronized (uf) {
            String fileId = JavaDirectory.fileId(filename, userId);
            var file = files.get(fileId);
            var info = file != null ? file.info() : new FileInfo();
            for(String fileURL : fileURLs){
                if(first){
                    info.setOwner(userId);
                    info.setFilename(filename);
                    info.setFileURL(fileURL);
                }
                JavaDirectory.getFileCounts(JavaDirectory.serverURIFrom(fileURL), true, this.fileCounts).numFiles().incrementAndGet();
                first = false;
            }
            uf.owned().add(fileId);
            file = new JavaDirectory.ExtendedFileInfo(fileURLs, fileId, info);
            files.put(fileId, file);
        }
        repManager.setCurrentVersion(version);
        sync.setResult(version, OK);
    }

    private void deleteFileOp(long version, String userId, String fileId){
        Log.info("Executing operation: DeleteFile version = " + version);

        var uf = userFiles.getOrDefault(userId, new JavaDirectory.UserFiles());
        JavaDirectory.ExtendedFileInfo info;
        synchronized (uf) {
            info = files.remove(fileId);
            uf.owned().remove(fileId);
            JavaDirectory.removeSharesOfFile(info, this.userFiles);
        }
        for (String fileURL : info.uris()) {
            JavaDirectory.getFileCounts(JavaDirectory.serverURIFrom(fileURL), false, fileCounts).numFiles().decrementAndGet();
        }
        repManager.setCurrentVersion(version);
        sync.setResult(version, OK);
    }

    private void shareFileOp(long version, String fileId, String userIdShare){
        Log.info("Executing operation: ShareFile version = " + version);

        var file = files.get(fileId);
        var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new JavaDirectory.UserFiles());
        synchronized (uf) {
            uf.shared().add(fileId);
            file.info().getSharedWith().add(userIdShare);
        }
        repManager.setCurrentVersion(version);
        sync.setResult(version, OK);
    }

    private void unshareFileOp(long version, String fileId, String userIdShare){
        Log.info("Executing operation: UnshareFile version = " + version);

        var file = files.get(fileId);
        var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new JavaDirectory.UserFiles());
        synchronized (uf) {
            uf.shared().remove(fileId);
            file.info().getSharedWith().remove(userIdShare);
        }
        repManager.setCurrentVersion(version);
        sync.setResult(version, OK);
    }

}