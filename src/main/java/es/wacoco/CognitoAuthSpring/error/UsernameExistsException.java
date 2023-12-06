package es.wacoco.CognitoAuthSpring.error;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String message) {
        super(message);
    }
}
