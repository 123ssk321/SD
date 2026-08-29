package tp.impl.servers.rest;

import org.glassfish.jersey.server.ResourceConfig;
import tp.api.service.java.Files;
import tp.impl.servers.common.JavaDropboxFiles;
import tp.impl.servers.rest.util.GenericExceptionMapper;
import util.Debug;
import util.Token;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FilesRestDropboxServer extends AbstractRestServer{
    public static final int PORT = 5679;

    private static Logger Log = Logger.getLogger(FilesRestDropboxServer.class.getName());

    protected FilesRestDropboxServer() {
        super(Log, Files.SERVICE_NAME, PORT);
    }

    @Override
    void registerResources(ResourceConfig config) {
        config.register( FilesDropboxResources.class );
        config.register( GenericExceptionMapper.class );
//		config.register( CustomLoggingFilter.class);
    }

    public static void main(String[] args) throws Exception {

        Debug.setLogLevel( Level.INFO, Debug.TP);

        //Token.set( args.length == 0 ? "" : args[0] );

        if(Boolean.parseBoolean(args[0]))
            JavaDropboxFiles.resetServerContents();

        new FilesRestDropboxServer().start();
    }
}
