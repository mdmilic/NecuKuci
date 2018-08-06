package rs.necukuci.storage;

import android.location.Location;

import com.google.gson.Gson;

public class LocationMapper {
    private static final Gson GSON_MAPPER = new Gson();

    public String toJson(final Location location) {
        return GSON_MAPPER.toJson(location);
    }

    public rs.necukuci.model.Location fromJson(final String jsonLocation) {
        return GSON_MAPPER.fromJson(jsonLocation, rs.necukuci.model.Location.class);
    }
}
