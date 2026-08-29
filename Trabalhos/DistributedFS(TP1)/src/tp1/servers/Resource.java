package tp1.servers;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.function.Function;
import java.util.function.Supplier;

import tp1.api.service.util.Result;

public class Resource {


    protected Resource(){
    }


    protected <T> T getResult(Supplier<Result<T>> resultSupplier, Function<Result.ErrorCode, Response.Status> errorCodeConverter){
        var result = resultSupplier.get();
        if(result.isOK())
            return result.value();
        else
            throw new WebApplicationException(errorCodeConverter.apply(result.error()));
    }

}
