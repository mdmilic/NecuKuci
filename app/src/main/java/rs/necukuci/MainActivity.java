package rs.necukuci;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import androidx.work.WorkManager;
import rs.necukuci.permissions.PermissionChecker;
import rs.necukuci.service.LocationCollectionService;
import rs.necukuci.service.LocationUploadWorker;
import rs.necukuci.service.ServiceHelper;
import rs.necukuci.user.UserManager;
import rs.necukuci.util.CrashlyticsUtil;
import rs.necukuci.util.EmulatorUtils;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        provider = locationService.getBestProvider(new Criteria(), false);
//        Location location = locationService.getLastKnownLocation(provider);

        final String cachedUserID = UserManager.getUserID();
        CrashlyticsUtil.logUser(cachedUserID);
        if (EmulatorUtils.isEmulator()) {
            Timber.i("This is EMULATOR build, userID=%s", cachedUserID);
        } else {
            Timber.i("This is NOT EMULATOR build, userID=%s", cachedUserID);
        }

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
                Timber.i("Location permissions granted in Main!");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
//                    requestLocationUpdates();
                    startLocationCollectionService();
                } else {
                    Timber.w("Location permissions denied!!!");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
            }
            case PermissionChecker.INTERNET_PERMISSION_CODE: {
                Timber.i("Internet permissions granted!");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    LocationUploadWorker.scheduleLocationUpload();
                } else {
                    Timber.w("Internet permissions denied!!!");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
            }
        }
    }

    private void startLocationCollectionService() {
        Timber.i("Starting location service from Main activity");
        final Intent locationCollectionServiceIntent = new Intent(this, LocationCollectionService.class);

        if (PermissionChecker.checkLocationPermission(this)) {
            if (!ServiceHelper.isServiceRunning(this, LocationCollectionService.class)) {
                this.startForegroundService(locationCollectionServiceIntent);
            } else {
                Timber.i("Location collection service already started!");
            }
        }
    }
}
