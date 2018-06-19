package rs.necukuci;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import rs.necukuci.permissions.PermissionChecker;
import rs.necukuci.service.LocationCollectionService;
import rs.necukuci.service.LocationUploadWorker;
import rs.necukuci.service.ServiceHelper;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
//    private DDBLocationFileUploader ddbLocationFileUploader;
//    private S3LocationFileUploader s3LocationFileUploader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "Initing worker manager...");
//        WorkManager.initialize(this.getApplicationContext(), new Configuration.Builder().build());
//        final AWSConfig awsConfig = new AWSConfig(this.getApplicationContext());
//        this.ddbLocationFileUploader = new DDBLocationFileUploader(awsConfig);
//        this.s3LocationFileUploader = new S3LocationFileUploader(awsConfig);
//        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        locationListener = new BackgroundLocationListener(dataStore);


//        provider = locationService.getBestProvider(new Criteria(), false);
//        Location location = locationService.getLastKnownLocation(provider);

//        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
//            @Override
//            public void onComplete(final AWSStartupResult awsStartupResult) {
//                Log.d(TAG, "AWSMobileClient is instantiated and you are connected to AWS! " + awsStartupResult.isIdentityIdAvailable());
//            }
//        }).execute();
        if (PermissionChecker.checkNetworkPermission(this)) {
            LocationUploadWorker.scheduleLocationUpload();
        }
//        startUpload();
    }

//    private void startUpload() {
//        if (PermissionChecker.checkNetworkPermission(this)) {
//            Log.i(TAG, Arrays.toString(this.fileList()));
//            final File file3 = new File(this.getFilesDir(), this.fileList()[3]);
////            ddbLocationFileUploader.execute(file.toPath());
//            s3LocationFileUploader.uploadFiles(file3.toPath());
//        }
//    }

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
//                    requestLocationUpdates();
//                    startUpload();
                    LocationUploadWorker.scheduleLocationUpload();
                } else {
                    Log.w(TAG, "Internet permissions denied!!!");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
            }
        }
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
