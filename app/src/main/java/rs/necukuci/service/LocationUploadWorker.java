package rs.necukuci.service;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import rs.necukuci.config.AWSConfig;
import rs.necukuci.storage.s3.S3LocationFileUploader;
import rs.necukuci.util.MainLooperExecutor;

public class LocationUploadWorker extends Worker {

    private static final String TAG = LocationUploadWorker.class.getSimpleName();
//    private final S3LocationFileUploader s3LocationFileUploader;

    public LocationUploadWorker() {
        super();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "DoWork Initiated: " + getId());
        Log.i(TAG, "DoWork run attempt count: " + getRunAttemptCount());
        Log.i(TAG, "DoWork Tags: " + getTags());
        return startUpload();
    }

    private Result startUpload() {
        final AWSConfig awsConfig = new AWSConfig(this.getApplicationContext());
        final S3LocationFileUploader s3LocationFileUploader = new S3LocationFileUploader(awsConfig);
        Log.i(TAG, "Files to upload: " + Arrays.toString(this.getApplicationContext().fileList()));
        for (final String fileName : this.getApplicationContext().fileList()) {
            if (isValidFileName(fileName) && !isCurrentFile(fileName)) {
                final File file = new File(this.getApplicationContext().getFilesDir(), fileName);
                Log.i(TAG, "Starting upload of file " + fileName);

                Log.i(TAG, String.format("Can read %s, write %s, ex %s", file.canRead(), file.canWrite(), file.canExecute()));
                Log.i(TAG, "Starting upload of filePath " + file.getAbsolutePath());

                s3LocationFileUploader.uploadFiles(file.toPath());
            } else {
                Log.w(TAG, "Skipping file " + fileName);
            }
        }
        return Result.SUCCESS;
    }

    public static void scheduleLocationUpload() {
        Log.i(TAG, "Scheduling uploads from: " + MainLooperExecutor.isMainThread());
        WorkManager.getInstance().cancelAllWork();
        final Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        final PeriodicWorkRequest periodicLocationUploadRequest = new PeriodicWorkRequest.Builder(LocationUploadWorker.class, Duration.ofHours(3))
                .setConstraints(constraints)
                .addTag("PeriodicLocationDataUpload")
                .build();
//        final OneTimeWorkRequest locationUploadRequest = new OneTimeWorkRequest.Builder(LocationUploadWorker.class)
//                .setConstraints(constraints)
//                .build();
//        WorkManager.getInstance().enqueue(OneTimeWorkRequest.from(LocationUploadWorker.class));
//        WorkManager.getInstance().enqueue(locationUploadRequest);
        WorkManager.getInstance().enqueueUniquePeriodicWork("PeriodicLocationDataUpload", ExistingPeriodicWorkPolicy.REPLACE, periodicLocationUploadRequest);
    }

    private boolean isValidFileName(final String fileName) {
        return fileName.startsWith("myLocation")
                || fileName.startsWith("locationCallback")
                || fileName.startsWith("locationListener");
    }
    private boolean isCurrentFile(final String fileName) {
        return fileName.contains(Instant.now().truncatedTo(ChronoUnit.DAYS).toString());
    }
}
