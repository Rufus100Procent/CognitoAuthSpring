package es.wacoco.CognitoAuthSpring.error;


//custom Exception
public class EmailExistsException extends RuntimeException {
    public EmailExistsException(String message) {
        super(message);
    }
}