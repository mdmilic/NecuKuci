package rs.necukuci.storage.local;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import rs.necukuci.util.EmulatorUtils;

public class LocalFileLocationStoreUtils {
    public static String getCurrentFileNameSuffix() {
        if (EmulatorUtils.isEmulator()) {
            return Instant.now().truncatedTo(ChronoUnit.MINUTES).toString();
        } else {
            return Instant.now().truncatedTo(ChronoUnit.DAYS).toString();
        }
    }

    public static boolean isValidFileName(final String fileName) {
        return (/*fileName.startsWith("myLocation") // TODO: Might contain duplicates, skip for now. Duplicates are when both providers give data at same time
                || */fileName.startsWith("locationCallback")
                || fileName.startsWith("locationListener"));
    }

    public static boolean isBackedUp(final String fileName) {
        return fileName.endsWith("bak");
    }

    public static boolean isCurrentFile(final String fileName) {
        return fileName.contains(getCurrentFileNameSuffix());
    }

    public static Instant getTimeFromName(final String fileName) {
        final String timeOfFile = fileName.substring(fileName.indexOf('-') + 1, fileName.indexOf('.'));
        return Instant.parse(timeOfFile);
    }
}
