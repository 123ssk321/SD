package sd2122.aula3.server.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2122.aula3.api.User;
import sd2122.aula3.api.service.RestUsers;

@Singleton
public class UsersResource implements RestUsers {

	private final Map<String,User> users = new HashMap<>();

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());
	
	public UsersResource() {
	}
		
	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);
		
		// Check if user data is valid
		if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || 
				user.getEmail() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}
		
		// Check if userId already exists
		if( users.containsKey(user.getUserId())) {
			Log.info("User already exists.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		//Add the user to the map of users
		users.put(user.getUserId(), user);
		return user.getUserId();
	}

	@Override
	public User getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);
		
		// Check if user is valid
		if(userId == null) {
			Log.info("UserId or passwrod null.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}
		
		User user = users.get(userId);
		
		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}
		
		//Check if the password is correct
		if(password == null || !user.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}
		
		return user;
	}

	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);
		// TODO Complete method

		// Check if user is valid
		if(userId == null || user == null) {
			Log.info("UserId or user object null.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		User userToUpdate = users.get(userId);

		// Check if userToUpdate exists
		if( userToUpdate == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		//Check if the password is correct
		if(password == null || !userToUpdate.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		String email = user.getEmail();
		String newUserId = user.getUserId();
		String fullName = user.getFullName();
		String newPassword = user.getPassword();

		if(email != null){
			userToUpdate.setEmail(email);
		}

		if(fullName != null){
			userToUpdate.setFullName(fullName);
		}
		if(newPassword != null){
			userToUpdate.setPassword(newPassword);
		}

		Log.info("User updated.");

		return userToUpdate;
	}


	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);
		// TODO Complete method

		// Check if user is valid
		if(userId == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException( Status.BAD_REQUEST );
		}

		User user = users.get(userId);

		// Check if user exists
		if( user == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		//Check if the password is correct
		if( password == null || !user.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		return users.remove(userId);
	}

	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);
		// TODO Complete method

		ArrayList<User> foundUsers;

		if(pattern == null || pattern.isBlank()){
			foundUsers = new ArrayList<User>(users.values());
		} else {
			Pattern pat = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			foundUsers = new ArrayList<>();

			for (var entry : users.entrySet()) {
				User oldUser = entry.getValue();
				if (pat.matcher(oldUser.getFullName()).find()) {
					User user = new User(oldUser.getUserId(), oldUser.getFullName(), oldUser.getEmail(), "");
					foundUsers.add(user);
				}
			}

		}
		return foundUsers;
	}

}
