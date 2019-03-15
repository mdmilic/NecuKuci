package rs.necukuci.storage.ddb;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import rs.necukuci.config.AWSConfig;
import rs.necukuci.model.Location;
import rs.necukuci.storage.ddb.model.GeoStoreRow;
import rs.necukuci.storage.ddb.model.UserStatsRow;
import rs.necukuci.user.UserManager;
import rs.necukuci.util.EmulatorUtils;
import timber.log.Timber;

import static rs.necukuci.storage.LocationMapper.createLocationFromLine;

public class DDBLocationDataStore extends AsyncTask<Path, Void, Void> {
    private static final DynamoDBMapper DYNAMO_DB_MAPPER = DynamoDBMapper.builder()
                                                                         .dynamoDBClient(new AmazonDynamoDBClient(AWSConfig.getMobileHubCredentialsProvider()))
                                                                         .awsConfiguration(AWSConfig.getMobileHubAwsConfiguration())
                                                                         .build();
    @Override
    protected Void doInBackground(final Path... paths) {
        Timber.i("DDB Executing in background...");
        try {
            final String userID = UserManager.getUserID();
            for (final Path path : paths) {
                batchStoreInDatabase(path, userID);

                final File backupFile = new File(path.toFile().getAbsolutePath() + ".bak");
                Files.copy(path, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Delete the file that is successfully stored in DDB and S3
                Timber.i("Attempting to delete %s", path.getFileName());
                path.toFile().delete();
            }
        } catch (final Exception e) {
            Timber.e(e, "Failed to store file in the DDB: ");
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void aVoid) {
        super.onPostExecute(aVoid);
        Timber.i("DDB Write async task finished!");
    }

    private void batchStoreInDatabase(final Path locationsFile, final String userID) throws Exception {

//        final Function<String, GeoStoreTableRow> stringObjectFunction = new Function<String, GeoStoreTableRow>() {
//            @Override
//            public GeoStoreTableRow apply(final String s) {
//                return convertToGeoStoreRow(s);
//            }
//        };//l -> convertToGeoStoreRow(l);
//        final List<GeoStoreTableRow> geoStoreRows = Files.lines(locations)
//                                                         .map(stringObjectFunction)
//                                                         .collect(Collectors.toList());

        final List<String> lines = Files.readAllLines(locationsFile, Charset.defaultCharset());
        final String[] split = locationsFile.getFileName().toString().split("-");
        final String tag;
        if (split.length > 1) {
            tag = split[0];
        } else {
            tag = "android";
        }
        try {

            final List<Location> locations = new ArrayList<>();
            for (final String line : lines) {
                locations.add(createLocationFromLine(line));
            }

            Timber.i("Converted file, storing!");
            writeLocations(locations, userID, tag);
            if (!EmulatorUtils.isEmulator()) { // TODO: Add test table for debug run
                Timber.i("Stored file, getting stats!");
                writeStats(locations, userID);
            }

        } catch (final Exception e) {
            Timber.e(e, "File %s had exception storing row!", locationsFile.getFileName());
            throw e;
        }
    }

    private void writeLocations(final List<Location> locations, final String userID, final String tag) throws Exception {
        final List<GeoStoreRow> batch = new ArrayList<>();
        for (final Location location : locations) {
            batch.add(convertToGeoStoreRow(location, userID, tag));
        }

        final List<DynamoDBMapper.FailedBatch> failedBatches = DYNAMO_DB_MAPPER.batchSave(batch);
        Timber.i("Failures: %s", failedBatches.size());
        if (!failedBatches.isEmpty()) {
            final DynamoDBMapper.FailedBatch failedBatch = failedBatches.get(0);
            Timber.e(failedBatch.getException(), "Failed to save row %s", failedBatch.getUnprocessedItems());
            throw failedBatch.getException();
        }
    }

    private void writeStats(final List<Location> locations, final String userID) {
        final Location lastElem = locations.get(locations.size() - 1);
        final UserStatsRow userStats = DYNAMO_DB_MAPPER.load(UserStatsRow.empty(userID));

        Timber.i("Loaded stats for user %s are %s", userID, userStats);

        double maxAlt;
        double minAlt;
        float maxSpeed;
        long lastSeen;
        double lastSeenLat;
        double lastSeenLng;
        final Map<String, Double> furthestWentFromDDB;
        final Set<String> continentsVisited;
        final Set<String> countriesVisited;
        final String homeCountry;
        if (Objects.isNull(userStats)) {
            maxAlt = 0;
            minAlt = 0;
            maxSpeed = 0;
            lastSeen = lastElem.getTime();
            lastSeenLat = lastElem.getLatitude();
            lastSeenLng = lastElem.getLongitude();
            furthestWentFromDDB = Collections.emptyMap();
            continentsVisited = null; // Collections.emptySet(); // Empty set not allowed
            countriesVisited = null; // Collections.emptySet(); // Empty set not allowed
            homeCountry = null; // Empty string not allowed
        } else {
            if (Objects.isNull(userStats.getFurthestWent())) {
                furthestWentFromDDB = Collections.emptyMap();
            } else {
                furthestWentFromDDB = userStats.getFurthestWent();
            }
            // No need to constantly update lastSeen after each iteration, just update it here
            if (userStats.getLastSeen() <= lastElem.getTime()) {
                lastSeen = lastElem.getTime();
                lastSeenLat = lastElem.getLatitude();
                lastSeenLng = lastElem.getLongitude();
            } else {
                lastSeen = userStats.getLastSeen();
                lastSeenLat = userStats.getLastKnownLat();
                lastSeenLng = userStats.getLastKnownLng();
            }
            maxAlt = userStats.getMaxAltitude();
            minAlt = userStats.getMinAltitude();
            maxSpeed = userStats.getMaxSpeed();
            continentsVisited = userStats.getContinentsVisited();
            countriesVisited = userStats.getCountriesVisited();
            homeCountry = userStats.getHomeCountry();
        }
        double latWest = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_WEST_LAT, 0d); // doesn't matter
        double lngWest = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_WEST_LNG, 180d); // max
        double latEast = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_EAST_LAT, 0d); // doesn't matter
        double lngEast = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_EAST_LNG, -180d); // min
        double latSouth = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_SOUTH_LAT, 90d); // max
        double lngSouth = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_SOUTH_LNG, 0d); // doesn't matter
        double latNorth = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_NORTH_LAT, -90d); // min
        double lngNorth = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_NORTH_LNG, 0d); // doesn't matter
        double lngMaxAlt = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_MAXALT_LNG, 0d); // doesn't matter
        double latMaxAlt = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_MAXALT_LAT, 0d); // doesn't matter
        double lngMinAlt = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_MINALT_LNG, 0d); // doesn't matter
        double latMinAlt = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_MINALT_LAT, 0d); // doesn't matter
        double lngMaxSpeed = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_MAXSPEED_LNG, 0d); // doesn't matter
        double latMaxSpeed = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_MAXSPEED_LAT, 0d); // doesn't matter
        double timeWest = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_WEST_TIME, 0d); // doesn't matter
        double timeEast = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_EAST_TIME, 0d); // doesn't matter
        double timeSouth = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_SOUTH_TIME, 0d); // doesn't matter
        double timeNorth = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_NORTH_TIME, 0d); // doesn't matter
        double timeMaxAlt = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_MAXALT_TIME, 0d); // doesn't matter
        double timeMinAlt = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_MINALT_TIME, 0d); // doesn't matter
        double timeMaxSpeed = furthestWentFromDDB.getOrDefault(UserStatsRow.FURTHEST_MAXSPEED_TIME, 0d); // doesn't matter
        for (final Location location : locations) {
            // Higher LNG => more to east higher LAT => more to north
            if (location.getLongitude() >= lngEast) {
                lngEast = location.getLongitude();
                latEast = location.getLatitude();
                timeEast = location.getTime();
            }
            if (location.getLongitude() <= lngWest) {
                lngWest = location.getLongitude();
                latWest = location.getLatitude();
                timeWest = location.getTime();
            }
            if (location.getLatitude() >= latNorth) {
                lngNorth = location.getLongitude();
                latNorth = location.getLatitude();
                timeNorth = location.getTime();
            }
            if (location.getLatitude() <= latSouth) {
                lngSouth = location.getLongitude();
                latSouth = location.getLatitude();
                timeSouth = location.getTime();
            }
            if (location.getAltitude() >= maxAlt) {
                maxAlt = location.getAltitude();
                lngMaxAlt = location.getLongitude();
                latMaxAlt = location.getLatitude();
                timeMaxAlt = location.getTime();
            }
            if (location.getAltitude() <= minAlt) {
                minAlt = location.getAltitude();
                lngMinAlt = location.getLongitude();
                latMinAlt = location.getLatitude();
                timeMinAlt = location.getTime();
            }
            if (location.getSpeed() >= maxSpeed) {
                maxSpeed = location.getSpeed();
                lngMaxSpeed = location.getLongitude();
                latMaxSpeed = location.getLatitude();
                timeMaxSpeed = location.getTime();
            }
        }

        final HashMap<String, Double> furthestMap = new HashMap<>();
        furthestMap.put(UserStatsRow.FURTHEST_NORTH_LAT, latNorth);
        furthestMap.put(UserStatsRow.FURTHEST_NORTH_LNG, lngNorth);
        furthestMap.put(UserStatsRow.FURTHEST_SOUTH_LAT, latSouth);
        furthestMap.put(UserStatsRow.FURTHEST_SOUTH_LNG, lngSouth);
        furthestMap.put(UserStatsRow.FURTHEST_EAST_LAT, latEast);
        furthestMap.put(UserStatsRow.FURTHEST_EAST_LNG, lngEast);
        furthestMap.put(UserStatsRow.FURTHEST_WEST_LAT, latWest);
        furthestMap.put(UserStatsRow.FURTHEST_WEST_LNG, lngWest);
        furthestMap.put(UserStatsRow.FURTHEST_MAXALT_LAT, latMaxAlt);
        furthestMap.put(UserStatsRow.FURTHEST_MAXALT_LNG, lngMaxAlt);
        furthestMap.put(UserStatsRow.FURTHEST_MINALT_LAT, latMinAlt);
        furthestMap.put(UserStatsRow.FURTHEST_MINALT_LNG, lngMinAlt);
        furthestMap.put(UserStatsRow.FURTHEST_MAXSPEED_LAT, latMaxSpeed);
        furthestMap.put(UserStatsRow.FURTHEST_MAXSPEED_LNG, lngMaxSpeed);
        furthestMap.put(UserStatsRow.FURTHEST_NORTH_TIME, timeNorth);
        furthestMap.put(UserStatsRow.FURTHEST_SOUTH_TIME, timeSouth);
        furthestMap.put(UserStatsRow.FURTHEST_EAST_TIME, timeEast);
        furthestMap.put(UserStatsRow.FURTHEST_WEST_TIME, timeWest);
        furthestMap.put(UserStatsRow.FURTHEST_MAXALT_TIME, timeMaxAlt);
        furthestMap.put(UserStatsRow.FURTHEST_MINALT_TIME, timeMinAlt);
        furthestMap.put(UserStatsRow.FURTHEST_MAXSPEED_TIME, timeMaxSpeed);

        final UserStatsRow newStats = UserStatsRow.builder()
                                                  .userId(userID)
                                                  .continentsVisited(continentsVisited)
                                                  .countriesVisited(countriesVisited)
                                                  .furthestWent(furthestMap)
                                                  .lastKnownLat(lastSeenLat)
                                                  .lastKnownLng(lastSeenLng)
                                                  .lastSeen(lastSeen)
                                                  .maxAltitude(maxAlt)
                                                  .minAltitude(minAlt)
                                                  .maxSpeed(maxSpeed)
                                                  .homeCountry(homeCountry)
                                                  .build();

        DYNAMO_DB_MAPPER.save(newStats);
        Timber.i("Stats for user %s are saved as %s", userID, newStats);
    }

    private GeoStoreRow convertToGeoStoreRow(final Location location, final String userID, final String... tags) {
        return GeoStoreRow.from(userID, location, tags);
    }
}