package rs.necukuci.model;

import lombok.Data;

@Data
public class GeoRectangle {
    private final GeoPoint bottomLeft;
    private final GeoPoint topRight;
}
