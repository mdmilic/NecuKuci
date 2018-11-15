package rs.necukuci.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.ButtCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.Iterables;
import com.google.common.geometry.S2LatLng;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import rs.necukuci.R;
import rs.necukuci.model.Location;
import timber.log.Timber;

import static rs.necukuci.storage.LocationMapper.createLocationFromLine;
import static rs.necukuci.storage.local.LocalFileLocationStoreUtils.getTimeFromName;
import static rs.necukuci.storage.local.LocalFileLocationStoreUtils.isValidFileName;

public class GmapFragment extends Fragment implements OnMapReadyCallback {
    private static final int TIME_SPAN_FOR_ROUTE = 7;
    private GoogleMap mMap;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,  @Nullable final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gmap, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this );
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Timber.i("Map ready but not loaded, be careful!");
        mMap = googleMap;
        mMap.setMaxZoomPreference(16);
//        mMap.setMinZoomPreference(0);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Timber.i("Map loaded, play around!");
            }
        });

        final Iterable<LatLng> list1 = getRoute("locationCallback");
        final Iterable<LatLng> list2 = getRoute("locationListener");

        final PolylineOptions polylineOptions1 = createPath(list1, Color.RED); // Worse precision and more points
        final PolylineOptions polylineOptions2 = createPath(list2, Color.BLUE); // Better precision and less points

        mMap.addPolyline(polylineOptions1);
        mMap.addPolyline(polylineOptions2);

        centerOnRoute(list1);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @NonNull
    private PolylineOptions createPath(final Iterable<LatLng> list, final int color) {
        return new PolylineOptions().addAll(list)
                                    .clickable(true)
                                    .color(color)
                                    .width(6)
                                    .endCap(new ButtCap());
    }

    private void centerOnRoute(final Iterable<LatLng> list) {
        if (list == null || Iterables.isEmpty(list)) {
            Timber.w("No geo points provided to center the route!!!");
            return;
        }

        final LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (final LatLng latLng : list) {
            builder.include(latLng);
        }
        final LatLngBounds bounds = builder.build();

        // Use this to workaround if map is not loaded but ready, alternative is onMapLoaded callback
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, BOUND_PADDING));
    }

    /**
     * get last X days of traveling
     */
    public Iterable<LatLng> getRoute(final String fileNamePrefix) {
        final File[] files = loadFilesOrderedByDate(fileNamePrefix);

        final List<LatLng> latLngs = convertToLatLng(files);
//        double distance = 0;
//        S2LatLng previous = S2LatLng.fromDegrees(latLngs.get(0).latitude, latLngs.get(0).longitude);
//        for (final LatLng latLng : latLngs) {
//            final S2LatLng next = S2LatLng.fromDegrees(latLng.latitude, latLng.longitude);
//            distance += next.getEarthDistance(previous);
//            previous = next;
//        }
//
//        Timber.i("%s locations found from %s files with total distance %s", latLngs.size(), files.length, distance);
        return latLngs;
    }

    @NonNull
    private ArrayList<LatLng> convertToLatLng(final File[] files) {
        final ArrayList<LatLng> latLngs = new ArrayList<>();
//        long totalDistance = 0;
        for (final File file : files) {
            long fileDistance = 0;
            try {
                final List<String> lines = Files.readAllLines(file.toPath());
//                final Location first = createLocationFromLine(lines.get(0));
//                S2LatLng previous = S2LatLng.fromDegrees(first.getLatitude(), first.getLongitude());
                for (final String line : lines) {
                    final Location location = createLocationFromLine(line);
//                    final S2LatLng newPoint = S2LatLng.fromDegrees(location.getLatitude(), location.getLongitude());
//                    final double meterDistance = newPoint.getEarthDistance(previous);
//                    fileDistance += meterDistance;
//                    previous = newPoint;
                    final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    latLngs.add(latLng);
                }
            } catch (IOException e) {
                Timber.e(e, "Failed to load the file %s", file.getAbsolutePath());
            }
            Timber.i("Distance for file %s is %s", file.toPath().getFileName(), fileDistance);
//            totalDistance += fileDistance;
        }
//        Timber.i("Total distance from all files is %s", totalDistance);
        return latLngs;
    }

    private File[] loadFilesOrderedByDate(final String fileNamePrefix) {
        // try loading last few days of files
        final Context context = this.getActivity().getApplicationContext();
        final File[] files = context.getFilesDir()
                                    .listFiles(new FilenameFilter() {
                                        private final Instant limitInPast = Instant.now()
                                                                                   .minus(Duration.ofDays(TIME_SPAN_FOR_ROUTE))
                                                                                   .truncatedTo(ChronoUnit.DAYS);
                                        @Override
                                        public boolean accept(final File dir, final String name) {
                                            Timber.i("Potential file: %s", name);
                                            if (isValidFileName(name) && name.contains(fileNamePrefix)) {
                                                try {
                                                    final Instant timeOfFile = getTimeFromName(name);
                                                    return limitInPast.isBefore(timeOfFile);
                                                } catch (final DateTimeParseException e) {
                                                    Crashlytics.logException(e);
                                                    Timber.e(e, "Failed to parse instant from %s", name);
                                                }
                                            }
                                            return false;
                                        }
                                    });

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(final File file1, final File file2) {
                try {
                    final Instant instant1 = getTimeFromName(file1.getName());
                    final Instant instant2 = getTimeFromName(file2.getName());
                    return instant1.compareTo(instant2);
                } catch (final DateTimeParseException e) { // This shouldn't happen as we already filtered out bad files before
                    Crashlytics.logException(e);
                    throw new IllegalArgumentException("Failed to parse instant from " + file1.getName() + " or " + file2.getName(), e);
                }
            }
        });
        Timber.i("%s files to be drawn: %s", files.length, Arrays.toString(files));
        return files;
    }
}
