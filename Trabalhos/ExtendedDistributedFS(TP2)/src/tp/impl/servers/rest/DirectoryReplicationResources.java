package tp.impl.servers.rest;

import tp.api.FileInfo;
import tp.api.service.java.Directory;
import tp.api.service.java.Result;
import tp.api.service.rest.RestDirectory;
import tp.impl.servers.common.JavaDirectory;
import tp.impl.servers.common.JavaReplicationDirectory;
import util.kafka.sync.SyncPoint;
import util.replication.Operation;
import util.replication.ReplicationManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import static tp.impl.clients.Clients.FilesClients;

public class DirectoryReplicationResources extends RestResource implements RestDirectory {
    private static Logger Log = Logger.getLogger(DirectoryReplicationResources.class.getName());

    private static final String REST = "/rest/";

    final Directory impl;
    private SyncPoint<Result.ErrorCode> sync;
    private ReplicationManager repManager;

    public DirectoryReplicationResources(SyncPoint<Result.ErrorCode> sync, ReplicationManager repManager) {
        impl = new JavaReplicationDirectory(sync, repManager);
        this.sync = sync;
        this.repManager = repManager;
    }

    public FileInfo writeFile(Long version, String filename, byte[] data, String userId, String password) {
        Log.info(String.format("REST writeFile: filename = %s, data.length = %d, userId = %s, password = %s \n",
                filename, data.length, userId, password));

        return super.resultOrThrow(impl.writeFile(filename, data, userId, password));
    }

    @Override
    public void deleteFile(Long version, String filename, String userId, String password) {
        Log.info(String.format("REST deleteFile: filename = %s, userId = %s, password =%s\n", filename, userId,
                password));

        super.resultOrThrow(impl.deleteFile(filename, userId, password));
    }

    @Override
    public void shareFile(Long version, String filename, String userId, String userIdShare, String password) {
        Log.info(String.format("REST shareFile: filename = %s, userId = %s, userIdShare = %s, password =%s\n", filename,
                userId, userIdShare, password));

        super.resultOrThrow(impl.shareFile(filename, userId, userIdShare, password));
    }

    @Override
    public void unshareFile(Long version, String filename, String userId, String userIdShare, String password) {
        Log.info(String.format("REST unshareFile: filename = %s, userId = %s, userIdShare = %s, password =%s\n",
                filename, userId, userIdShare, password));

        super.resultOrThrow(impl.unshareFile(filename, userId, userIdShare, password));
    }

    @Override
    public byte[] getFile(Long version, String filename, String userId, String accUserId, String password) {
        Log.info(String.format("REST getFile: filename = %s, userId = %s, accUserId = %s, password =%s\n", filename,
                userId, accUserId, password));

        version = version == null ? 0:version;
        if(version > repManager.getCurrentVersion())
            sync.waitForResult(version);

        var res = impl.getFile(filename, userId, accUserId, password);
        if (res.error() == Result.ErrorCode.REDIRECT) {
            Queue<String> uris = res.errorValue();
            List<String> soapUris = new LinkedList<>();
            List<String> restUris = new LinkedList<>();
            while (!uris.isEmpty()){
                var uri = uris.remove();
                if(!uri.contains(REST)){
                    String fileId = JavaDirectory.fileId(filename, userId);
                    res = FilesClients.get(uri).getFile(fileId, JavaReplicationDirectory.generateTokenForFiles(fileId));
                    soapUris.add(uri);
                    if(res.isOK()){
                        break;
                    }
                } else {
                    restUris.add(uri);
                }
            }
            uris.addAll(restUris);
            uris.addAll(soapUris);
        }
        return super.resultOrThrow(res);
    }

    @Override
    public List<FileInfo> lsFile(Long version, String userId, String password) {
        long T0 = System.currentTimeMillis();
        try {

            Log.info(String.format("REST lsFile: userId = %s, password = %s\n", userId, password));
            version = version == null ? 0:version;
            if(version > repManager.getCurrentVersion())
                sync.waitForResult(version);
            return super.resultOrThrow(impl.lsFile(userId, password));
        } finally {
            System.err.println("TOOK:" + (System.currentTimeMillis() - T0));
        }
    }

    @Override
    public void deleteUserFiles(Long version, String userId, String password, String token) {
        Log.info(
                String.format("REST deleteUserFiles: user = %s, password = %s, token = %s\n", userId, password, token));

        super.resultOrThrow(impl.deleteUserFiles(userId, password, token));
    }

}
