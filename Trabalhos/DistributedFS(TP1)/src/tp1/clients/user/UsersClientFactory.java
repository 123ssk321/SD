package tp1.clients.user;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.net.URI;

import tp1.api.service.util.Users;


public class UsersClientFactory {

    LoadingCache<URI, Users> cache;


    public UsersClientFactory(){
        cache = CacheBuilder.newBuilder().build(
                new CacheLoader<URI, Users>() {
                    @Override
                    public Users load(URI uri) {
                        if (uri.toString().endsWith("rest"))
                            return new RestUsersClient(uri);
                        else
                            return new SoapUsersClient(uri);
                    }
                });
    }


    public Users getClient(URI serverURI) {
        try {
            return cache.get(serverURI);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
