package util.replication.operations;

import tp.impl.servers.common.JavaReplicationDirectory;
import tp.impl.servers.common.JavaUsers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class DeleteUser {
    public static String DELIMITER = "&";

    private String userId;
    private String password;

    public DeleteUser(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return JavaReplicationDirectory.DELETE_USER_OP + JavaUsers.RECORD_DELIMITER +
                "userId=" + userId + DELIMITER +
                "password=" + password;
    }

    public static DeleteUser getOperationFrom(String deleteUserOp){
        String[] meta = deleteUserOp.split(DELIMITER);
        String userId = meta[0].split("=")[1];
        String password = meta[1].split("=")[1];
        return new DeleteUser(userId, password);
    }

}
