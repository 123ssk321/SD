package tp1.clients.user;

import jakarta.xml.ws.BindingProvider;

import java.net.URI;
import java.util.List;

import tp1.api.User;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;
import tp1.api.service.util.ErrorCodeConverter;
import tp1.api.service.util.Result;
import tp1.api.service.util.Users;
import tp1.clients.SoapClient;


public class SoapUsersClient extends SoapClient implements Users {

    private final SoapUsers usersProvider;


    public SoapUsersClient(URI serverURI) {
        super(serverURI, SoapUsers.NAMESPACE, SoapUsers.NAME);
        usersProvider = service.getPort(tp1.api.service.soap.SoapUsers.class);
        SoapClient.setClientTimeouts((BindingProvider) usersProvider);
    }


    @Override
    public Result<String> createUser(User user) {
        return super.reTry( () -> cltCreateUser(user));
    }

    @Override
    public Result<User> getUser(String userId, String password) {
        return super.reTry( () -> cltGetUser(userId, password));
    }

    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        return super.reTry( () -> cltUpdateUser(userId, password, user));
    }

    @Override
    public Result<User> deleteUser(String userId, String password) {
        return super.reTry( () -> cltDeleteUser(userId, password));
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return super.reTry( () -> cltSearchUsers(pattern));
    }

    private Result<String> cltCreateUser(User user) {
        try {
            return Result.ok(usersProvider.createUser(user));
        } catch (UsersException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<User> cltGetUser(String userId, String password) {
        try {
            return Result.ok(usersProvider.getUser(userId, password));
        } catch (UsersException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }

    }

    private Result<User> cltUpdateUser(String userId, String password, User user) {
        try {
            return Result.ok(usersProvider.updateUser(userId, password, user));
        } catch (UsersException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<User> cltDeleteUser(String userId, String password) {
        try {
            return Result.ok(usersProvider.deleteUser(userId, password));
        } catch (UsersException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

    private Result<List<User>> cltSearchUsers(String pattern) {
        try {
            return Result.ok(usersProvider.searchUsers(pattern));
        } catch (UsersException e) {
            return Result.error(ErrorCodeConverter.converterStringErrorToErrorCode(e.getMessage()));
        }
    }

}
