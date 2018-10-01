package rs.necukuci.geo.s2;

import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2LatLngRect;

import rs.necukuci.model.GeoHash;
import rs.necukuci.model.GeoPoint;
import rs.necukuci.model.GeoRectangle;

public class S2Utils {

    private S2Utils() {}

    /**
     * An utility method to get a bounding box of latitude and longitude from a given GeoQueryRequest.
     *
     * @param geoRectangle
     *            It contains all of the necessary information to form a latitude and longitude box.
     *
     * */
    public static S2LatLngRect getBoundingLatLngRect(final GeoRectangle geoRectangle) {
        final GeoPoint minPoint = geoRectangle.getBottomLeft();
        final GeoPoint maxPoint = geoRectangle.getTopRight();

        if (minPoint != null && maxPoint != null) {
            final S2LatLng minLatLng = S2LatLng.fromDegrees(minPoint.getLatitude(), minPoint.getLongitude());
            final S2LatLng maxLatLng = S2LatLng.fromDegrees(maxPoint.getLatitude(), maxPoint.getLongitude());

            return new S2LatLngRect(minLatLng, maxLatLng);
        } else {
            throw new IllegalArgumentException("min and max points must exist!!!");
        }
    }

    /**
     * Creates a geoHash from lat and lng
     * @param geoPoint point that needs to be converted to geoHash
     * @return resulting geoHash
     */
    public static GeoHash generateGeoHash(final GeoPoint geoPoint) {
        final S2LatLng latLng = S2LatLng.fromDegrees(geoPoint.getLatitude(), geoPoint.getLongitude());
//        final S2Cell cell = new S2Cell(latLng);
//        final S2CellId cellId = cell.id();
        final S2CellId cellId = S2CellId.fromLatLng(latLng);
        return new GeoHash(cellId.id());
    }
}
