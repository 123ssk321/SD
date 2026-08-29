package tp1.clients.file;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import tp1.api.service.rest.RestFiles;
import tp1.api.service.util.Files;
import tp1.api.service.util.Result;
import tp1.clients.RestClient;
import tp1.clients.util.RestResponseResult;

public class RestFilesClient extends RestClient implements Files {

    final WebTarget target;


    public RestFilesClient(URI serverURI){
        super(serverURI);
        target = client.target( serverURI ).path( RestFiles.PATH );
    }


    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
       return  super.reTry( () -> cltWriteFile(fileId, data, token));
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {
       return super.reTry( () -> cltDeleteFile(fileId, token));

    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {
        return super.reTry( () -> cltGetFile(fileId, token));
    }

    private Result<Void> cltWriteFile(String fileId, byte[] data, String token){
        Response r = target.path( fileId )
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(data, MediaType.APPLICATION_OCTET_STREAM));

        int responseStatus = r.getStatus();
        boolean successCondition = Integer.toString(responseStatus).startsWith("20");
        return RestResponseResult.getResult(r, responseStatus, successCondition);
    }

    private Result<Void> cltDeleteFile(String fileId, String token){
        Response r = target.path( fileId )
                .request()
                .delete();

        int responseStatus = r.getStatus();
        boolean successCondition = Integer.toString(responseStatus).startsWith("20");
        return RestResponseResult.getResult(r, responseStatus, successCondition);
    }

    private Result<byte[]> cltGetFile(String fileId, String token) {
        Response r = target.path(fileId)
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

}
