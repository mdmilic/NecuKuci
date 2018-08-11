package rs.necukuci;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.amazonaws.mobile.auth.ui.SignInUI;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;

import rs.necukuci.permissions.PermissionChecker;
import rs.necukuci.service.LocationCollectionService;
import rs.necukuci.service.ServiceHelper;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = "AuthActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        startLocationCollectionService();
    }

    private void initAWSMobileClient() {
        // Add a call to initialize AWSMobileClient
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(final AWSStartupResult awsStartupResult) {
                Log.i("SignIn", "Init signing " + awsStartupResult);
                final SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(AuthActivity.this, SignInUI.class);
                signin.login(AuthActivity.this, MainActivity.class).execute();
            }
        }).execute();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String permissions[], @NonNull final int[] grantResults) {
        switch (requestCode) {
            case PermissionChecker.LOCATION_PERMISSION_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Location permissions granted!");
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    startLocationCollectionService();
                } else {
                    Log.w(TAG, "Location permissions denied!!!");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
            }
        }
    }

    private void startLocationCollectionService() {
        Log.i(TAG, "Starting location service from Auth");
        final Intent locationCollectionServiceIntent = new Intent(this, LocationCollectionService.class);

        if (PermissionChecker.checkLocationPermission(this)) {
            if (!ServiceHelper.isServiceRunning(this, LocationCollectionService.class)) {
                this.startForegroundService(locationCollectionServiceIntent);
            } else {
                Log.i(TAG, "Location collection service already started!");
            }
            initAWSMobileClient();
        }
    }
}
