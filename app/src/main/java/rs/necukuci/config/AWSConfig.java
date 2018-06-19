package rs.necukuci.config;

import android.content.Context;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;

import lombok.Data;

@Data
public class AWSConfig {

    private final Context context;

    public static AWSCredentialsProvider getCredentialsProvider() {
        return AWSMobileClient.getInstance().getCredentialsProvider();
    }

    public static AWSConfiguration getAwsConfiguration() {
        return AWSMobileClient.getInstance().getConfiguration();
    }
}
