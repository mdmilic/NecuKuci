package rs.necukuci.storage.ddb.model;

import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.google.common.collect.Sets;

import java.util.Locale;
import java.util.Set;

import lombok.Builder;
import lombok.Data;
import rs.necukuci.geo.s2.S2Utils;
import rs.necukuci.model.GeoHash;
import rs.necukuci.model.GeoPoint;
import rs.necukuci.model.Location;

@Builder
@Data
@DynamoDBTable(tableName = "necukuci-mobilehub-725813148-GeoStoreTest")
public class GeoStoreTestTableRow extends GeoStoreRow {
    // Keys
    // Hash key for primary table and GSI
    @DynamoDBHashKey
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "userId-utcTimeMillis")
    private final String userId;
    // Primary sort key
    @DynamoDBRangeKey
    private final String geoHash_Time;
    // Sort key for GSI
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "userId-utcTimeMillis")
    private final long utcTimeMillis;

    //Location properties
    @DynamoDBAttribute
    private final long geoHash;
    @DynamoDBAttribute
    private final double latitude;
    @DynamoDBAttribute
    private final double longitude;
    @DynamoDBAttribute
    private final double altitude;
    @DynamoDBAttribute
    private final float speed;
    @DynamoDBAttribute
    private final float bearing;
    @DynamoDBAttribute
    private final String provider;
    @DynamoDBAttribute
    private final float horizontalAccuracy;
    @DynamoDBAttribute
    private final float verticalAccuracy;
    @DynamoDBAttribute
    private final float speedAccuracy;
    @DynamoDBAttribute
    private final float bearingAccuracyDegrees;
    @DynamoDBAttribute
    private final Set<String> tags;

    public static GeoStoreRow from(final String userId, final Location location, final String...tags) {
        final GeoHash geoHash = S2Utils.generateGeoHash(new GeoPoint(location.getLatitude(), location.getLongitude()));
        final long utcTimeMillis = location.getTime();
        final String geoHash_time = String.format(Locale.US, "%d_%d", geoHash.getVal(), utcTimeMillis);
        Log.v("Test BAZA", "GeoHash: " + geoHash_time);

        return GeoStoreTestTableRow.builder()
                                   .userId(userId)
                                   .geoHash_Time(geoHash_time)
                                   .utcTimeMillis(utcTimeMillis)
                                   .geoHash(geoHash.getVal())
                                   .latitude(location.getLatitude())
                                   .longitude(location.getLongitude())
                                   .altitude(location.getAltitude()) // negative altitudes are valid
                                   .speed(location.getSpeed())
                                   .bearing(location.getBearing())
                                   .provider(location.getProvider())
                                   .horizontalAccuracy(location.getAccuracy())
                                   .verticalAccuracy(location.getVerticalAccuracyMeters())
                                   .speedAccuracy(location.getSpeedAccuracyMetersPerSecond())
                                   .bearingAccuracyDegrees(location.getBearingAccuracyDegrees())
                                   .tags(Sets.newHashSet(tags))
                                   .build();
    }
}
