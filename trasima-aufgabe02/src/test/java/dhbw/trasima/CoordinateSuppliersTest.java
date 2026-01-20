package dhbw.trasima;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Unit tests for the CoordinateSuppliers utility class.
 */
class CoordinateSuppliersTest {

    private static final double DELTA = 0.0001;

    // ==================== fixed() Tests ====================

    @Nested
    @DisplayName("fixed() Tests")
    class FixedTests {

        @Test
        @DisplayName("fixed should return the same coordinate")
        void testFixed_ReturnsSameCoordinate() {
            Coordinates original = Coordinates.fromDegrees(49.47, 8.53);
            Supplier<Coordinates> supplier = CoordinateSuppliers.fixed(original);

            Coordinates result = supplier.get();

            assertEquals(original.getLatitudeDeg(), result.getLatitudeDeg(), DELTA);
            assertEquals(original.getLongitudeDeg(), result.getLongitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("fixed should return equal values on multiple calls")
        void testFixed_MultipleCallsReturnEqual() {
            Coordinates original = Coordinates.fromDegrees(49.47, 8.53);
            Supplier<Coordinates> supplier = CoordinateSuppliers.fixed(original);

            Coordinates result1 = supplier.get();
            Coordinates result2 = supplier.get();
            Coordinates result3 = supplier.get();

            assertEquals(result1.getLatitudeDeg(), result2.getLatitudeDeg(), DELTA);
            assertEquals(result2.getLatitudeDeg(), result3.getLatitudeDeg(), DELTA);
            assertEquals(result1.getLongitudeDeg(), result2.getLongitudeDeg(), DELTA);
            assertEquals(result2.getLongitudeDeg(), result3.getLongitudeDeg(), DELTA);
        }
    }

    // ==================== randomInArea() Tests ====================

    @Nested
    @DisplayName("randomInArea() Tests")
    class RandomInAreaTests {

        @Test
        @DisplayName("randomInArea should return coordinates within bounds")
        void testRandomInArea_WithinBounds() {
            double minLat = 49.0;
            double maxLat = 50.0;
            double minLon = 8.0;
            double maxLon = 9.0;

            Supplier<Coordinates> supplier = CoordinateSuppliers.randomInArea(minLat, maxLat, minLon, maxLon);

            for (int i = 0; i < 100; i++) {
                Coordinates coord = supplier.get();
                assertTrue(coord.getLatitudeDeg() >= minLat && coord.getLatitudeDeg() <= maxLat,
                        "Latitude should be within bounds: " + coord.getLatitudeDeg());
                assertTrue(coord.getLongitudeDeg() >= minLon && coord.getLongitudeDeg() <= maxLon,
                        "Longitude should be within bounds: " + coord.getLongitudeDeg());
            }
        }

        @Test
        @DisplayName("randomInArea should produce different values")
        void testRandomInArea_ProducesDifferentValues() {
            Supplier<Coordinates> supplier = CoordinateSuppliers.randomInArea(49.0, 50.0, 8.0, 9.0);

            Set<Double> latitudes = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                latitudes.add(supplier.get().getLatitudeDeg());
            }

            assertTrue(latitudes.size() > 1, "Should produce different latitude values");
        }

        @Test
        @DisplayName("randomInArea with min equals max should return constant value")
        void testRandomInArea_MinEqualsMax() {
            double lat = 49.5;
            double lon = 8.5;

            Supplier<Coordinates> supplier = CoordinateSuppliers.randomInArea(lat, lat, lon, lon);

            for (int i = 0; i < 10; i++) {
                Coordinates coord = supplier.get();
                assertEquals(lat, coord.getLatitudeDeg(), DELTA);
                assertEquals(lon, coord.getLongitudeDeg(), DELTA);
            }
        }
    }

    // ==================== randomInMannheim() Tests ====================

    @Nested
    @DisplayName("randomInMannheim() Tests")
    class RandomInMannheimTests {

        @Test
        @DisplayName("randomInMannheim should return coordinates within Mannheim bounds")
        void testRandomInMannheim_WithinMannheimBounds() {
            // Mannheim bounds: 49.46-49.52, 8.44-8.56
            double minLat = 49.46;
            double maxLat = 49.52;
            double minLon = 8.44;
            double maxLon = 8.56;

            Supplier<Coordinates> supplier = CoordinateSuppliers.randomInMannheim();

            for (int i = 0; i < 100; i++) {
                Coordinates coord = supplier.get();
                assertTrue(coord.getLatitudeDeg() >= minLat && coord.getLatitudeDeg() <= maxLat,
                        "Latitude should be within Mannheim bounds: " + coord.getLatitudeDeg());
                assertTrue(coord.getLongitudeDeg() >= minLon && coord.getLongitudeDeg() <= maxLon,
                        "Longitude should be within Mannheim bounds: " + coord.getLongitudeDeg());
            }
        }
    }

    // ==================== randomInRadius() Tests ====================

    @Nested
    @DisplayName("randomInRadius() Tests")
    class RandomInRadiusTests {

        @Test
        @DisplayName("randomInRadius should return coordinates within radius")
        void testRandomInRadius_WithinRadius() {
            Coordinates center = Coordinates.fromDegrees(49.47, 8.53);
            double radiusKm = 1.0;

            Supplier<Coordinates> supplier = CoordinateSuppliers.randomInRadius(center, radiusKm);

            for (int i = 0; i < 100; i++) {
                Coordinates coord = supplier.get();
                double distanceM = center.calculateDistance(coord);
                double distanceKm = distanceM / 1000.0;

                assertTrue(distanceKm <= radiusKm + 0.01,
                        "Distance should be within radius: " + distanceKm + " km");
            }
        }

        @Test
        @DisplayName("randomInRadius should produce different values")
        void testRandomInRadius_ProducesDifferentValues() {
            Coordinates center = Coordinates.fromDegrees(49.47, 8.53);
            Supplier<Coordinates> supplier = CoordinateSuppliers.randomInRadius(center, 1.0);

            Set<Double> latitudes = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                latitudes.add(supplier.get().getLatitudeDeg());
            }

            assertTrue(latitudes.size() > 1, "Should produce different latitude values");
        }

        @Test
        @DisplayName("randomInRadius with zero radius should return center")
        void testRandomInRadius_ZeroRadius() {
            Coordinates center = Coordinates.fromDegrees(49.47, 8.53);
            Supplier<Coordinates> supplier = CoordinateSuppliers.randomInRadius(center, 0.0);

            for (int i = 0; i < 10; i++) {
                Coordinates coord = supplier.get();
                assertEquals(center.getLatitudeDeg(), coord.getLatitudeDeg(), DELTA);
                assertEquals(center.getLongitudeDeg(), coord.getLongitudeDeg(), DELTA);
            }
        }
    }
}