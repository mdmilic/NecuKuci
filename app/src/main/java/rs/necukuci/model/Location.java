package rs.necukuci.model;


import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * This is a duplicate of {} because that one implements Parcelable which is not serializable
 * @see android.location.Location
 */

@Data
@Accessors(prefix = {"m"})
public class Location {
    private final String mProvider;
    private final long mTime;
    private final long mElapsedRealtimeNanos;
    private final double mLatitude;
    private final double mLongitude;
    private final double mAltitude;
    private final float mSpeed;
    private final float mBearing;
    @Getter(AccessLevel.NONE)
    private final float mHorizontalAccuracyMeters;
    private final float mVerticalAccuracyMeters;
    private final float mSpeedAccuracyMetersPerSecond;
    private final float mBearingAccuracyDegrees;

    public float getAccuracy() {
        return this.mHorizontalAccuracyMeters;
    }
}
