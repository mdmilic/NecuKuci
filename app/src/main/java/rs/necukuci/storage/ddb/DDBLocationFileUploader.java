package rs.necukuci.storage.ddb;

import java.nio.file.Path;

import rs.necukuci.config.AWSConfig;

public class DDBLocationFileUploader {
    private static final String TAG = "DDBFileUploader";
    private AWSConfig awsConfig;

    public DDBLocationFileUploader(final AWSConfig awsConfig) {
        this.awsConfig = awsConfig;
    }


    public void writeLocationFiles(final Path...paths) {
        new DDBLocationDataStore(awsConfig).execute(paths);
    }
}
