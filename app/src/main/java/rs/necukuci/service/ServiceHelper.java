package rs.necukuci.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import timber.log.Timber;

public class ServiceHelper {
    public static boolean isServiceRunning(final Activity activity, final Class<?> serviceClass) {
        final ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (final ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Timber.i("Service is already running");
                return true;
            }
        }
        Timber.i("Service is not running yet");
        return false;
    }
}
