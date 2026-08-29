package sd2122.aula8.dropbox;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import org.pac4j.scribe.builder.api.DropboxApi20;

import java.nio.file.Files;
import java.nio.file.Paths;

public class UpdateFile {

    private static final String apiKey = "tv881188xx6d6ly";
    private static final String apiSecret = "z0etm4xln8eg87i";
    private static final String accessTokenStr = "sl.BHJYKfEQwnIssGZZFqq9PfQXjGUrSN7j21bA27ZYiqY55YAU0venRvzo1_nnqigIgczm8UbgRx6jA5u2WuRbYG4PBZfxaS9Mu3Icm3rfr5LYNkWEMuGsPWf1z8pK0YwkF-RRpbk";

    private static final String UPLOAD_FILE_V1_URL = "https://content.dropboxapi.com/2/files/upload";

    private static final int HTTP_SUCCESS = 200;
    private static final String CONTENT_TYPE_HDR = "Content-Type";
    private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";

    private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;

    public UpdateFile() {
        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
    }

    public void execute( String fileName ) throws Exception {

        var updateFile = new OAuthRequest(Verb.POST, UPLOAD_FILE_V1_URL);
        updateFile.addHeader("Dropbox-API-Arg", "{\"path\": \"/OnePiece/Episode1015/episode1015.txt\", \"mode\":\"overwrite\"}");
        updateFile.addHeader(CONTENT_TYPE_HDR, OCTET_STREAM_CONTENT_TYPE);

        updateFile.setPayload(Files.readAllBytes(Paths.get(fileName)));

        service.signRequest(accessToken, updateFile);

        Response r = service.execute(updateFile);
        if (r.getCode() != HTTP_SUCCESS)
            throw new RuntimeException(String.format("Failed to create file: %s, Status: %d, \nReason: %s\n", fileName, r.getCode(), r.getBody()));
    }

    public static void main(String[] args) throws Exception {

		/*if( args.length != 1 ) {
			System.err.println("usage: java CreateDirectory <dir>");
			System.exit(0);
		}*/

        var file = "./Episode1015.txt";
        var cd = new UpdateFile();

        cd.execute(file);
        System.out.println("Directory '" + file + "' created successfuly.");
    }

}
