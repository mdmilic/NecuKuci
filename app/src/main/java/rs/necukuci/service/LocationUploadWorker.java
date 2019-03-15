package rs.necukuci.service;

import android.support.annotation.NonNull;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import rs.necukuci.config.AWSConfig;
import rs.necukuci.storage.s3.S3LocationFileUploader;
import rs.necukuci.util.MainLooperExecutor;
import timber.log.Timber;

import static rs.necukuci.storage.local.LocalFileLocationStoreUtils.isBackedUp;
import static rs.necukuci.storage.local.LocalFileLocationStoreUtils.isCurrentFile;
import static rs.necukuci.storage.local.LocalFileLocationStoreUtils.isValidFileName;

public class LocationUploadWorker extends Worker {
    private static final Duration UPLOAD_REQUEST_INTERVAL = Duration.ofMinutes(15);

    public LocationUploadWorker() {
        super();
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Timber.i("DoWork Initiated: %s", getId());
            Timber.i("DoWork run attempt count: %s", getRunAttemptCount());
            Timber.i("DoWork Tags: %s", getTags());
            return startUpload();
        } catch (final Throwable t) {
            Timber.e(t, "Failed uploading a file if %s", getId());
            return Result.FAILURE;
        }
    }

    private Result startUpload() {
        final String[] fileList = this.getApplicationContext().fileList();
        final S3LocationFileUploader s3LocationFileUploader = new S3LocationFileUploader(new AWSConfig(this.getApplicationContext()));
        final List<String> filesToUpload = new ArrayList<>();
        for (final String fileName : fileList) {
            if (isValidForUpload(fileName)) {
                filesToUpload.add(fileName);
            } else {
                Timber.w("Skipping file %s", fileName);
            }
        }
        Timber.i("Files to upload (%s): %s", filesToUpload.size(), filesToUpload);

        for (int i = 0; i < filesToUpload.size(); i++) {
            final File file = new File(this.getApplicationContext().getFilesDir(), filesToUpload.get(i));

            Timber.i("Starting upload of filePath %s, %s/%s/%s", file.getAbsolutePath(), file.canRead(), file.canWrite(), file.canExecute());

            try {
                Thread.sleep(2000);
                s3LocationFileUploader.uploadFiles(file.toPath());
            } catch (final InterruptedException e) {
                Timber.e("Thread interrupted while sleeping!!!");
            }
            if (i >= 2) {
                break;
            }
        }
        return Result.SUCCESS;
    }

    // TODO: Check if already scheduled
    public static void scheduleLocationUpload() {
        WorkManager.getInstance().cancelAllWork();
        Timber.i("Scheduling uploads from: %s", MainLooperExecutor.isMainThread());
        final Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        final PeriodicWorkRequest periodicLocationUploadRequest = new PeriodicWorkRequest.Builder(LocationUploadWorker.class, UPLOAD_REQUEST_INTERVAL)
                .setConstraints(constraints)
                .addTag("PeriodicLocationDataUpload")
                .build();
        WorkManager.getInstance().enqueueUniquePeriodicWork("PeriodicLocationDataUpload", ExistingPeriodicWorkPolicy.REPLACE, periodicLocationUploadRequest);
    }

    private boolean isValidForUpload(final String fileName) {
        return isValidFileName(fileName)
                && !isBackedUp(fileName)
                && !isCurrentFile(fileName);
    }
}
