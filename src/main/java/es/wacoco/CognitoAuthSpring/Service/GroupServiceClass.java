package es.wacoco.CognitoAuthSpring.Service;

import es.wacoco.CognitoAuthSpring.Cognito.AwsCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

@Service
public class GroupServiceClass {
    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthService.class);
    private final AwsCredentials awsCredentials;

    @Autowired
    public GroupServiceClass(AwsCredentials awsCredentials) {
        this.awsCredentials = awsCredentials;
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

}
