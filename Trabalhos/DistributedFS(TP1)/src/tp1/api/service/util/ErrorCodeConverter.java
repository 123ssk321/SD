package tp1.api.service.util;

import jakarta.ws.rs.core.Response;


public class ErrorCodeConverter {

    public static Response.Status convertErrorCodeToHttpError(Result.ErrorCode errorCode){
        return switch (errorCode) {
            case OK -> Response.Status.OK;
            case CONFLICT -> Response.Status.CONFLICT;
            case NOT_FOUND -> Response.Status.NOT_FOUND;
            case BAD_REQUEST -> Response.Status.BAD_REQUEST;
            case FORBIDDEN -> Response.Status.FORBIDDEN;
            case INTERNAL_ERROR -> Response.Status.INTERNAL_SERVER_ERROR;
            case NOT_IMPLEMENTED -> Response.Status.NOT_IMPLEMENTED;
        };
    }

    public static Result.ErrorCode convertHttpErrorToErrorCode(Response.Status httpError){
        return switch (httpError) {
            case OK, NO_CONTENT -> Result.ErrorCode.OK;
            case CONFLICT -> Result.ErrorCode.CONFLICT;
            case NOT_FOUND -> Result.ErrorCode.NOT_FOUND;
            case BAD_REQUEST -> Result.ErrorCode.BAD_REQUEST;
            case FORBIDDEN-> Result.ErrorCode.FORBIDDEN;
            case INTERNAL_SERVER_ERROR -> Result.ErrorCode.INTERNAL_ERROR;
            case NOT_IMPLEMENTED-> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> throw new IllegalStateException("Unexpected value: " + httpError);
        };
    }

    public static Result.ErrorCode converterStringErrorToErrorCode(String error){
        return switch(error) {
            case "OK", "NO_CONTENT" -> Result.ErrorCode.OK;
            case "CONFLICT" -> Result.ErrorCode.CONFLICT;
            case "NOT_FOUND" -> Result.ErrorCode.NOT_FOUND;
            case "BAD_REQUEST" -> Result.ErrorCode.BAD_REQUEST;
            case "FORBIDDEN" -> Result.ErrorCode.FORBIDDEN;
            case "INTERNAL_SERVER_ERROR" -> Result.ErrorCode.INTERNAL_ERROR;
            case "NOT_IMPLEMENTED" -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> throw new IllegalStateException("Unexpected value: " + error);
        };
    }
}
