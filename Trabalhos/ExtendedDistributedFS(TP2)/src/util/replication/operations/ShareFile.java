package util.replication.operations;

import tp.impl.servers.common.JavaReplicationDirectory;
import tp.impl.servers.common.JavaUsers;

public class ShareFile {
    public static String DELIMITER = "&";

    private String operation;
    private String fileId;
    private String userIdShare;

    public ShareFile(String fileId, String userIdShare){
        this.fileId=fileId;
        this.userIdShare=userIdShare;
    }

    public String getUserIdShare() {
        return userIdShare;
    }

    public String getFileId() {
        return fileId;
    }

    @Override
    public String toString() {
        return JavaReplicationDirectory.SHARE_FILE_OP + JavaUsers.RECORD_DELIMITER+
                "fileId="+ fileId + DELIMITER +
                "userIdShare="+ userIdShare;
    }

    public static ShareFile getOperationFrom(String shareFileOp){
        String[] meta = shareFileOp.split(DELIMITER);
        String fileId = meta[0].split("=")[1];
        String userIdShare = meta[1].split("=")[1];
        return new ShareFile(fileId, userIdShare);
    }
}
