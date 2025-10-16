package server;

public class BadRequestException extends RuntimeException {
    private final String message;

    public BadRequestException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage () {
        return String.format("{\"message\": \"%s\"}", message);
    }
}
