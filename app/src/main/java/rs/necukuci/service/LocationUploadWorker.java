package rs.necukuci.service;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.time.Duration;
import java.util.Arrays;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import rs.necukuci.config.AWSConfig;
import rs.necukuci.storage.local.LocalFileLocationStoreUtils;
import rs.necukuci.storage.s3.S3LocationFileUploader;
import rs.necukuci.util.MainLooperExecutor;

public class LocationUploadWorker extends Worker {

    private static final String TAG = LocationUploadWorker.class.getSimpleName();

    public LocationUploadWorker() {
        super();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {

        Log.i(TAG, "DoWork Initiated: " + getId());
        Log.i(TAG, "DoWork run attempt count: " + getRunAttemptCount());
        Log.i(TAG, "DoWork Tags: " + getTags());
        return startUpload();
        } catch (final Throwable t) {
            Log.e(TAG, "Failed uploading file: ", t);
            return Result.FAILURE;
        }
    }

    private Result startUpload() {
        Log.i(TAG, "Files to upload: " + Arrays.toString(this.getApplicationContext().fileList()));
        final S3LocationFileUploader s3LocationFileUploader = new S3LocationFileUploader(new AWSConfig(this.getApplicationContext()));
        for (final String fileName : this.getApplicationContext().fileList()) {
            if (isValidFileName(fileName) && !isCurrentFile(fileName)) {
                final File file = new File(this.getApplicationContext().getFilesDir(), fileName);
                Log.i(TAG, String.format("Can read %s, write %s, ex %s", file.canRead(), file.canWrite(), file.canExecute()));
                Log.i(TAG, "Starting upload of filePath " + file.getAbsolutePath());

                try {
                    Thread.sleep(2000);
                    s3LocationFileUploader.uploadFiles(file.toPath());
                } catch (final InterruptedException e) {
                    Log.e(TAG, "Thread interrupted while sleeping!!!");
                }
            } else {
                Log.w(TAG, "Skipping file " + fileName);
            }
        }
        return Result.SUCCESS;
    }

    public static void scheduleLocationUpload() {
        WorkManager.getInstance().cancelAllWork();
        Log.i(TAG, "Scheduling uploads from: " + MainLooperExecutor.isMainThread());
        final Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        final PeriodicWorkRequest periodicLocationUploadRequest = new PeriodicWorkRequest.Builder(LocationUploadWorker.class, Duration.ofMinutes(30))
                .setConstraints(constraints)
                .addTag("PeriodicLocationDataUpload")
                .build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("PeriodicLocationDataUpload", ExistingPeriodicWorkPolicy.REPLACE, periodicLocationUploadRequest);
    }

    private boolean isValidFileName(final String fileName) {
        return (fileName.startsWith("myLocation")
                || fileName.startsWith("locationCallback")
                || fileName.startsWith("locationListener"))
                && !fileName.endsWith("bak");
    }

    private boolean isCurrentFile(final String fileName) {
        return fileName.contains(LocalFileLocationStoreUtils.getCurrentFileName());
    }
}
