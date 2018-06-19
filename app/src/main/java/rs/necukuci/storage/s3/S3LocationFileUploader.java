package rs.necukuci.storage.s3;

import java.nio.file.Path;

import rs.necukuci.config.AWSConfig;
import rs.necukuci.util.MainLooperExecutor;

public class S3LocationFileUploader {
//    private final S3LocationDataStore dataStore;
    private final AWSConfig awsConfig;
    public S3LocationFileUploader(final AWSConfig awsConfig) {
//        this.dataStore = new S3LocationDataStore(awsConfig);
        this.awsConfig = awsConfig;
    }

    /**
     * This method MUST be invoked on UI thread. Starts background thread that uploads the files to S3
     * @param paths file paths that need uploading to S3
     */
    public void uploadFiles(final Path...paths) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new S3LocationDataStore(awsConfig).execute(paths);
            }
        };

        MainLooperExecutor.executeOnMainLoop(runnable);
    }
}
