package dhbw.trasima;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Utility class providing common Coordinate suppliers for VirtualVehicleFactory.
 */
public class CoordinateSuppliers {

    private static final Random RANDOM = new Random();

    /**
     * Package-private constructor to allow extending from Coordinates.Suppliers.
     */
    CoordinateSuppliers() {
    }

    /**
     * Creates a supplier that always returns the same fixed coordinate.
     *
     * @param coord The fixed coordinate
     * @return Supplier that returns the coordinate
     */
    public static Supplier<Coordinates> fixed(Coordinates coord) {
        return () -> coord;
    }

    /**
     * Creates a supplier that generates random coordinates within a rectangular area.
     *
     * @param minLat Minimum latitude in degrees
     * @param maxLat Maximum latitude in degrees
     * @param minLon Minimum longitude in degrees
     * @param maxLon Maximum longitude in degrees
     * @return Supplier that generates random coordinates in the area
     */
    public static Supplier<Coordinates> randomInArea(double minLat, double maxLat,
                                                     double minLon, double maxLon) {
        return () -> Coordinates.fromDegrees(
                minLat + RANDOM.nextDouble() * (maxLat - minLat),
                minLon + RANDOM.nextDouble() * (maxLon - minLon)
        );
    }

    /**
     * Creates a supplier that generates random coordinates within the Mannheim area.
     * Covers roughly the city center and surrounding areas.
     *
     * @return Supplier that generates random coordinates in Mannheim
     */
    public static Supplier<Coordinates> randomInMannheim() {
        return randomInArea(49.46, 49.52, 8.44, 8.56);
    }

    /**
     * Creates a supplier that generates random coordinates within a circular area.
     *
     * @param center   Center coordinate
     * @param radiusKm Radius in kilometers
     * @return Supplier that generates random coordinates within the circle
     */
    public static Supplier<Coordinates> randomInRadius(Coordinates center, double radiusKm) {
        return () -> {
            // Random angle and distance
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = RANDOM.nextDouble() * radiusKm;

            // Convert to coordinate offset (approximate)
            // 1 degree latitude ≈ 111 km
            // 1 degree longitude ≈ 111 km * cos(latitude)
            double latOffset = (distance * Math.cos(angle)) / 111.0;
            double lonOffset = (distance * Math.sin(angle)) / (111.0 * Math.cos(Math.toRadians(center.getLatitudeDeg())));

            return Coordinates.fromDegrees(
                    center.getLatitudeDeg() + latOffset,
                    center.getLongitudeDeg() + lonOffset
            );
        };
    }
}