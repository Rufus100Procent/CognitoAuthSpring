package es.wacoco.CognitoAuthSpring.Service;

import es.wacoco.CognitoAuthSpring.Cognito.AwsCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

@Service
public class CognitoAuthService {

    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthService.class);
    private final AwsCredentials awsCredentials;
    private final GroupServiceClass groupServiceClass;
    @Autowired
    public CognitoAuthService(AwsCredentials awsCredentials, GroupServiceClass groupServiceClass) {
        this.awsCredentials = awsCredentials;
        this.groupServiceClass = groupServiceClass;
    }

    public void signUp(String username, String password, String email) {
        try {
            logger.info("Initiating user sign-up for username: {}", username);
            SignUpRequest request = SignUpRequest.builder()
                    .clientId(awsCredentials.getCognitoClientId())
                    .username(username)
                    .password(password)
                    .userAttributes(AttributeType.builder().name("email").value(email).build())
                    .build();

            // Perform user sign-up
            awsCredentials.getCognitoClient().signUp(request);

            // Add the user to the "Default" group
            groupServiceClass.addUserToGroup(username, "Default");

            logger.info("User sign-up successful for username: {}", username);
        } catch (Exception e) {
            logger.error("Error during user sign-up for username: {}", username, e);
            throw e;
        }
    }
    public void confirmSignUp(String username, String confirmationCode) {
        try {
            logger.info("Confirming sign-up for username: {}", username);
            ConfirmSignUpRequest request = ConfirmSignUpRequest.builder()
                    .clientId(awsCredentials.getCognitoClientId())
                    .username(username)
                    .confirmationCode(confirmationCode)
                    .build();

            awsCredentials.getCognitoClient().confirmSignUp(request);
            logger.info("Confirmation successful for username: {}", username);
        } catch (Exception e) {
            logger.error("Error during confirmation for username: {}", username, e);
            throw e;
        }
    }

    public AuthenticationResultType signIn(String username, String password) {
        try {
            logger.info("Initiating user sign-in for username: {}", username);
            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(
                            Map.of(
                                    "USERNAME", username,
                                    "PASSWORD", password
                            )
                    )
                    .clientId(awsCredentials.getCognitoClientId())
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .build();

            AdminInitiateAuthResponse authResponse = awsCredentials.getCognitoClient().adminInitiateAuth(authRequest);

            AuthenticationResultType authResult = authResponse.authenticationResult();
            logger.info("User sign-in successful for username: {}", username);
            return authResult;
        } catch (Exception e) {
            logger.error("Error during user sign-in for username: {}", username, e);
            throw e;
        }
    }


    public void changePassword(String username, String newPassword) {
        try {
            AdminSetUserPasswordRequest passwordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .username(username)
                    .password(newPassword)
                    .permanent(true)
                    .build();

            awsCredentials.getCognitoClient().adminSetUserPassword(passwordRequest);
            logger.info("Password set successfully for user: {}", username);
        } catch (Exception e) {
            logger.error("Error setting password for user: {}", username, e);
            throw e;
        }
    }

    public void deleteUser(String username) {
        try {
            logger.info("Deleting user: {}", username);

            AdminDeleteUserRequest deleteUserRequest = AdminDeleteUserRequest.builder()
                    .username(username)
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .build();

            awsCredentials.getCognitoClient().adminDeleteUser(deleteUserRequest);

            logger.info("User deletion successful for username: {}", username);
        } catch (Exception e) {
            logger.error("Error during user deletion for username: {}", username, e);
            throw e;
        }
    }
}
