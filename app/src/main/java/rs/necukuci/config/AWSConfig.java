package rs.necukuci.config;

import android.content.Context;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;

import lombok.Data;

@Data
public class AWSConfig {

    private final Context context;
    private final AWSConfiguration awsConfiguration;
    private final AWSCredentialsProvider credentialsProvider;

    public AWSConfig(final Context context) {
        final AWSConfiguration awsConfiguration = new AWSConfiguration(context);
        this.context = context;
        this.awsConfiguration = awsConfiguration;
        this.credentialsProvider = getMobileHubCredentialsProvider(); // TODO: Change to the one below. Still use this for now as otherwise TransferUtility doesn't have access for some reason
//        this.credentialsProvider = new CognitoCachingCredentialsProvider(context, awsConfiguration);
    }

    // https://github.com/aws/aws-sdk-android/issues/420
    // "we recommend using CognitoCachingCredentialsProvider and AmazonS3Client to create TransferUtility rather than using AWSMobileClient.
    // We are taking it as a feature request to add support for application context in AWSMobileClient"

    public static AWSCredentialsProvider getMobileHubCredentialsProvider() {
        return AWSMobileClient.getInstance().getCredentialsProvider();
    }

    public static AWSConfiguration getMobileHubAwsConfiguration() {
        return AWSMobileClient.getInstance().getConfiguration();
    }
}
