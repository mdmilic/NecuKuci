package rs.necukuci.storage.local;

import android.content.Context;
import android.location.Location;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.time.Instant;

import rs.necukuci.storage.LocationMapper;
import timber.log.Timber;

public class LocalFileLocationDataStore {
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
            Timber.e("Directory not created");
        }
        return file;
    }

    public void writeLocation(final Location location) {
        try {
            final String filename = filePrefix + "-" + LocalFileLocationStoreUtils.getCurrentFileNameSuffix() + ".txt";
            final String jsonLocation = Instant.now() + ": " + LocationMapper.toJson(location) + "\n";
            Timber.i("Writing %s to: %s", filename, jsonLocation);
            final FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_APPEND);
            outputStream.write(jsonLocation.getBytes());
            outputStream.close();
            Timber.i("Location written to file: %s", context.getFilesDir().getAbsolutePath());
        } catch (final Exception e) {
            Timber.e(e, "Exception writing location to file");
        }
    }
}
