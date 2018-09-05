package rs.necukuci.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.Objects;

import rs.necukuci.R;
import timber.log.Timber;


public class PermissionChecker {
    public static final int LOCATION_PERMISSION_CODE = 99;
    public static final int INTERNET_PERMISSION_CODE = 98;

    public static boolean checkLocationPermission(final Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Timber.i("Requesting location permission");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                            }
                        })
                        .create()
                        .show();
                Timber.i("Location permissions requested");
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
            }
            return false;
        } else {
            Timber.i("Already have location permissions");
            return true;
        }
    }

    public static boolean checkNetworkPermission(final Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            Timber.i("Requesting internet permission");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.INTERNET)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION_CODE);
                            }
                        })
                        .create()
                        .show();
                Timber.i("Internet permissions requested");
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION_CODE);
            }
            return false;
        } else {
            Timber.i("Already have internet permissions");
            return true;
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        return Objects.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState());
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        return Objects.equals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())
                || Objects.equals(Environment.MEDIA_MOUNTED_READ_ONLY, Environment.getExternalStorageState());
    }
}
