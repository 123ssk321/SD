package util.replication.operations;

import tp.impl.servers.common.JavaReplicationDirectory;
import tp.impl.servers.common.JavaUsers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class WriteFile {
    public static String DELIMITER = "&";

    private String operation;
    private String userId;
    private String filename;
    private Queue<String> fileURLs;

    public WriteFile(String userId, String filename, Queue<String> fileURLs) {
        this.userId = userId;
        this.filename = filename;
        this.fileURLs = fileURLs;
    }

    public String getUserId() {
        return userId;
    }

    public String getFilename() {
        return filename;
    }

    public Queue<String> getFileURLs() {
        return fileURLs;
    }

    @Override
    public String toString() {
        return JavaReplicationDirectory.WRITE_FILE_OP + JavaUsers.RECORD_DELIMITER +
                "userId=" + userId + DELIMITER +
                "filename=" + filename + DELIMITER +
                "serverURIs=" + fileURLs;
    }


    public static WriteFile getOperationFrom(String writeFileOp){
        String[] meta = writeFileOp.split(DELIMITER);
        String userId = meta[0].split("=")[1];
        String filename = meta[1].split("=")[1];
        Queue<String> fileURLs = new LinkedList<>(Arrays.asList(meta[2].split("=")[1]
                .replace("[", "")
                .replace("]", "")
                .split("\\s*,\\s*")));
        return new WriteFile(userId, filename, fileURLs);
    }

}
