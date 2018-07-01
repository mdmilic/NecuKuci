package rs.necukuci.storage.ddb.model;

import android.location.*;
import android.util.*;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.google.common.collect.*;

import java.util.*;

import lombok.*;
import rs.necukuci.geo.s2.*;
import rs.necukuci.model.*;

@Builder
@Data
@DynamoDBTable(tableName = "necukuci-mobilehub-725813148-GeoStore")
public class GeoStoreTableRow extends GeoStoreRow{
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

    public static GeoStoreTableRow from(final String userId, final Location location, final String...tags) {
        final GeoHash geoHash = S2Utils.generateGeoHash(new GeoPoint(location.getLatitude(), location.getLongitude()));
        final long utcTimeMillis = location.getTime();
        final String geoHash_time = String.format(Locale.US, "%d_%d", geoHash.getVal(), utcTimeMillis);
        Log.v("BAZA", "GeoHash: " + geoHash_time);

        return GeoStoreTableRow.builder()
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
