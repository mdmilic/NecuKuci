package rs.necukuci;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import rs.necukuci.permissions.PermissionChecker;
import rs.necukuci.service.LocationCollectionService;
import rs.necukuci.service.ServiceHelper;
import rs.necukuci.util.CrashlyticsTree;
import timber.log.Timber;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Fabric.with(this, new Crashlytics());
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        Timber.plant(new CrashlyticsTree());

        startLocationCollectionService();
    }

    private void initAWSMobileClient() {
        Timber.i("Initing AWS mobile client");
        // Add a call to initialize AWSMobileClient
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(final AWSStartupResult awsStartupResult) {
                registerSignInListener();
                showSignIn();
            }
        }).execute();
//        AccessToken.getCurrentAccessToken();
//        AWSMobileClient.getInstance().initialize(this).execute();
        // Sign-in listener
//        registerSignInListener();
//        showSignIn();
    }

    private void registerSignInListener() {
        IdentityManager.getDefaultIdentityManager().addSignInStateChangeListener(new SignInStateChangeListener() {
            @Override
            public void onUserSignedIn() {
                Timber.d("User Signed In");
            }

            // Sign-out listener
            @Override
            public void onUserSignedOut() {
                Timber.d("User Signed Out");
                showSignIn();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String permissions[], @NonNull final int[] grantResults) {
        switch (requestCode) {
            case PermissionChecker.LOCATION_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.i("Location permissions granted!");
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    startLocationCollectionService();
                } else {
                    Timber.w("Location permissions denied!!!");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
            }
        }
    }

    private void showSignIn() {
        Timber.i("Showing SingIn UI ");
        final SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(AuthActivity.this, SignInUI.class);
        signin.login(AuthActivity.this, NavigationActivity.class).execute();
    }

    private void startLocationCollectionService() {
        Timber.i("Starting location service from Auth");
        Crashlytics.log("Starting location service from Auth");
        final Intent locationCollectionServiceIntent = new Intent(this, LocationCollectionService.class);

        if (PermissionChecker.checkLocationPermission(this)) {
            if (!ServiceHelper.isServiceRunning(this, LocationCollectionService.class)) {
                this.startForegroundService(locationCollectionServiceIntent);

                final View view = findViewById(R.id.authActivityID);
                Snackbar.make(view, "NecuKuci started tracking your travel", Snackbar.LENGTH_LONG).show();
            } else {
                Timber.i("Location collection service already started!");
            }
            initAWSMobileClient();
        }
    }
}
