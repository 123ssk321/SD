package tp.impl.servers.common;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import tp.api.service.java.Directory;
import tp.api.service.java.Files;
import tp.api.service.java.Result;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import org.pac4j.scribe.builder.api.DropboxApi20;
import tp.api.service.java.Users;
import tp.impl.servers.rest.AbstractRestServer;
import tp.impl.servers.rest.FilesRestServer;
import util.Hash;
import util.IP;
import util.Secrets;
import util.Token;
import util.kafka.KafkaSubscriber;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static tp.api.service.java.Result.ErrorCode.BAD_REQUEST;
import static tp.api.service.java.Result.ErrorCode.NOT_FOUND;
import static tp.api.service.java.Result.error;
import static tp.api.service.java.Result.ok;

public class JavaDropboxFiles implements Files {


    private static final String DOWNLOAD_FILE_V1_URL = "https://content.dropboxapi.com/2/files/download";
    private static final String DELETE_FILE_V2_URL = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String UPLOAD_FILE_V1_URL = "https://content.dropboxapi.com/2/files/upload";

    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";
    private static final String JSON_CONTENT_TYPE = "application/json";

    private static final String SECRETS_PROP = "FILES_EXTRA_ARGS";
    private static final String DROPBOX_SECRETS_PROP = "FILES_PROXY_EXTRA_ARGS";
    public static final int VALID_SECONDS = 10;
    private static final String FROM_BEGINNING = "earliest";

    private static final String SECRET_APIKEY_PROP = "apiKey";
    private static final String SECRET_APISECRET_PROP = "apiSecret";
    private static final String SECRET_ACCESS_TOKEN_PROP = "accessTokenStr";

    private static String apiKey = Secrets.secretFrom(SECRET_APIKEY_PROP, DROPBOX_SECRETS_PROP);;
    private static String apiSecret = Secrets.secretFrom(SECRET_APISECRET_PROP, DROPBOX_SECRETS_PROP);;
    private static String accessTokenStr = Secrets.secretFrom(SECRET_ACCESS_TOKEN_PROP, DROPBOX_SECRETS_PROP);;

    private Gson json;
    private OAuth20Service service;
    private OAuth2AccessToken accessToken;

    static final String DELIMITER = "$$$";
    private static final String ROOT = "/TMP";
    private static final int HTTP_SUCCESS = 200;

    private String usersSecret;
    private String directorySecret;

    private record DeleteFileV1Args(String path) {

    }

    public JavaDropboxFiles() {
        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
        usersSecret = Secrets.secretFrom(Users.SERVICE_NAME, SECRETS_PROP);
        directorySecret = Secrets.secretFrom(Directory.SERVICE_NAME, SECRETS_PROP);

        var subscriber = KafkaSubscriber.createSubscriber(JavaUsers.KAFKA_BROKERS, List.of(JavaUsers.DELETE_USER_TOPIC, JavaDirectory.DELETE_FILE_TOPIC), FROM_BEGINNING);

        subscriber.start(false, (r) -> {
            processDeleteUserRecord(r);
        });
    }

    @Override
    public Result<byte[]> getFile(String fileId, String token) {
        if(isValid(fileId, token)) {
            String filePath = fileId.replace(DELIMITER, "/");

            var getFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_V1_URL);
            getFile.addHeader(CONTENT_TYPE_HDR, "");
            getFile.addHeader("Dropbox-API-Arg", "{\"path\": \"" + ROOT + "/" + filePath + "\"}");

            Response r = executeRequest(this.service, this.accessToken, getFile);
            byte[] data = null;

            try {
                data = r.getStream().readAllBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return r.getCode() == HTTP_SUCCESS ? Result.ok(data) : error(NOT_FOUND);
        } else
            return error(BAD_REQUEST);
    }

    @Override
    public Result<Void> deleteFile(String fileId, String token) {
        if(isValid(fileId, token)) {
            String filePath = fileId.replace(DELIMITER, "/");

            var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
            deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

            deleteFile.setPayload(json.toJson(new DeleteFileV1Args(ROOT + "/" + filePath)));

            Response r = executeRequest(this.service, this.accessToken, deleteFile);

            return r.getCode() == HTTP_SUCCESS ? ok() : error(NOT_FOUND);
        } else
            return error(BAD_REQUEST);
    }

    @Override
    public Result<Void> writeFile(String fileId, byte[] data, String token) {
        if(isValid(fileId, token)) {
            String filePath = fileId.replace(DELIMITER, "/");

            var createFile = new OAuthRequest(Verb.POST, UPLOAD_FILE_V1_URL);
            createFile.addHeader("Dropbox-API-Arg", "{\"mode\":\"overwrite\", \"path\": \"" + ROOT + "/" + filePath + "\"}");
            createFile.addHeader(CONTENT_TYPE_HDR, OCTET_STREAM_CONTENT_TYPE);

            createFile.setPayload(data);

            executeRequest(this.service, this.accessToken, createFile);

            return ok();
        } else
            return error(BAD_REQUEST);
    }

    @Override
    public Result<Void> deleteUserFiles(String userId, String token) {
        if(isValid(token)) {
            var deleteFolder = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
            deleteFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

            deleteFolder.setPayload(json.toJson(new DeleteFileV1Args(ROOT + "/" + userId)));

            executeRequest(this.service, this.accessToken, deleteFolder);

            return ok();
        }else
            return error(BAD_REQUEST);
    }

    public static void resetServerContents() {
        Gson json = new Gson();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(accessTokenStr);
        OAuth20Service service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);

        var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
        deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        deleteFile.setPayload(json.toJson(new DeleteFileV1Args(ROOT)));

        executeRequest(service, accessToken, deleteFile);
    }

    private static Response executeRequest(OAuth20Service service, OAuth2AccessToken accessToken, OAuthRequest request) {
        Response r = null;
        service.signRequest(accessToken, request);

        try {
            r = service.execute(request);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;
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

    private void processDeleteUserRecord(ConsumerRecord<String, String> r) {
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