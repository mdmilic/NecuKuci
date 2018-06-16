package rs.necukuci.location;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import rs.necukuci.storage.local.LocalFileLocationDataStore;

public class BackgroundFusedLocationCallback extends LocationCallback {

    private static final String TAG = "LocationCallback";
    private final LocalFileLocationDataStore dataStore;

    public BackgroundFusedLocationCallback(final LocalFileLocationDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        super.onLocationResult(locationResult);
        Log.i(TAG, "Received # location " + locationResult.getLocations().size());
        for (final Location location : locationResult.getLocations()) {
            dataStore.writeLocation(location);
        }
    }

    public static LocationRequest createLocationRequest() {
        return LocationRequest.create()
                .setInterval(20000)
                .setMaxWaitTime(120000) // Allows location batching to save power
                .setFastestInterval(2000)
                .setSmallestDisplacement(50)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }
}
