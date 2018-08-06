package rs.necukuci.storage.ddb;

import java.nio.file.Path;

import rs.necukuci.util.MainLooperExecutor;

public class DDBLocationFileUploader {
    /**
     * This method MUST be invoked on UI thread. Starts background thread that uploads the files to DDB
     * @param paths file paths that need uploading to DDB
     */
    public void writeLocationFiles(final Path...paths) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new DDBLocationDataStore().execute(paths);
            }
        };
        MainLooperExecutor.executeOnMainLoop(runnable);
    }
}
