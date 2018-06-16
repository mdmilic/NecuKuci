package rs.necukuci.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import rs.necukuci.storage.local.LocalFileLocationDataStore;

public class BackgroundLocationListener implements LocationListener {
    private static final String LISTENER = "LocationListener";
    private final LocalFileLocationDataStore dataStore;

    public BackgroundLocationListener(final LocalFileLocationDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.i(LISTENER, "onLocationChanged: " + location);
        dataStore.writeLocation(location);
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        Log.i(LISTENER, "onStatusChanged: " + provider + ", stat " + status + ", extra: " + extras.toString());

    }

    @Override
    public void onProviderEnabled(final String provider) {
        Log.i(LISTENER, "onProviderEnabled: " + provider);

    }

    @Override
    public void onProviderDisabled(final String provider) {
        Log.i(LISTENER, "onProviderDisabled: " + provider);
    }
}
