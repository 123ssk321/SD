package util.replication.operations;

import tp.impl.servers.common.JavaReplicationDirectory;
import tp.impl.servers.common.JavaUsers;

public class UnshareFile {
    public static String DELIMITER = "&";

    private String fileId;
    private String userIdShare;

    public UnshareFile(String fileId, String userIdShare){
        this.fileId=fileId;
        this.userIdShare=userIdShare;
    }

    public String getFileId() {
        return fileId;
    }

    public String getUserIdShare() {
        return userIdShare;
    }

    @Override
    public String toString() {
        return JavaReplicationDirectory.UNSHARE_FILE_OP + JavaUsers.RECORD_DELIMITER+
                "fileId="+ fileId + DELIMITER +
                "userIdShare="+ userIdShare;
    }

    public static UnshareFile getOperationFrom(String unShareFileOp){
        String[] meta = unShareFileOp.split(DELIMITER);
        String fileId = meta[0].split("=")[1];
        String userIdShare = meta[1].split("=")[1];
        return new UnshareFile(fileId, userIdShare);
    }
}
