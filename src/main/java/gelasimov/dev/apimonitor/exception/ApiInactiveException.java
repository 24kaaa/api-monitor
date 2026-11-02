package gelasimov.dev.apimonitor.exception;

public class ApiInactiveException extends RuntimeException {
    public ApiInactiveException(String message) {
        super(message);
    }
}
