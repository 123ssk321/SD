package tp.impl.servers.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.Set;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tp.api.service.java.Files;
import tp.api.service.java.Result;
import tp.api.service.rest.RestFiles;
import tp.impl.servers.common.JavaDirectory;
import tp.impl.servers.common.JavaFiles;
import util.Token;

public class RestResource {

	/**
	 * Given a Result<T>, either returns the value, or throws the JAX-WS Exception
	 * matching the error code...
	 */
	protected <T> T resultOrThrow(Result<T> result) {
		if (result.isOK())
			return result.value();
		else
			throw new WebApplicationException(statusCode(result));
	}

	/**
	 * Translates a Result<T> to a HTTP Status code
	 */
	static protected Status statusCode(Result<?> result) {
		switch (result.error()) {
		case CONFLICT:
			return Status.CONFLICT;
		case NOT_FOUND:
			return Status.NOT_FOUND;
		case FORBIDDEN:
			return Status.FORBIDDEN;
		case TIMEOUT:
		case BAD_REQUEST:
			return Status.BAD_REQUEST;
		case NOT_IMPLEMENTED:
			return Status.NOT_IMPLEMENTED;
		case INTERNAL_ERROR:
			return Status.INTERNAL_SERVER_ERROR;
		case OK:
			return result.value() == null ? Status.NO_CONTENT : Status.OK;
		case REDIRECT:
			doRedirect(result);

		default:
			return Status.INTERNAL_SERVER_ERROR;
		}
	}

	static private void doRedirect(Result<?> result) throws WebApplicationException {
		Queue<String> uris = result.errorValue();
		var location = uris.remove();
		uris.add(location);
		URI fileURL = URI.create(location);
		String fileId = fileURL.getPath().split("/")[3];
		String token = JavaDirectory.generateTokenForFiles(fileId);
		try {
			fileURL = new URI(fileURL.getScheme(), fileURL.getAuthority(),
					fileURL.getPath(), RestFiles.TOKEN+"="+token, fileURL.getFragment());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		throw new WebApplicationException(Response.temporaryRedirect(fileURL).build());
	}
}