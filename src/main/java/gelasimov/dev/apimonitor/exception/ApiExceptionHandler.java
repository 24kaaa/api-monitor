package gelasimov.dev.apimonitor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiAlreadyRegisteredException.class)
    public ResponseEntity<String> handle(ApiAlreadyRegisteredException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handle400(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiNotFoundException.class)
    public ResponseEntity<String> handle404 (ApiNotFoundException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ApiInactiveException.class)
    public ResponseEntity<String> handle409 (ApiInactiveException exception) {
        return new ResponseEntity<>(exception.getMessage(),HttpStatus.CONFLICT);
    }
}