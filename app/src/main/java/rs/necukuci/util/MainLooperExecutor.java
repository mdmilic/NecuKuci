package rs.necukuci.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class MainLooperExecutor {

    private static final String TAG = "MainLooperExecutor";

    public static void executeOnMainLoop(final Runnable runnable) {
        if (isMainThread()) {
            Log.i(TAG, "on main looper, executing...");
            runnable.run();
        } else {
            Log.i(TAG, "not on main looper, dispatching");
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(runnable);
        }
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().isCurrentThread();
    }
}
