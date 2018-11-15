package rs.necukuci.storage.ddb.model;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "necukuci-mobilehub-725813148-UserStats")
public class UserStatsRow {
    public static String FURTHEST_EAST_LAT = "FURTHEST_EAST_LAT";
    public static String FURTHEST_EAST_LNG = "FURTHEST_EAST_LNG";
    public static String FURTHEST_WEST_LAT = "FURTHEST_WEST_LAT";
    public static String FURTHEST_WEST_LNG = "FURTHEST_WEST_LNG";
    public static String FURTHEST_NORTH_LAT = "FURTHEST_NORTH_LAT";
    public static String FURTHEST_NORTH_LNG = "FURTHEST_NORTH_LNG";
    public static String FURTHEST_SOUTH_LAT = "FURTHEST_SOUTH_LAT";
    public static String FURTHEST_SOUTH_LNG = "FURTHEST_SOUTH_LNG";

    @DynamoDBHashKey
    private String userId;

    @DynamoDBAttribute
    private Set<String> continentsVisited;
    @DynamoDBAttribute
    private Set<String> countriesVisited;
    @DynamoDBAttribute
    private Map<String, Double> furthestWent;
    @DynamoDBAttribute
    private double lastKnownLat;
    @DynamoDBAttribute
    private double lastKnownLng;
    @DynamoDBAttribute
    private long lastSeen;
    @DynamoDBAttribute
    private double maxAltitude;
    @DynamoDBAttribute
    private double minAltitude;
    @DynamoDBAttribute
    private float maxSpeed;

    public static UserStatsRow empty(final String user) {
        return UserStatsRow.builder()
                           .userId(user)
                           .furthestWent(new HashMap<String, Double>())
                           .lastKnownLat(0)
                           .lastKnownLng(0)
                           .lastSeen(0)
                           .maxAltitude(0)
                           .minAltitude(0)
                           .maxSpeed(0)
                           .build();
    }

    static UserStatsRow from(final String user) {
        return UserStatsRow.builder()
                           .userId(user)
//                           .countriesVisited() // needs conditional update - unsupported with mapper
//                           .continentsVisited() // needs conditional update - unsupported with mapper
//                           .furthestWent() // needs conditional update - unsupported with mapper
                           .lastKnownLat(0)
                           .lastKnownLng(0)
                           .lastSeen(0)
                           .maxAltitude(0)
                           .minAltitude(0)
                           .maxSpeed(0)
                           .build();
    }
}