package tp1.servers.users.logic;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import tp1.api.FileInfo;
import tp1.api.User;
import tp1.api.service.util.Directory;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.api.util.Discovery;
import tp1.api.util.URLConfigurator;
import tp1.clients.directory.DirectoryClientFactory;


public class UsersLogic implements Users {

    private static final Logger Log = Logger.getLogger(UsersLogic.class.getName());

    private final Map<String,User> users = new HashMap<>();

    private final Discovery discovery;

    private final String directoryServiceName;

    private final DirectoryClientFactory directoryClientFactory;


    public UsersLogic(Discovery discovery, String directoryServiceName){
        this.discovery = discovery;
        this.directoryServiceName = directoryServiceName;

        directoryClientFactory =  new DirectoryClientFactory();
    }


    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        String userId = user.getUserId();

        // Check if user data is valid
        if(userId == null || user.getPassword() == null || user.getFullName() == null || user.getEmail() == null) {
            Log.info("Invalid user object.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        synchronized (users) {
            // Check if userId already exists
            if (users.containsKey(userId)) {
                Log.info("User already exists.");
                return Result.error(Result.ErrorCode.CONFLICT);
            }

            users.put(userId, user);
        }
        return Result.ok(userId);
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        Log.info("getUser : user = " + userId + "; pwd = " + password);

        // Check if userId is valid
        if(userId == null) {
            Log.info("UserId is null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        synchronized (users) {
            User user = users.get(userId);

            // Check if user exists and password is correct
            Result.ErrorCode errorCode = this.checkParameters(user, password);
            if(errorCode != Result.ErrorCode.OK)
                return Result.error(errorCode);

            return Result.ok(user);
        }
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

        // Check if user is valid
        if(userId == null || user == null) {
            Log.info("UserId or user object are null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        synchronized (users) {
            User userToUpdate = users.get(userId);

            // Check if user exists and password is correct
            Result.ErrorCode errorCode = this.checkParameters(userToUpdate, password);
            if(errorCode != Result.ErrorCode.OK)
                return Result.error(errorCode);

            String email = user.getEmail();
            String fullName = user.getFullName();
            String newPassword = user.getPassword();

            if (email != null) {
                userToUpdate.setEmail(email);
            }

            if (fullName != null) {
                userToUpdate.setFullName(fullName);
            }
            if (newPassword != null) {
                userToUpdate.setPassword(newPassword);
            }

            Log.info("User updated.");

            return Result.ok(userToUpdate);
        }
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        Log.info("deleteUser : user = " + userId + "; pwd = " + password);

        // Check if user is valid
        if(userId == null) {
            Log.info("UserId is null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        synchronized (users) {
            User user = users.get(userId);

            // Check if user exists and password is correct
            Result.ErrorCode errorCode = this.checkParameters(user, password);
            if(errorCode != Result.ErrorCode.OK)
                return Result.error(errorCode);

        }

        // Delete user files
        String directoryServiceURI = URLConfigurator.getURLFor(discovery, directoryServiceName)[0].toString();
        Directory directoryService = directoryClientFactory.getClient(URI.create(directoryServiceURI));

        var userFilesResult = directoryService.lsFile(userId, password);
        if (userFilesResult.isOK()) {
            for (FileInfo fileInfo : userFilesResult.value()) {
                String filename = fileInfo.getFilename();
                if (fileInfo.getOwner().equals(userId)) {
                    directoryService.deleteFile(filename, userId, password);
                } else {
                    for (String userIdShared : fileInfo.getSharedWith()) {
                        directoryService.unshareFile(filename, userId, userIdShared, password);
                    }
                }
            }
        }

        synchronized (users) {
            return Result.ok(users.remove(userId));
        }
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);

        ArrayList<User> foundUsers;

        synchronized (users) {
            if (pattern == null || pattern.isBlank()) {
                foundUsers = new ArrayList<>(users.values());
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

            return Result.ok(foundUsers);
        }
    }

    private Result.ErrorCode checkParameters(User user, String password){
        // Check if user exists
        if (user == null) {
            Log.info("User does not exist.");
            return Result.ErrorCode.NOT_FOUND;
        }

        //Check if the password is correct
        if (!user.getPassword().equals(password)) {
            Log.info("Password is incorrect.");
            return Result.ErrorCode.FORBIDDEN;
        }
        return Result.ErrorCode.OK;
    }

}
