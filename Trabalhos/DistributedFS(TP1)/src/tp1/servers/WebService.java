package tp1.servers;

import java.util.function.Supplier;

import tp1.api.service.util.Result;


public class WebService {


    protected WebService(){
    }


    protected <T> T getResult(Supplier<Result<T>> resultSupplier) throws Exception {
        var result = resultSupplier.get();
        if(result.isOK())
            return result.value();
        else {
            throw new Exception(result.error().name());
        }
    }

}
