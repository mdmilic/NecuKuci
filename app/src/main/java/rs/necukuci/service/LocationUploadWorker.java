package rs.necukuci.service;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import rs.necukuci.config.AWSConfig;
import rs.necukuci.storage.s3.S3LocationFileUploader;

public class LocationUploadWorker extends Worker {

    private static final String TAG = LocationUploadWorker.class.getSimpleName();
    private final S3LocationFileUploader s3LocationFileUploader;

    public LocationUploadWorker() {
        super();
        Log.i(TAG, "Creating Worker");
        final AWSConfig awsConfig = new AWSConfig(this.getApplicationContext());
        this.s3LocationFileUploader = new S3LocationFileUploader(awsConfig);
    }

    @NonNull
    @Override
    public WorkerResult doWork() {
        return startUpload();
    }

    private WorkerResult startUpload() {
        Log.i(TAG, Arrays.toString(this.getApplicationContext().fileList()));
        final File file3 = new File(this.getApplicationContext().getFilesDir(), this.getApplicationContext().fileList()[3]);
        s3LocationFileUploader.uploadFiles(file3.toPath());
        return WorkerResult.SUCCESS;
    }

    public static void scheduleLocationUpload() {
        final Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        final OneTimeWorkRequest locationUploadRequest = new OneTimeWorkRequest.Builder(LocationUploadWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance().enqueue(locationUploadRequest);
    }
}
