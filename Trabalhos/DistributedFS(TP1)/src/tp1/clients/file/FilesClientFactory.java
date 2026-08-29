package tp1.clients.file;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.net.URI;

import tp1.api.service.util.Directory;
import tp1.api.service.util.Files;


public class FilesClientFactory {

    LoadingCache<URI, Files> cache;


    public FilesClientFactory(){
        cache = CacheBuilder.newBuilder().build(
                new CacheLoader<URI, Files>() {
                    @Override
                    public Files load(URI uri) {
                        if (uri.toString().endsWith("rest"))
                            return new RestFilesClient(uri);
                        else
                            return new SoapFilesClient(uri);
                    }
                });
    }


    public Files getClient(URI serverURI) {
        try{
            return cache.get(serverURI);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
