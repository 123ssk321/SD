package tp1.servers.users.soap;



import jakarta.jws.WebService;

import java.util.List;

import tp1.api.User;
import tp1.api.service.soap.SoapUsers;
import tp1.api.service.soap.UsersException;
import tp1.api.service.util.Users;
import tp1.api.util.Discovery;
import tp1.servers.directory.soap.SoapDirectoryServer;
import tp1.servers.users.logic.UsersLogic;


@WebService(serviceName=SoapUsers.NAME, targetNamespace=SoapUsers.NAMESPACE, endpointInterface=SoapUsers.INTERFACE)
public class SoapUsersWebService extends tp1.servers.WebService implements SoapUsers {

    private final Users users;


    public SoapUsersWebService(Discovery discovery) {
        super();
        users = new UsersLogic(discovery, SoapDirectoryServer.SERVICE_NAME);
    }


    @Override
    public String createUser(User user) throws UsersException {
        try {
            return super.getResult(()->users.createUser( user ));
        } catch (Exception e) {
            throw new UsersException(e.getMessage());
        }
    }

    @Override
    public User getUser(String userId, String password) throws UsersException {
        try {
            return super.getResult(()->users.getUser( userId, password ));
        } catch (Exception e) {
            throw new UsersException(e.getMessage());
        }
    }

    @Override
    public User updateUser(String userId, String password, User user) throws UsersException {
        try {
            return super.getResult(()->users.updateUser( userId, password, user ));
        } catch (Exception e) {
            throw new UsersException(e.getMessage());
        }
    }

    @Override
    public User deleteUser(String userId, String password) throws UsersException {
        try {
            return super.getResult(()->users.deleteUser( userId, password ));
        } catch (Exception e) {
            throw new UsersException(e.getMessage());
        }
    }

    @Override
    public List<User> searchUsers(String pattern) throws UsersException {
        try {
            return super.getResult(()->users.searchUsers( pattern ));
        } catch (Exception e) {
            throw new UsersException(e.getMessage());
        }
    }

}
