package rs.necukuci.storage.s3;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.nio.file.Path;

import rs.necukuci.config.AWSConfig;
import rs.necukuci.storage.ddb.DDBLocationFileUploader;

public class S3LocationDataStore extends AsyncTask<Path, Void, Void> {
    private static final String TAG = S3LocationDataStore.class.getSimpleName();

    private final TransferUtility transferUtility;
    private final AWSConfig awsConfig;

    S3LocationDataStore(final AWSConfig awsConfig) {
        this.awsConfig = awsConfig;
        this.transferUtility = TransferUtility.builder()
                                              .context(awsConfig.getContext())
                                              .s3Client(new AmazonS3Client(AWSConfig.getCredentialsProvider()))
                                              .awsConfiguration(AWSConfig.getAwsConfiguration())
                                              .build();
    }

    @Override
    protected Void doInBackground(final Path... paths) {
        for (final Path path : paths) {
            try {
                uploadPathToPermanentStorage(path);
            } catch (final Exception e) {
                Log.e(TAG, "Unhandled exception uploading file " + path.getFileName(), e);
            }
        }
        return null;
    }

    private void uploadPathToPermanentStorage(final Path path) {
        Log.i(TAG, "Trying to upload file: " + path.getFileName());

        // Attach a listener to the observer to get state update and progress notifications

        //TODO: Find a better way
//        final Runnable runnable = () -> new DDBLocationFileUploader(this.awsConfig).writeLocationFiles(path);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new DDBLocationFileUploader(S3LocationDataStore.this.awsConfig).writeLocationFiles(path);
            }
        };

        final TransferListener transferListener = new S3LocationTransferListener(runnable);
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/plain");
        final String s3Key = String.format("private/%s/locationsData/%s", "us-east-1:6bd5c573-8cbd-4917-ba39-784747e7cb98", path.getFileName());
        final TransferObserver transferObserver = transferUtility.upload(s3Key,
                                                                         path.toFile(),
                                                                         metadata,
                                                                         null,
                                                                         transferListener);

        Log.i(TAG, "Upload attempted to: " + transferObserver.getBucket()+"/"+transferObserver.getKey() + " State: " + transferObserver.getState());
    }
}
