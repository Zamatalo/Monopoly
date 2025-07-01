package util.exceptions;

public class ResponseSerializationException extends RuntimeException {
    public ResponseSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
