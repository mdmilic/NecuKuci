package rs.necukuci.storage.local;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import rs.necukuci.util.EmulatorUtils;

public class LocalFileLocationStoreUtils {
    public static String getCurrentFileName() {
        if (EmulatorUtils.isEmulator()) {
            return Instant.now().truncatedTo(ChronoUnit.MINUTES).toString();
        } else {
            return Instant.now().truncatedTo(ChronoUnit.DAYS).toString();
        }
    }
}
