package rs.necukuci.service;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import androidx.work.OneTimeWorkRequest;
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
    public WorkerResult doWork() {
        return startUpload();
    }

    private WorkerResult startUpload() {
        final AWSConfig awsConfig = new AWSConfig(this.getApplicationContext());
        final S3LocationFileUploader s3LocationFileUploader = new S3LocationFileUploader(awsConfig);
        Log.i(TAG, Arrays.toString(this.getApplicationContext().fileList()));
        for (final String fileName : this.getApplicationContext().fileList()) {
            if (fileName.startsWith("myLocation")
                    || fileName.startsWith("locationCallback")
                    || fileName.startsWith("locationListener")
                    || isCurrentFile(fileName)) {
                final File file = new File(this.getApplicationContext().getFilesDir(), fileName);
                Log.i(TAG, "Starting upload of file " + fileName);

                s3LocationFileUploader.uploadFiles(file.toPath());
                break;
            } else {
                Log.w(TAG, "Skipping file " + fileName);
            }

        }
        return WorkerResult.SUCCESS;
    }

    public static void scheduleLocationUpload() {
        Log.i(TAG, "Scheduling uploads from: " + MainLooperExecutor.isMainThread());
//        final Constraints constraints = new Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build();
//        final OneTimeWorkRequest locationUploadRequest = new OneTimeWorkRequest.Builder(LocationUploadWorker.class)
//                .setConstraints(constraints)
//                .build();
        WorkManager.getInstance().enqueue(OneTimeWorkRequest.from(LocationUploadWorker.class));
//        WorkManager.getInstance().enqueue(locationUploadRequest);
    }

    private boolean isCurrentFile(final String fileName) {
        return fileName.contains(Instant.now().truncatedTo(ChronoUnit.DAYS).toString());
    }
}
