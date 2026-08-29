package tp1.clients.user;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.RestClient;
import tp1.clients.util.RestResponseResult;


public class RestUsersClient extends RestClient implements Users {

	final WebTarget target;

	
	RestUsersClient( URI serverURI ) {
		super( serverURI );
		target = client.target( serverURI ).path( RestUsers.PATH );
	}

	
	@Override
	public Result<String> createUser(User user) {
		return super.reTry( () -> clt_createUser( user ));
	}

	@Override
	public Result<User> getUser(String userId, String password) {
		return super.reTry( () -> clt_getUser( userId, password ));	}

	@Override
	public Result<User> updateUser(String userId, String oldpwd, User user) {
		return super.reTry( () -> clt_updateUser( userId, oldpwd, user ));	}

	@Override
	public Result<User> deleteUser(String userId, String password) {
		return super.reTry( () -> clt_deleteUser( userId, password ));	}

	@Override
	public Result<List<User>> searchUsers(String pattern) {
		return super.reTry( () -> clt_searchUsers( pattern ));
	}


	private Result<String> clt_createUser( User user) {
		Response r = target.request()
				.accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(user, MediaType.APPLICATION_JSON));

		int responseStatus = r.getStatus();
		boolean successCondition = Integer.toString(responseStatus).startsWith("20") && r.hasEntity();
		return RestResponseResult.getResult(r, responseStatus, successCondition, () -> r.readEntity(String.class));
	}

	private Result<User> clt_getUser(String userId, String password){
		Response r = target.path( userId )
				.queryParam(RestUsers.PASSWORD, password)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();

		int responseStatus = r.getStatus();
		boolean successCondition = Integer.toString(responseStatus).startsWith("20") && r.hasEntity();
		return RestResponseResult.getResult(r, responseStatus, successCondition, () -> r.readEntity(User.class));
	}

	private Result<User> clt_updateUser(String userId, String oldpwd, User u){
		Response r = target.path( userId )
				.queryParam(RestUsers.PASSWORD, oldpwd).request()
				.accept(MediaType.APPLICATION_JSON)
				.put(Entity.entity(u, MediaType.APPLICATION_JSON));

		int responseStatus = r.getStatus();
		boolean successCondition = Integer.toString(responseStatus).startsWith("20") && r.hasEntity();
		return RestResponseResult.getResult(r, responseStatus, successCondition, () -> r.readEntity(User.class));
	}

	private Result<User> clt_deleteUser(String userId, String password){
		Response r = target.path(userId)
				.queryParam(RestUsers.PASSWORD, password)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.delete();
		int responseStatus = r.getStatus();
		boolean successCondition = Integer.toString(responseStatus).startsWith("20") && r.hasEntity();
		return RestResponseResult.getResult(r, responseStatus, successCondition, () -> r.readEntity(User.class));
	}

	private Result<List<User>>clt_searchUsers(String pattern) {
		Response r = target
				.queryParam(RestUsers.QUERY, pattern)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.get();
		int responseStatus = r.getStatus();
		boolean successCondition = Integer.toString(responseStatus).startsWith("20") && r.hasEntity();
		return RestResponseResult.getResult(r, responseStatus, successCondition, () -> r.readEntity(new GenericType<List<User>>() {}));
	}

}
