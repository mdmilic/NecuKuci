package rs.necukuci.util;

import android.os.Handler;
import android.os.Looper;

import timber.log.Timber;

public class MainLooperExecutor {

    private static final String TAG = "MainLooperExecutor";

    public static void executeOnMainLoop(final Runnable runnable) {
        if (isMainThread()) {
            Timber.i("on main looper, executing...");
            runnable.run();
        } else {
            Timber.i("not on main looper, dispatching");
            final Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(runnable);
        }
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().isCurrentThread();
    }
}
