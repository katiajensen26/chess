package service;

public class ErrorException extends Exception {
    private final String message;

    public ErrorException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage () {
        return String.format("{\"message\": \"%s\"}", message);
    }
}
