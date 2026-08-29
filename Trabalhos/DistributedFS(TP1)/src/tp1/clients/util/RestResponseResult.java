package tp1.clients.util;

import jakarta.ws.rs.core.Response;

import java.util.function.Supplier;

import tp1.api.service.util.ErrorCodeConverter;
import tp1.api.service.util.Result;


public class RestResponseResult {

    public static <T> Result<T> getResult(Response r, int responseStatus, boolean successCondition){
        return RestResponseResult.getResult(r, responseStatus, successCondition, null);
    }

    public static <T> Result<T> getResult(Response r, int responseStatus, boolean successCondition, Supplier<T> func){
        if(successCondition) {
            if(func == null)
                return Result.ok();
            else
                return Result.ok(func.get());
        } else
            System.out.println("Error, HTTP error status: " + r.getStatus() );
        return Result.error(ErrorCodeConverter.convertHttpErrorToErrorCode(Response.Status.fromStatusCode(responseStatus)));
    }

}
