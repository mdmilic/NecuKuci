package rs.necukuci.storage.local;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;

import rs.necukuci.storage.LocationMapper;

public class LocalFileLocationDataStore {
    private static final String STORAGE_TAG = LocalFileLocationDataStore.class.getSimpleName();
    private static final LocationMapper LOCATION_MAPPER = new LocationMapper();
    private final Context context;
    private final String filePrefix;
    public LocalFileLocationDataStore(final Context context, final String filePrefix) {
        this.context = context;
        this.filePrefix = filePrefix;
    }

    public static File getPrivateDocumentsStorageDir(final Context context, final String dirName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), dirName);
        if (!file.mkdirs()) {
            Log.e(STORAGE_TAG, "Directory not created");
        }
        return file;
    }

    public void writeLocation(final Location location) {
        try {
            final String filename = filePrefix + "-" + LocalFileLocationStoreUtils.getCurrentFileName() + ".txt";
            final String jsonLocation = Instant.now() + ": " + LOCATION_MAPPER.toJson(location) + "\n";
            Log.i(STORAGE_TAG, "Writing to " + filename + ": " + jsonLocation);
            final FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(jsonLocation.getBytes());
            outputStream.close();
            Log.i(STORAGE_TAG, "Location written to file: " + context.getFilesDir().getAbsolutePath());
        } catch (final Exception e) {
            Log.e(STORAGE_TAG, "Exception writing location to file", e);
        }
    }
}
