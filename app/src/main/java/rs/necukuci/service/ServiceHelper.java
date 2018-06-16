package rs.necukuci.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

public class ServiceHelper {
    public static boolean isServiceRunning(final Activity activity, final Class<?> serviceClass) {
        final ActivityManager manager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}
