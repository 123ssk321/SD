package tp1.servers.users.rest.resources;

import jakarta.inject.Singleton;

import java.util.List;

import tp1.api.User;
import tp1.api.service.rest.RestUsers;
import tp1.api.service.util.ErrorCodeConverter;
import tp1.api.service.util.Users;
import tp1.api.util.Discovery;
import tp1.servers.Resource;
import tp1.servers.directory.rest.DirectoryServer;
import tp1.servers.users.logic.UsersLogic;


@Singleton
public class UsersResource extends Resource implements RestUsers{

	private final Users users;


	public UsersResource(Discovery discovery) {
		super();
		users = new UsersLogic(discovery, DirectoryServer.SERVICE);
	}


	@Override
	public String createUser(User user) {
		return super.getResult(()->users.createUser(user), ErrorCodeConverter::convertErrorCodeToHttpError);
	}

	@Override
	public User getUser(String userId, String password) {
		return super.getResult(()->users.getUser(userId, password), ErrorCodeConverter::convertErrorCodeToHttpError);
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		return super.getResult(()->users.updateUser(userId, password, user), ErrorCodeConverter::convertErrorCodeToHttpError);
	}

	@Override
	public User deleteUser(String userId, String password) {
		return super.getResult(()->users.deleteUser(userId, password), ErrorCodeConverter::convertErrorCodeToHttpError);
	}

	public List<User> searchUsers(String pattern) {
		return super.getResult(()->users.searchUsers(pattern), ErrorCodeConverter::convertErrorCodeToHttpError);
	}

}
