package tp.impl.servers.rest.util;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import tp.api.service.rest.RestDirectory;
import util.replication.ReplicationManager;

import java.io.IOException;

public class VersionFilter implements ContainerResponseFilter {
    private ReplicationManager repManager;

    public VersionFilter(ReplicationManager repManager){
        this.repManager = repManager;
    }


    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) throws IOException {
        containerResponseContext.getHeaders().add(RestDirectory.HEADER_VERSION, repManager.getCurrentVersion());
    }
}
