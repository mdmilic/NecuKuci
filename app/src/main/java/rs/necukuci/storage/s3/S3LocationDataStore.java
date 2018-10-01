package rs.necukuci.storage.s3;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.nio.file.Path;

import rs.necukuci.config.AWSConfig;
import rs.necukuci.storage.ddb.DDBLocationFileUploader;
import rs.necukuci.user.UserManager;
import rs.necukuci.util.EmulatorUtils;
import timber.log.Timber;

public class S3LocationDataStore extends AsyncTask<Path, Void, Void> {
    private final TransferUtility transferUtility;

//  TODO: Change the way upload works, create transfer util once: This exception was thrown after 9h 45min (40th upload) of working fine
//    08-25 10:45:26.872 15433-15433/? E/AndroidRuntime: FATAL EXCEPTION: main
//    Process: rs.necukuci, PID: 15433
//    java.lang.NullPointerException: Attempt to invoke virtual method 'com.amazonaws.auth.CognitoCachingCredentialsProvider com.amazonaws.mobile.auth.core.IdentityManager.getUnderlyingProvider()' on a null object reference
//    at com.amazonaws.mobile.client.AWSMobileClient.getCredentialsProvider(AWSMobileClient.java:229)
//    at rs.necukuci.config.AWSConfig.getCredentialsProvider(AWSConfig.java:17)
//    at rs.necukuci.storage.s3.S3LocationDataStore.<init>(S3LocationDataStore.java:28)
//    at rs.necukuci.storage.s3.S3LocationFileUploader$1.run(S3LocationFileUploader.java:24)
//    at android.os.Handler.handleCallback(Handler.java:790)
//    at android.os.Handler.dispatchMessage(Handler.java:99)
//    at android.os.Looper.loop(Looper.java:164)
//    at android.app.ActivityThread.main(ActivityThread.java:6494)
//    at java.lang.reflect.Method.invoke(Native Method)
//    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:438)
//    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:807)
    S3LocationDataStore(final AWSConfig awsConfig) {
        Timber.i("Creating TransferUtility");
        this.transferUtility = TransferUtility.builder()
                                              .context(awsConfig.getContext())
                                              .s3Client(new AmazonS3Client(awsConfig.getCredentialsProvider()))
                                              .awsConfiguration(awsConfig.getAwsConfiguration())
                                              .build();
        Timber.i("Created TransferUtility");
    }

    @Override
    protected Void doInBackground(final Path... paths) {
        Timber.i("S3 Executing in background...");
        for (final Path path : paths) {
            try {
                uploadPathToPermanentStorage(path);
            } catch (final Exception e) {
                Timber.e(e, "Exception uploading file %s", path.getFileName());
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void aVoid) {
        super.onPostExecute(aVoid);
        Timber.i("S3 Upload async task finished!");
    }

    private void uploadPathToPermanentStorage(final Path path) {
        Timber.i("Trying to upload file: %s", path.toAbsolutePath()); //getFileName());

        // Attach a listener to the observer to get state update and progress notifications

        //TODO: Find a better way
//        final Runnable runnable = () -> new DDBLocationFileUploader(this.awsConfig).writeLocationFiles(path);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new DDBLocationFileUploader().writeLocationFiles(path);
            }
        };

        final TransferListener transferListener = new S3LocationTransferListener(runnable);
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        final String s3Key = getS3Key(path);
        final TransferObserver transferObserver = transferUtility.upload(s3Key,
                                                                         path.toFile(),
                                                                         metadata,
                                                                         null,
                                                                         transferListener);
        Timber.i("Upload attempted to %s/%s: %s", transferObserver.getBucket(), transferObserver.getKey(), transferObserver.getState());
    }

    private String getS3Key(final Path path) {
        final String cachedUserID = UserManager.getUserID();
        if (EmulatorUtils.isEmulator()) {
            return String.format("private/%s/test/locationsData/%s", cachedUserID, path.getFileName());
        } else {
            return String.format("private/%s/locationsData/%s", cachedUserID, path.getFileName());
        }
    }
}
