package tp1.api.util;

import java.net.URI;


public class URLConfigurator {

    public static URI[] getURLFor(Discovery discovery, String serviceName){
        URI[] serverUri = null;
        while (serverUri == null){
            serverUri = discovery.knownUrisOf(serviceName);
        }
        return serverUri;
    }
}
