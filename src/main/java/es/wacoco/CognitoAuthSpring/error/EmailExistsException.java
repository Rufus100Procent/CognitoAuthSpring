package es.wacoco.CognitoAuthSpring.error;

public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String message) {
        super(message);
    }
}