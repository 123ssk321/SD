package tp1.clients.directory;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import tp1.api.FileInfo;
import tp1.api.service.rest.RestDirectory;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.clients.RestClient;
import tp1.clients.util.RestResponseResult;


public class RestDirectoryClient extends RestClient implements Directory {

    final WebTarget target;


    public RestDirectoryClient(URI serverURI){
        super(serverURI);
        target = client.target( serverURI ).path( RestDirectory.PATH );
    }


    @Override
    public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) {
        return super.reTry(() -> cltWriteFile(filename, data, userId, password));
    }

    @Override
    public Result<Void> deleteFile(String filename, String userId, String password) {
        return super.reTry(() -> cltDeleteFile(filename, userId, password));
    }

    @Override
    public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
        return super.reTry(() -> cltShareFile(filename, userId, userIdShare, password));
    }

    @Override
    public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
       return super.reTry(() -> cltUnshareFile(filename, userId, userIdShare, password));
    }

    @Override
    public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
        return super.reTry(() -> cltGetFile(filename, userId, accUserId, password));
    }

    @Override
    public Result<List<FileInfo>> lsFile(String userId, String password) {
        return super.reTry(() -> cltLsFile(userId, password));
    }

    private Result<FileInfo> cltWriteFile(String filename, byte[] data, String userId, String password){
        Response r = target.path(userId)
                .path(filename)
                .queryParam(RestDirectory.PASSWORD, password)
                .request()
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        int responseStatus = r.getStatus();
        boolean successCondition = Integer.toString(responseStatus).startsWith("20");
        return RestResponseResult.getResult(r, responseStatus, successCondition, () -> r.readEntity(FileInfo.class));
    }

    private Result<Void> cltDeleteFile(String filename, String userId, String password){
        Response r = target.path(userId)
                .path(filename)
                .queryParam(RestDirectory.PASSWORD, password)
                .request()
                .delete();

        int responseStatus = r.getStatus();
        boolean successCondition = Integer.toString(responseStatus).startsWith("20");
        return RestResponseResult.getResult(r, responseStatus, successCondition);
    }

    private Result<Void> cltShareFile(String filename, String userId, String userIdShare, String password){
        Response r = target.path(userId)
                .path(filename)
                .path(RestDirectory.SHARE)
                .path(userIdShare)
                .queryParam(RestDirectory.PASSWORD, password)
                .request()
                .post(Entity.text(""));

        int responseStatus = r.getStatus();
        boolean successCondition = Integer.toString(responseStatus).startsWith("20");
        return RestResponseResult.getResult(r, responseStatus, successCondition);
    }

    private Result<Void> cltUnshareFile(String filename, String userId, String userIdShare, String password){
        Response r = target.path(userId)
                .path(filename)
                .path(RestDirectory.SHARE)
                .path(userIdShare)
                .queryParam(RestDirectory.PASSWORD, password)
                .request()
                .delete();

        int responseStatus = r.getStatus();
        boolean successCondition = Integer.toString(responseStatus).startsWith("20");
        return RestResponseResult.getResult(r, responseStatus, successCondition);
    }

    private Result<byte[]> cltGetFile(String filename, String userId, String accUserId, String password) {
        Response r = target.path(userId)
                .path(filename)
                .queryParam(RestDirectory.ACC_USER_ID, accUserId)
                .queryParam(RestDirectory.PASSWORD, password)
                .request()
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .get();

        int responseStatus = r.getStatus();
        boolean successCondition = Integer.toString(responseStatus).startsWith("20") && r.hasEntity();
        return RestResponseResult.getResult(r, responseStatus, successCondition, () -> {
            try {
                return r.readEntity(InputStream.class).readAllBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private Result<List<FileInfo>> cltLsFile(String userId, String password){
        Response r = target.path(userId)
                .queryParam(RestDirectory.PASSWORD, password)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        int responseStatus = r.getStatus();
        boolean successCondition = Integer.toString(responseStatus).startsWith("20") && r.hasEntity();
        return RestResponseResult.getResult(r, responseStatus, successCondition, () -> r.readEntity(new GenericType<List<FileInfo>>() {}));
    }

}
