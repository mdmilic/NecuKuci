package rs.necukuci.user;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.facebook.AccessToken;

import timber.log.Timber;

public class UserManager {
    public static String getUserID() {
        if (!IdentityManager.getDefaultIdentityManager().isUserSignedIn()) {
            throw new IllegalStateException("User is not signed in, cant fetch credentials for AWS"); // TODO: Transition to login screen instead of crash
        } else {
            Timber.i("User signed in as: %s", IdentityManager.getDefaultIdentityManager().getCachedUserID());
            Timber.i("Facebook Access token: %s", AccessToken.getCurrentAccessToken());
        }
        return IdentityManager.getDefaultIdentityManager().getCachedUserID();
    }
}
