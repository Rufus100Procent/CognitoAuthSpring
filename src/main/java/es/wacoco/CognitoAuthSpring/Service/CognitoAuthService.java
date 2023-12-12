package es.wacoco.CognitoAuthSpring.Service;

import es.wacoco.CognitoAuthSpring.Cognito.AwsCredentials;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.List;
import java.util.Map;

@Service
public class CognitoAuthService {

    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthService.class);
    private final AwsCredentials awsCredentials;

    @Autowired
    public CognitoAuthService(AwsCredentials awsCredentials) {
        this.awsCredentials = awsCredentials;
    }


    // Exception Handling method
    private void handleCognitoException(String message, String username, Exception e) {
        logger.error(message, username, e);
        throw new CognitoServiceException(message, e);
    }

    // Session Management method
    public String getCurrentLoggedInUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("loggedInUsername");
        }
        return null;
    }

    public void signUp(String username, String password, String email) {
        try {
            logger.info("Initiating user sign-up for username: {}", username);
            SignUpRequest request = SignUpRequest.builder()
                    .clientId(awsCredentials.getCognitoClientId())
                    .username(username)
                    .password(password)
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("custom:UserRole").value("Default").build()
                    )
                    .build();

            awsCredentials.getCognitoClient().signUp(request);

            // Log confirmation message
            logger.info("User sign-up successful for username: {}", username);
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error during user sign-up", username, e);
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
            addUserToGroup(username, "Default");

            // Log confirmation message
            logger.info("Confirmation successful for username: {}", username);
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error during confirmation for username: {}", username, e);
        }
    }

    public AuthenticationResultType signIn(String username, String password, HttpServletRequest request) {
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
            HttpSession session = request.getSession();
            session.setAttribute("loggedInUsername", username);

            // Log confirmation message
            logger.info("User sign-in successful for username: {}", username);
            return authResult;
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error during user sign-in for username: {}", username, e);
            throw e;
        }
    }

    public void changePassword(String username, String newPassword, String previousPassword) {
        try {
            AdminSetUserPasswordRequest passwordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .username(username)
                    .password(previousPassword)
                    .password(newPassword)
                    .permanent(true)
                    .build();

            awsCredentials.getCognitoClient().adminSetUserPassword(passwordRequest);

            // Log confirmation message
            logger.info("Password set successfully for user: {}", username);
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error setting password for username: {}", username, e);
        }
    }

    public void deleteUser(String username, HttpServletRequest request) {
        try {
            String loggedInUsername = getCurrentLoggedInUsername(request);
            logger.info("Logged-in Username: {}", loggedInUsername);
            logger.info("Target Username: {}", username);

            // Check if the current user is an admin
            if (isAdmin(loggedInUsername)) {
                // Check if the user is in the "Default" group
                if (isInDefaultGroup(username)) {
                    // If the user is in the "Default" group, show a message
                    String errorMessage = "You are not allowed to delete a user from the 'Default' group.";
                    logger.warn(errorMessage);
                    throw new IllegalStateException(errorMessage);
                }

                AdminDeleteUserRequest deleteUserRequest = AdminDeleteUserRequest.builder()
                        .username(username)
                        .userPoolId(awsCredentials.getCognitoPoolId())
                        .build();

                awsCredentials.getCognitoClient().adminDeleteUser(deleteUserRequest);

                // Log confirmation message
                logger.info("User deletion successful for username: {}", username);
            } else {
                // If not an admin, show a message
                String errorMessage = "You are not allowed to perform this action. User " + loggedInUsername + " is not an admin.";
                logger.warn(errorMessage);
                throw new IllegalStateException(errorMessage);
            }
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error during user deletion for username: {}", username, e);
        }
    }

    public List<String> listUserAttributes(HttpServletRequest request) {
        try {
            String loggedInUsername = getCurrentLoggedInUsername(request);
            AdminGetUserRequest adminGetUserRequest = AdminGetUserRequest.builder()
                    .username(loggedInUsername)
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .build();

            AdminGetUserResponse adminGetUserResponse = awsCredentials.getCognitoClient().adminGetUser(adminGetUserRequest);

            List<AttributeType> userAttributes = adminGetUserResponse.userAttributes();
            for (AttributeType attribute : userAttributes) {
                logger.info("Attribute - Name: {}, Value: {}", attribute.name(), attribute.value());
            }

            // Extract attribute names
            return userAttributes.stream()
                    .map(AttributeType::name)
                    .toList();
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error listing user attributes", null, e);
            throw e;
        }
    }

    public ListUsersResponse listUsers(HttpServletRequest request) {
        try {
            ListUsersRequest listUsersRequest = ListUsersRequest.builder()
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .build();

            ListUsersResponse listUsersResponse = awsCredentials.getCognitoClient().listUsers(listUsersRequest);

            // Log confirmation message
            logger.info("User list retrieval successful");
            return listUsersResponse;
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error during user list retrieval", null, e);
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

            // Log confirmation message
            logger.info("Groups retrieval successful");
            return listGroupsResponse;
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error during groups retrieval", null, e);
            throw e;
        }
    }

    public void addUserToGroup(String username, String groupName) {
        try {
            // Check if the user is being added to the "Admin" group
            if ("Admin".equals(groupName)) {
                // If the user is being added to the "Admin" group, remove them from the "Default" group
                if (isInDefaultGroup(username)) {
                    AdminRemoveUserFromGroupRequest removeUserFromGroupRequest = AdminRemoveUserFromGroupRequest.builder()
                            .groupName("Default")
                            .username(username)
                            .userPoolId(awsCredentials.getCognitoPoolId())
                            .build();

                    awsCredentials.getCognitoClient().adminRemoveUserFromGroup(removeUserFromGroupRequest);

                    // Log confirmation message
                    logger.info("User removed from 'Default' group: {}", username);
                }
            }

            // Add the user to the specified group
            AdminAddUserToGroupRequest addUserToGroupRequest = AdminAddUserToGroupRequest.builder()
                    .groupName(groupName)
                    .username(username)
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .build();

            awsCredentials.getCognitoClient().adminAddUserToGroup(addUserToGroupRequest);

            // Log confirmation message
            logger.info("User added to group: {} - {}", username, groupName);
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error adding user to group", username, e);
        }
    }
    public boolean isInDefaultGroup(String username) {
        try {
            AdminListGroupsForUserResponse groupsForUserResponse = adminListGroupsForUser(username);
            List<GroupType> groups = groupsForUserResponse.groups();

            for (GroupType group : groups) {
                if ("Default".equals(group.groupName())) {
                    return true;
                }
            }
            return false;
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error checking group for username: {}", username, e);
            throw e;
        }
    }

    public boolean isAdmin(String username) {
        try {
            AdminListGroupsForUserResponse groupsForUserResponse = adminListGroupsForUser(username);
            List<GroupType> groups = groupsForUserResponse.groups();

            for (GroupType group : groups) {
                if ("Admin".equals(group.groupName())) {
                    return true;
                }
            }
            return false;
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error checking isAdmin for username: {}", username, e);
            throw e;
        }
    }


    public AdminListGroupsForUserResponse adminListGroupsForUser(String username) {
        try {
            AdminListGroupsForUserRequest listGroupsRequest = AdminListGroupsForUserRequest.builder()
                    .username(username)
                    .userPoolId(awsCredentials.getCognitoPoolId())
                    .build();

            return awsCredentials.getCognitoClient().adminListGroupsForUser(listGroupsRequest);
        } catch (CognitoIdentityProviderException e) {
            handleCognitoException("Error listing groups for user for username: {}", username, e);
            throw e;
        }
    }

    // Custom Exception for Cognito Service
    public static class CognitoServiceException extends RuntimeException {
        public CognitoServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}