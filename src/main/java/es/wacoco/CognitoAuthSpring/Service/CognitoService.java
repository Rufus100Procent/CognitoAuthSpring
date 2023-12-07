package es.wacoco.CognitoAuthSpring.Service;

import es.wacoco.CognitoAuthSpring.Cognito.AwsCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

@Service
/*
// *** to do *******
* many error handling is needed,
* only admin need to delete a user and add a user to a group
* defaultuser cannot delete or add someone to a group, it will get error message "you are not in admin group" or you dont have permitions
* the class is getting biger, create diffrent classes, for example login/register/confirm/delete is in one class, and list/group is another
* change password needs to be added
* method that looks if a user is in a default group or admin, and create button if defaultuser logs in and clicks it, the user gets message and if it is admin it diffrent message
* documentations
* Write unit test in src/test/java*/
public class CognitoService {

    private static final Logger logger = LoggerFactory.getLogger(CognitoService.class);
    private final AwsCredentials awsCredentials;

    @Autowired
    public CognitoService(AwsCredentials awsCredentials) {
        this.awsCredentials = awsCredentials;
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

            // Add the user to the "defaultUser" group
            addUserToGroup(username, "Default");

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


    public ListUsersResponse listUsers() {
        try {
            logger.info("Listing all users");

            ListUsersRequest listUsersRequest = ListUsersRequest.builder()
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .build();

            ListUsersResponse listUsersResponse = awsCredentials.getCognitoClient().listUsers(listUsersRequest);

            logger.info("User list retrieval successful");
            return listUsersResponse;
        } catch (Exception e) {
            logger.error("Error during user list retrieval", e);
            throw e;
        }
    }

    public ListGroupsResponse listAllGroups() {
        try {
            logger.info("Listing all groups");
            ListGroupsRequest listGroupsRequest = ListGroupsRequest.builder()
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .build();

            ListGroupsResponse listGroupsResponse = awsCredentials.getCognitoClient().listGroups(listGroupsRequest);

            logger.info("Groups retrieval successful");
            return listGroupsResponse;
        } catch (Exception e) {
            logger.error("Error during groups retrieval", e);
            throw e;
        }
    }


    public void addUserToGroup(String username, String groupName) {
        try {
            logger.info("Adding user {} to group {}", username, groupName);

            AdminAddUserToGroupRequest addUserToGroupRequest = AdminAddUserToGroupRequest.builder()
                    .groupName(groupName)
                    .username(username)
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .build();

            awsCredentials.getCognitoClient().adminAddUserToGroup(addUserToGroupRequest);

            logger.info("User added to group successfully");
        } catch (Exception e) {
            logger.error("Error adding user to group", e);
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
