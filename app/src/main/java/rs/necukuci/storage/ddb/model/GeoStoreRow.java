package rs.necukuci.storage.ddb.model;


import rs.necukuci.model.Location;
import rs.necukuci.util.EmulatorUtils;

public abstract class GeoStoreRow {
    public static GeoStoreRow from(final String userId, final Location location, final String...tags) {
        if (EmulatorUtils.isEmulator()) {
            return GeoStoreTestTableRow.from(userId, location, tags);
        } else {
            return GeoStoreTableRow.from(userId, location, tags);
        }
    }
}
