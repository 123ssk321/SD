package tp.impl.servers.rest;

import org.glassfish.jersey.server.ResourceConfig;
import tp.api.service.java.Directory;
import tp.api.service.java.Result;
import tp.impl.servers.rest.util.GenericExceptionMapper;
import tp.impl.servers.rest.util.VersionFilter;
import util.Debug;
import util.Token;
import util.replication.ReplicationManager;

import java.util.logging.Level;
import java.util.logging.Logger;
import util.kafka.sync.SyncPoint;

public class DirectoryReplicationRestServer extends AbstractRestServer {

    public static final int PORT = 4568;

    private static Logger Log = Logger.getLogger(DirectoryReplicationRestServer.class.getName());

    DirectoryReplicationRestServer() {
        super(Log, Directory.SERVICE_NAME, PORT);
    }

    @Override
    void registerResources(ResourceConfig config) {
        var repManager = new ReplicationManager();
        config.register( new DirectoryReplicationResources(new SyncPoint<Result.ErrorCode>(), repManager) );
        config.register( GenericExceptionMapper.class );
        config.register(new VersionFilter(repManager));
//		config.register( CustomLoggingFilter.class);
    }

    public static void main(String[] args) throws Exception {

        Debug.setLogLevel( Level.INFO, Debug.TP);

        Token.set( args.length > 0 ? args[0] : "");

        new DirectoryReplicationRestServer().start();
    }
}
