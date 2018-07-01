package rs.necukuci.storage.s3;

import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;

public class S3LocationTransferListener implements TransferListener {
    private static final String TAG = S3LocationTransferListener.class.getSimpleName();
    private Runnable runnable;

    public S3LocationTransferListener(final Runnable runnable) {
        this.runnable = runnable;
    }

    // This is executed back on main thread regardless of the thread that constructs it
    @Override
    public void onStateChanged(final int id, final TransferState state) {
        Log.i(TAG, "Upload state changed: " + state);
        //TODO: State is completed even after fail
        if (TransferState.COMPLETED == state) {
//            //upload to DDB
            runnable.run();
        }
    }

    @Override
    public void onProgressChanged(final int id, final long bytesCurrent, final long bytesTotal) {
        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
        int percentDone = (int) percentDonef;

        Log.d(TAG, "S3Upload - ID:" + id + " bytesCurrent: " + bytesCurrent + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
    }

    @Override
    public void onError(final int id, final Exception ex) {
        Log.e(TAG, "Exception uploading file to S3 id: " + id, ex);
    }
}
