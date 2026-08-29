package util.replication.operations;

import tp.impl.servers.common.JavaReplicationDirectory;
import tp.impl.servers.common.JavaUsers;

import java.util.List;

public class DeleteFile {


    public static String DELIMITER = "&";

    private String operation;
    private String userId;
    private String fileId;

    public DeleteFile(String userId, String fileId){
        this.fileId=fileId;
        this.userId=userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getFileId() {
        return fileId;
    }

    @Override
    public String toString() {
        return JavaReplicationDirectory.DELETE_FILE_OP + JavaUsers.RECORD_DELIMITER+
                "userId="+ userId + DELIMITER +
                "fileId="+ fileId;
    }

    public static DeleteFile getOperationFrom(String deleteFileOp){
        String[] meta = deleteFileOp.split(DELIMITER);
        String userId = meta[0].split("=")[1];
        String fileId = meta[1].split("=")[1];
        return new DeleteFile(userId, fileId);
    }
}
