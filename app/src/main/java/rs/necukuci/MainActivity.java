package rs.necukuci;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.amazonaws.mobile.auth.core.IdentityManager;

import androidx.work.WorkManager;
import rs.necukuci.permissions.PermissionChecker;
import rs.necukuci.service.LocationCollectionService;
import rs.necukuci.service.LocationUploadWorker;
import rs.necukuci.service.ServiceHelper;
import rs.necukuci.util.EmulatorUtils;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        provider = locationService.getBestProvider(new Criteria(), false);
//        Location location = locationService.getLastKnownLocation(provider);

        if (EmulatorUtils.isEmulator()) {
            Log.i(TAG, "This is EMULATOR build");
        } else {
            Log.i(TAG, "This is NOT EMULATOR build");
        }

        registerSignOutBtn();

        // TODO: Can remove after stable build because of the phone
        WorkManager.getInstance().cancelAllWork();

        if (PermissionChecker.checkNetworkPermission(this)) {
            LocationUploadWorker.scheduleLocationUpload();
        }
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
            case PermissionChecker.INTERNET_PERMISSION_CODE: {
                Log.i(TAG, "Internet permissions granted!");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    LocationUploadWorker.scheduleLocationUpload();
                } else {
                    Log.w(TAG, "Internet permissions denied!!!");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
            }
        }
    }

    private void registerSignOutBtn() {
        // Create log out Button on click listener
        findViewById(R.id.signOutButton).setOnClickListener( new View.OnClickListener() {
            public void onClick(final View v) {
                // Cancel all scheduled work as app wont have permissions to access AWS
                WorkManager.getInstance().cancelAllWork();
                IdentityManager.getDefaultIdentityManager().signOut();
            }
        });
    }

    private void startLocationCollectionService() {
        Log.i(TAG, "Starting location service from Main activity");
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
