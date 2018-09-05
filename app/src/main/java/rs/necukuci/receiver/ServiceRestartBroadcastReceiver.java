package rs.necukuci.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import rs.necukuci.service.LocationCollectionService;
import timber.log.Timber;

public class ServiceRestartBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        Timber.i("Restarting location service %s", Process.myTid());
        context.startService(new Intent(context, LocationCollectionService.class));
    }
}
