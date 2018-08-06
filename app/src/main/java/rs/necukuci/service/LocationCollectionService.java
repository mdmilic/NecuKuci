package rs.necukuci.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import rs.necukuci.R;
import rs.necukuci.location.BackgroundFusedLocationCallback;
import rs.necukuci.location.BackgroundLocationListener;
import rs.necukuci.receiver.ServiceRestartBroadcastReceiver;
import rs.necukuci.storage.local.LocalFileLocationDataStore;

public class LocationCollectionService extends Service {
    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "123456";

    private FusedLocationProviderClient fusedLocationClient;
    private BackgroundFusedLocationCallback locationCallback;

    private LocationManager locationManager;
    private LocationListener locationListener;

    public LocationCollectionService() {
        super();
        Log.i(TAG, "Service created! " + + Process.myTid());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        final Notification notification = registerNotification();
        this.startForeground(1, notification);

        final LocalFileLocationDataStore listenerDataStore = new LocalFileLocationDataStore(this.getApplicationContext(), "locationListener");
        final LocalFileLocationDataStore callbackDataStore = new LocalFileLocationDataStore(this.getApplicationContext(), "locationCallback");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new BackgroundLocationListener(listenerDataStore);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new BackgroundFusedLocationCallback(callbackDataStore);
        requestFusedLocationUpdates();
        requestLocationUpdates();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy! " + Process.myTid());
//        final Intent broadcastIntent = new Intent("rs.necukuci.RestartLocationService");
        final Intent broadcastIntent = new Intent(this.getApplicationContext(), ServiceRestartBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
    }

    private void requestFusedLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(BackgroundFusedLocationCallback.createLocationRequest(), locationCallback, null);
        }
    }

    private void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 20000, 50, locationListener);
        }
    }

    private Notification registerNotification() {
        final String text = "NecuKuci is currently tracking you!";
        final Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Necu kuci bre", importance);
            channel.setDescription("Necu Kuci notification channel");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(text)
                    .setAutoCancel(true)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build();
        }
        return notification;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
