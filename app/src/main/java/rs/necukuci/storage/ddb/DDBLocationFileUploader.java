package rs.necukuci.storage.ddb;

import java.nio.file.Path;

import rs.necukuci.config.AWSConfig;
import rs.necukuci.util.MainLooperExecutor;

public class DDBLocationFileUploader {
    private AWSConfig awsConfig;

    public DDBLocationFileUploader(final AWSConfig awsConfig) {
        this.awsConfig = awsConfig;
    }

    /**
     * This method MUST be invoked on UI thread. Starts background thread that uploads the files to DDB
     * @param paths file paths that need uploading to DDB
     */
    public void writeLocationFiles(final Path...paths) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new DDBLocationDataStore(awsConfig).execute(paths);
            }
        };
        MainLooperExecutor.executeOnMainLoop(runnable);
    }
}
