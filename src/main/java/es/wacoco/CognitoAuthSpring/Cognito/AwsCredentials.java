package es.wacoco.CognitoAuthSpring.Cognito;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;


@Component
public class AwsCredentials {

    @Value("${aws.cognito.region}")
    private String awsRegion;

    @Value("${aws.cognito.pool-id}")
    private String cognitoPoolId;

    @Value("${aws.cognito.client-id}")
    private String cognitoClientId;

    @Value("${aws.access-key}")
    private String awsAccessKey;

    @Value("${aws.secret-key}")
    private String awsSecretKey;


    public AwsCredentials() {
    }

    public AwsCredentials(String awsRegion, String cognitoPoolId, String cognitoClientId, String awsAccessKey, String awsSecretKey) {
        this.awsRegion = awsRegion;
        this.cognitoPoolId = cognitoPoolId;
        this.cognitoClientId = cognitoClientId;
        this.awsAccessKey = awsAccessKey;
        this.awsSecretKey = awsSecretKey;
    }

    public CognitoIdentityProviderClient getCognitoClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .build();
    }


    public String getCognitoPoolId() {
        return cognitoPoolId;
    }


    public String getCognitoClientId() {
        return cognitoClientId;
    }


}
