package util.replication;

public class ReplicationManager {

    private long currentVersion;

    public ReplicationManager(){
        currentVersion = 0;
    }

    public long getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(long version){
        currentVersion = version;
    }
}
