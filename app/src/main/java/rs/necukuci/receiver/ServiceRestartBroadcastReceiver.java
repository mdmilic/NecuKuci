package rs.necukuci.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import rs.necukuci.service.LocationCollectionService;

public class ServiceRestartBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "RestartReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "Restarting location service " + Process.myTid());
        context.startService(new Intent(context, LocationCollectionService.class));
    }
}
