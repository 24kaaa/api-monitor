package gelasimov.dev.apimonitor.exception;

public class ApiAlreadyRegisteredException extends RuntimeException {
    public ApiAlreadyRegisteredException(String message) {
        super(message);
    }
}
