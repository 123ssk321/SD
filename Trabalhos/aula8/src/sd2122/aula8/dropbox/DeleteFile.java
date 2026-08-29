package sd2122.aula8.dropbox;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import org.pac4j.scribe.builder.api.DropboxApi20;
import sd2122.aula8.dropbox.msgs.DeleteFileV1Args;

import java.nio.file.Files;
import java.nio.file.Paths;

public class DeleteFile {
    private static final String apiKey = "tv881188xx6d6ly";
    private static final String apiSecret = "z0etm4xln8eg87i";
    private static final String accessTokenStr = "sl.BHJYKfEQwnIssGZZFqq9PfQXjGUrSN7j21bA27ZYiqY55YAU0venRvzo1_nnqigIgczm8UbgRx6jA5u2WuRbYG4PBZfxaS9Mu3Icm3rfr5LYNkWEMuGsPWf1z8pK0YwkF-RRpbk";

    private static final String DELETE_FILE_V2_URL = "https://api.dropboxapi.com/2/files/delete_v2";

    private static final int HTTP_SUCCESS = 200;
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json";

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    public DeleteFile() {
        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
    }

    public void execute( String fileName ) throws Exception {

        var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_V2_URL);
        deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        deleteFile.setPayload(json.toJson(new DeleteFileV1Args("/OnePiece/Episode1015/episode1015.txt")));

        service.signRequest(accessToken, deleteFile);

        Response r = service.execute(deleteFile);
        if (r.getCode() != HTTP_SUCCESS)
            throw new RuntimeException(String.format("Failed to create file: %s, Status: %d, \nReason: %s\n", fileName, r.getCode(), r.getBody()));
    }

    public static void main(String[] args) throws Exception {

		/*if( args.length != 1 ) {
			System.err.println("usage: java CreateDirectory <dir>");
			System.exit(0);
		}*/

        var file = "./estupido.mkv";
        var cd = new DeleteFile();

        cd.execute(file);
        System.out.println("Directory '" + file + "' created successfuly.");
    }
}
