package rs.necukuci.util;

import com.crashlytics.android.Crashlytics;

public class CrashlyticsUtil {

    public static void logUser(final String userId) {
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(userId);
//        Crashlytics.setUserEmail("user@fabric.io");
//        Crashlytics.setUserName("Test User");
    }
}
