package server;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ResponseException extends RuntimeException {

    public enum StatusCode {
        BadRequest,
        Unauthorized,
        AlreadyTaken,
        ServerError,
    }

    final private StatusCode code;

    public ResponseException(StatusCode code, String message) {
        super(message);
        this.code = code;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", code));
    }

    public static ResponseException fromJson(String json) {
        var map = new Gson().fromJson(json, HashMap.class);
        int code = ((Number) map.get("status")).intValue();
        var status = fromHttpStatus(code);
        String msg = map.get("message").toString();
        return new ResponseException(status, msg);
    }

    public StatusCode code() {
        return code;
    }

    public static StatusCode fromHttpStatus(int httpStatusCode) {
        return switch(httpStatusCode) {
            case 400 -> StatusCode.BadRequest;
            case 401 -> StatusCode.Unauthorized;
            case 403 -> StatusCode.AlreadyTaken;
            case 500 -> StatusCode.ServerError;
            default -> throw new IllegalArgumentException("Unknown status code: " + httpStatusCode);
        };
    }

    public int httpStatusCode() {
        return switch(code) {
            case BadRequest -> 400;
            case Unauthorized -> 401;
            case AlreadyTaken -> 403;
            case ServerError -> 500;
        };
    }

    private static class ErrorResponse {
        int status;
        String message;
    }

}
