package rs.necukuci.storage;

import com.google.gson.Gson;

import rs.necukuci.model.Location;

public class LocationMapper {
    private static final Gson GSON_MAPPER = new Gson();

    public static String toJson(final android.location.Location location) {
        return GSON_MAPPER.toJson(location);
    }

    public static Location fromJson(final String jsonLocation) {
        return GSON_MAPPER.fromJson(jsonLocation, Location.class);
    }

    public static Location createLocationFromLine(final String line) {
        final String[] lineParts = line.split(": ");

        final Location location;
        if (lineParts.length == 2) {
            final String locationJson = lineParts[1];
            location = fromJson(locationJson);
        } else if (lineParts.length == 1) { // First few files didn't have timestamp
            final String locationJson = lineParts[0];
            location = fromJson(locationJson);
        } else {
            throw new IllegalArgumentException("Line is malformed: " + line);
        }
        return location;
    }
}
