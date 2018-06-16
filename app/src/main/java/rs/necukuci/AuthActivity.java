package rs.necukuci;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v7.app.*;
import android.util.*;

import com.amazonaws.mobile.auth.ui.*;
import com.amazonaws.mobile.client.*;

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
        // Add a call to initialize AWSMobileClient
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override
            public void onComplete(AWSStartupResult awsStartupResult) {
                Log.i("SignIn", "Init signing");
                final SignInUI signin = (SignInUI) AWSMobileClient.getInstance().getClient(AuthActivity.this, SignInUI.class);
                signin.login(AuthActivity.this, MainActivity.class).execute();
            }
        }).execute();

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String permissions[], @NonNull final int[] grantResults) {
        switch (requestCode) {
            case PermissionChecker.LOCATION_PERMISSION_CODE: {
                Log.i(TAG, "Location permissions granted!");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
//                    requestLocationUpdates();
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
        final Intent locationCollectionServiceIntent = new Intent(this, LocationCollectionService.class);

        if (PermissionChecker.checkLocationPermission(this)) {
            if (!ServiceHelper.isServiceRunning(this, LocationCollectionService.class)) {
                this.startForegroundService(locationCollectionServiceIntent);
            } else {
                Log.i(TAG, "Location collection service already started!");
            }
        }
    }
}
