package tp1.clients.directory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.net.URI;

import tp1.api.service.util.Directory;


public class DirectoryClientFactory {

    LoadingCache<URI, Directory> cache;


    public DirectoryClientFactory(){
        cache = CacheBuilder.newBuilder().build(
                new CacheLoader<URI, Directory>() {
                    @Override
                    public Directory load(URI uri) {
                        if (uri.toString().endsWith("rest"))
                            return new RestDirectoryClient(uri);
                        else
                            return new SoapDirectoryClient(uri);
                    }
                });

    }


    public Directory getClient(URI serverURI) {
        try{
            return cache.get(serverURI);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}

