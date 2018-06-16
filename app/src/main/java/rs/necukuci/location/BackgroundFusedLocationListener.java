package rs.necukuci.location;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import rs.necukuci.storage.local.LocalFileLocationDataStore;

public class BackgroundFusedLocationListener implements LocationListener {

    private final LocalFileLocationDataStore dataStore;

    public BackgroundFusedLocationListener(final LocalFileLocationDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.i("TAGA", "BackgroundFusedLocationListener");
        dataStore.writeLocation(location);
    }

    public static LocationRequest createLocationRequest() {
        return LocationRequest.create()
                .setInterval(10000)
                .setMaxWaitTime(120000) // Allows location batching to save power
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }
}
