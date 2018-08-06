package rs.necukuci.storage.s3;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.nio.file.Path;

import rs.necukuci.config.AWSConfig;
import rs.necukuci.storage.ddb.DDBLocationFileUploader;
import rs.necukuci.util.EmulatorUtils;

public class S3LocationDataStore extends AsyncTask<Path, Void, Void> {
    private static final String TAG = S3LocationDataStore.class.getSimpleName();

    private final TransferUtility transferUtility;

    S3LocationDataStore(final AWSConfig awsConfig) {
        Log.i(TAG, "Creating TransferUtility");
        this.transferUtility = TransferUtility.builder()
                                              .context(awsConfig.getContext())
                                              .s3Client(new AmazonS3Client(AWSConfig.getCredentialsProvider()))
                                              .awsConfiguration(AWSConfig.getAwsConfiguration())
                                              .build();
        Log.i(TAG, "Created TransferUtility");
    }

    @Override
    protected Void doInBackground(final Path... paths) {
        Log.i(TAG, "S3 Executing in background...");
        for (final Path path : paths) {
            try {
                uploadPathToPermanentStorage(path);
            } catch (final Exception e) {
                Log.e(TAG, "Exception uploading file " + path.getFileName(), e);
            }
        }
        return null;
    }

    private void uploadPathToPermanentStorage(final Path path) {
        Log.i(TAG, "Trying to upload file: " + path.toAbsolutePath()); //getFileName());

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

        Log.i(TAG, "Upload attempted to: " + transferObserver.getBucket()+"/"+transferObserver.getKey() + " State: " + transferObserver.getState());
    }

    private String getS3Key(final Path path) {
        if (!IdentityManager.getDefaultIdentityManager().isUserSignedIn()) {
            throw new IllegalStateException("User is not signed in, cant fetch credentials for AWS");
        }

        final String cachedUserID = IdentityManager.getDefaultIdentityManager().getCachedUserID();
        if (EmulatorUtils.isEmulator()) {
            return String.format("private/%s/test/locationsData/%s", cachedUserID, path.getFileName());
        } else {
            return String.format("private/%s/locationsData/%s", cachedUserID, path.getFileName());
        }
    }
}
