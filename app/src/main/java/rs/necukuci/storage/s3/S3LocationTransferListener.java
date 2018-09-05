package rs.necukuci.storage.s3;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;

import timber.log.Timber;

public class S3LocationTransferListener implements TransferListener {
    private Runnable runnable;

    public S3LocationTransferListener(final Runnable runnable) {
        this.runnable = runnable;
    }

    // This is executed back on main thread regardless of the thread that constructs it
    @Override
    public void onStateChanged(final int id, final TransferState state) {
        Timber.i("Upload state changed[%s]: %s", id, state);
        if (TransferState.COMPLETED == state) {
//            //upload to DDB
            runnable.run();
        }
    }

    @Override
    public void onProgressChanged(final int id, final long bytesCurrent, final long bytesTotal) {
        float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
        int percentDone = (int) percentDonef;

        Timber.d("S3Upload[%s] %s bytesCurrent: %s bytesTotal: %s", id, percentDone, bytesCurrent, bytesTotal);
    }

    @Override
    public void onError(final int id, final Exception ex) {
        Timber.e(ex, "Exception uploading file to S3 id: %s", id);
    }
}
