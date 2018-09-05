package rs.necukuci.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import rs.necukuci.storage.local.LocalFileLocationDataStore;
import timber.log.Timber;

public class BackgroundLocationListener implements LocationListener {
    private final LocalFileLocationDataStore dataStore;

    public BackgroundLocationListener(final LocalFileLocationDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Override
    public void onLocationChanged(final Location location) {
        Timber.i("onLocationChanged: %s", location);
        dataStore.writeLocation(location);
    }

    @Override
    public void onStatusChanged(final String provider, final int status, final Bundle extras) {
        Timber.i("onStatusChanged: %s, stat=%s, extra: %s", provider, status, extras.toString());

    }

    @Override
    public void onProviderEnabled(final String provider) {
        Timber.i("onProviderEnabled: %s", provider);

    }

    @Override
    public void onProviderDisabled(final String provider) {
        Timber.i("onProviderDisabled: %s", provider);
    }
}
