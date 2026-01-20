package dhbw.trasima;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for the Coordinates class.
 * Tests all methods including edge cases and boundary conditions.
 */
class CoordinatesTest {

    private static final double DELTA = 0.0001; // Tolerance for floating point comparisons
    private static final double STRICT_DELTA = 0.001; // Stricter tolerance for distance tests

    // Test location constants
    private static final double MANNHEIM_LAT_DEG = 49.4875;
    private static final double MANNHEIM_LON_DEG = 8.4660;
    private static final double HEIDELBERG_LAT_DEG = 49.4093;
    private static final double HEIDELBERG_LON_DEG = 8.6944;

    // Pre-calculated radians for common test values
    private static final double MANNHEIM_LAT_RAD = Math.toRadians(MANNHEIM_LAT_DEG);
    private static final double MANNHEIM_LON_RAD = Math.toRadians(MANNHEIM_LON_DEG);

    // ==================== Constructor Tests ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should initialize coordinates correctly (radians)")
        void testConstructorRadians() {
            Coordinates coord = new Coordinates(MANNHEIM_LAT_RAD, MANNHEIM_LON_RAD);

            assertEquals(MANNHEIM_LAT_RAD, coord.getLatitude(), DELTA);
            assertEquals(MANNHEIM_LON_RAD, coord.getLongitude(), DELTA);
        }

        @Test
        @DisplayName("Constructor should clamp latitude to valid range - upper bound")
        void testConstructorLatitudeClampUpper() {
            Coordinates coordNorth = new Coordinates(Math.PI, 0);
            assertEquals(Math.PI / 2, coordNorth.getLatitude(), DELTA);
        }

        @Test
        @DisplayName("Constructor should clamp latitude to valid range - lower bound")
        void testConstructorLatitudeClampLower() {
            Coordinates coordSouth = new Coordinates(-Math.PI, 0);
            assertEquals(-Math.PI / 2, coordSouth.getLatitude(), DELTA);
        }

        @Test
        @DisplayName("Constructor should normalize longitude > π")
        void testConstructorLongitudeNormalizationPositive() {
            Coordinates coord = new Coordinates(0, Math.PI * 1.5); // 270°
            // Should normalize to -π/2 (-90°)
            assertEquals(-Math.PI / 2, coord.getLongitude(), DELTA);
        }

        @Test
        @DisplayName("Constructor should normalize longitude < -π")
        void testConstructorLongitudeNormalizationNegative() {
            Coordinates coord = new Coordinates(0, -Math.PI * 1.5); // -270°
            // Should normalize to π/2 (90°)
            assertEquals(Math.PI / 2, coord.getLongitude(), DELTA);
        }

        @Test
        @DisplayName("Constructor should throw exception for NaN latitude")
        void testConstructorNaNLatitude() {
            assertThrows(IllegalArgumentException.class, () -> new Coordinates(Double.NaN, 0));
        }

        @Test
        @DisplayName("Constructor should throw exception for NaN longitude")
        void testConstructorNaNLongitude() {
            assertThrows(IllegalArgumentException.class, () -> new Coordinates(0, Double.NaN));
        }

        @Test
        @DisplayName("Constructor should throw exception for Infinite latitude")
        void testConstructorInfiniteLatitude() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Coordinates(Double.POSITIVE_INFINITY, 0));
        }

        @Test
        @DisplayName("Constructor should throw exception for Infinite longitude")
        void testConstructorInfiniteLongitude() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Coordinates(0, Double.NEGATIVE_INFINITY));
        }
    }

    // ==================== Factory Method Tests ====================

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("fromDegrees should create coordinates from degree values")
        void testFromDegrees() {
            Coordinates coord = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);

            assertEquals(MANNHEIM_LAT_DEG, coord.getLatitudeDeg(), DELTA);
            assertEquals(MANNHEIM_LON_DEG, coord.getLongitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("fromDegrees should handle North Pole")
        void testFromDegreesNorthPole() {
            Coordinates northPole = Coordinates.fromDegrees(90.0, 0.0);
            assertEquals(90.0, northPole.getLatitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("fromDegrees should handle South Pole")
        void testFromDegreesSouthPole() {
            Coordinates southPole = Coordinates.fromDegrees(-90.0, 0.0);
            assertEquals(-90.0, southPole.getLatitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("fromDegrees should normalize 180° and -180° to equivalent values")
        void testFromDegreesDateLine() {
            Coordinates dateLine1 = Coordinates.fromDegrees(0.0, 180.0);
            Coordinates dateLine2 = Coordinates.fromDegrees(0.0, -180.0);

            // Both should represent the same location (±180°)
            // Using IEEEremainder, both normalize to π or -π
            assertEquals(Math.abs(dateLine1.getLongitudeDeg()),
                    Math.abs(dateLine2.getLongitudeDeg()), DELTA);
        }

        @Test
        @DisplayName("fromDegrees should handle zero coordinates")
        void testFromDegreesZero() {
            Coordinates origin = Coordinates.fromDegrees(0.0, 0.0);
            assertEquals(0.0, origin.getLatitudeDeg(), DELTA);
            assertEquals(0.0, origin.getLongitudeDeg(), DELTA);
        }
    }

    // ==================== Getter/Setter Tests ====================

    @Nested
    @DisplayName("Getter/Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Degree getters should return correct values")
        void testDegreeGetters() {
            Coordinates coord = Coordinates.fromDegrees(45.0, 90.0);

            assertEquals(45.0, coord.getLatitudeDeg(), DELTA);
            assertEquals(90.0, coord.getLongitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("setLatitude should clamp values beyond North Pole")
        void testSetLatitudeClampNorth() {
            Coordinates coord = new Coordinates(0, 0);
            coord.setLatitude(Math.PI);
            assertEquals(Math.PI / 2, coord.getLatitude(), DELTA);
        }

        @Test
        @DisplayName("setLatitude should clamp values beyond South Pole")
        void testSetLatitudeClampSouth() {
            Coordinates coord = new Coordinates(0, 0);
            coord.setLatitude(-Math.PI);
            assertEquals(-Math.PI / 2, coord.getLatitude(), DELTA);
        }

        @Test
        @DisplayName("setLatitude should accept valid values")
        void testSetLatitudeValid() {
            Coordinates coord = new Coordinates(0, 0);
            coord.setLatitude(Math.PI / 4);
            assertEquals(Math.PI / 4, coord.getLatitude(), DELTA);
        }

        @Test
        @DisplayName("setLongitude should normalize value > π")
        void testSetLongitudeNormalizationPositive() {
            Coordinates coord = new Coordinates(0, 0);
            coord.setLongitude(Math.toRadians(270.0)); // Should wrap to -90°
            assertEquals(Math.toRadians(-90.0), coord.getLongitude(), DELTA);
        }

        @Test
        @DisplayName("setLongitude should normalize value < -π")
        void testSetLongitudeNormalizationNegative() {
            Coordinates coord = new Coordinates(0, 0);
            coord.setLongitude(Math.toRadians(-270.0)); // Should wrap to 90°
            assertEquals(Math.toRadians(90.0), coord.getLongitude(), DELTA);
        }

        @Test
        @DisplayName("setLatitude should throw exception for NaN")
        void testSetLatitudeNaN() {
            Coordinates coord = new Coordinates(0, 0);
            assertThrows(IllegalArgumentException.class, () -> coord.setLatitude(Double.NaN));
        }

        @Test
        @DisplayName("setLongitude should throw exception for Infinite")
        void testSetLongitudeInfinite() {
            Coordinates coord = new Coordinates(0, 0);
            assertThrows(IllegalArgumentException.class,
                    () -> coord.setLongitude(Double.POSITIVE_INFINITY));
        }
    }

    // ==================== Distance Calculation Tests ====================

    @Nested
    @DisplayName("Distance Calculation Tests")
    class DistanceTests {

        @Test
        @DisplayName("calculateDistance should return 0 for same coordinates")
        void testDistanceSamePoint() {
            Coordinates coord1 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates coord2 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);

            double distance = coord1.calculateDistance(coord2);

            assertEquals(0.0, distance, DELTA);
        }

        @Test
        @DisplayName("calculateDistance should be symmetric")
        void testDistanceSymmetry() {
            Coordinates mannheim = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates heidelberg = Coordinates.fromDegrees(HEIDELBERG_LAT_DEG, HEIDELBERG_LON_DEG);

            double dist1 = mannheim.calculateDistance(heidelberg);
            double dist2 = heidelberg.calculateDistance(mannheim);

            assertEquals(dist1, dist2, DELTA);
        }

        @Test
        @DisplayName("calculateDistance Mannheim to Heidelberg should be approximately 17.8 km")
        void testDistanceMannheimHeidelberg() {
            Coordinates mannheim = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates heidelberg = Coordinates.fromDegrees(HEIDELBERG_LAT_DEG, HEIDELBERG_LON_DEG);

            double distance = mannheim.calculateDistance(heidelberg);

            // Verified distance: approximately 17.8 km = 17800 meters
            assertTrue(distance > 17000 && distance < 19000,
                    "Distance should be approximately 17.8 km, but was: " + distance + " meters");
        }

        @Test
        @DisplayName("calculateDistance static method should match instance method")
        void testDistanceStaticMethod() {
            Coordinates coord1 = Coordinates.fromDegrees(0.0, 0.0);
            Coordinates coord2 = Coordinates.fromDegrees(0.0, 1.0);

            double distanceInstance = coord1.calculateDistance(coord2);
            double distanceStatic = Coordinates.calculateDistance(coord1, coord2);

            assertEquals(distanceInstance, distanceStatic, DELTA);
        }

        @Test
        @DisplayName("calculateDistance should throw exception for null parameter")
        void testDistanceNullParameter() {
            Coordinates coord = Coordinates.fromDegrees(0.0, 0.0);

            assertThrows(IllegalArgumentException.class, () -> coord.calculateDistance(null));
            assertThrows(IllegalArgumentException.class,
                    () -> Coordinates.calculateDistance(coord, null));
            assertThrows(IllegalArgumentException.class,
                    () -> Coordinates.calculateDistance(null, coord));
        }

        @Test
        @DisplayName("calculateDistance across equator should be correct")
        void testDistanceAcrossEquator() {
            Coordinates north = Coordinates.fromDegrees(1.0, 0.0);
            Coordinates south = Coordinates.fromDegrees(-1.0, 0.0);

            double distance = north.calculateDistance(south);

            // 2 degrees of latitude ≈ 222 km
            assertTrue(distance > 220000 && distance < 225000,
                    "Distance should be approximately 222 km, but was: " + (distance / 1000) + " km");
        }

        @Test
        @DisplayName("calculateDistance across International Date Line should be correct")
        void testDistanceAcrossDateLine() {
            Coordinates east = Coordinates.fromDegrees(0.0, 179.0);
            Coordinates west = Coordinates.fromDegrees(0.0, -179.0);

            double distance = east.calculateDistance(west);

            // 2 degrees of longitude at equator ≈ 222 km
            assertTrue(distance > 220000 && distance < 225000,
                    "Distance should be approximately 222 km, but was: " + (distance / 1000) + " km");
        }
    }

    // ==================== Bearing Calculation Tests ====================

    @Nested
    @DisplayName("Bearing Calculation Tests")
    class BearingTests {

        @Test
        @DisplayName("calculateBearing due North should be 0")
        void testBearingNorth() {
            Coordinates start = Coordinates.fromDegrees(49.0, 8.0);
            Coordinates end = Coordinates.fromDegrees(50.0, 8.0);

            double bearing = start.calculateBearing(end);

            assertEquals(0.0, bearing, DELTA);
        }

        @Test
        @DisplayName("calculateBearing due East should be π/2")
        void testBearingEast() {
            Coordinates start = Coordinates.fromDegrees(0.0, 0.0);
            Coordinates end = Coordinates.fromDegrees(0.0, 1.0);

            double bearing = start.calculateBearing(end);

            assertEquals(Math.PI / 2, bearing, DELTA);
        }

        @Test
        @DisplayName("calculateBearing due South should be π")
        void testBearingSouth() {
            Coordinates start = Coordinates.fromDegrees(50.0, 8.0);
            Coordinates end = Coordinates.fromDegrees(49.0, 8.0);

            double bearing = start.calculateBearing(end);

            assertEquals(Math.PI, bearing, DELTA);
        }

        @Test
        @DisplayName("calculateBearing due West should be 3π/2")
        void testBearingWest() {
            Coordinates start = Coordinates.fromDegrees(0.0, 1.0);
            Coordinates end = Coordinates.fromDegrees(0.0, 0.0);

            double bearing = start.calculateBearing(end);

            assertEquals(3 * Math.PI / 2, bearing, DELTA);
        }

        @Test
        @DisplayName("calculateBearing should throw exception for null target")
        void testBearingNullTarget() {
            Coordinates start = Coordinates.fromDegrees(49.0, 8.0);

            assertThrows(IllegalArgumentException.class, () -> start.calculateBearing(null));
        }
    }

    // ==================== Update Method Tests ====================

    @Nested
    @DisplayName("Update Method Tests")
    class UpdateTests {

        @Test
        @DisplayName("update should move North correctly")
        void testUpdateNorth() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLat = coord.getLatitudeDeg();

            // Move North (direction = 0 rad) at 100 km/h for 3600 seconds (1 hour)
            coord.update(100.0, 0.0, 3600.0);

            assertTrue(coord.getLatitudeDeg() > initialLat, "Latitude should increase");
            assertEquals(8.0, coord.getLongitudeDeg(), 0.01);
        }

        @Test
        @DisplayName("update should move East correctly")
        void testUpdateEast() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLon = coord.getLongitudeDeg();

            // Move East (direction = π/2) at 100 km/h for 3600 seconds
            coord.update(100.0, Math.PI / 2, 3600.0);

            assertTrue(coord.getLongitudeDeg() > initialLon, "Longitude should increase");
            assertEquals(49.0, coord.getLatitudeDeg(), 0.01);
        }

        @Test
        @DisplayName("update should move South correctly")
        void testUpdateSouth() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLat = coord.getLatitudeDeg();

            // Move South (direction = π) at 100 km/h for 3600 seconds
            coord.update(100.0, Math.PI, 3600.0);

            assertTrue(coord.getLatitudeDeg() < initialLat, "Latitude should decrease");
            assertEquals(8.0, coord.getLongitudeDeg(), 0.01);
        }

        @Test
        @DisplayName("update should move West correctly")
        void testUpdateWest() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLon = coord.getLongitudeDeg();

            // Move West (direction = 3π/2) at 100 km/h for 3600 seconds
            coord.update(100.0, 3 * Math.PI / 2, 3600.0);

            assertTrue(coord.getLongitudeDeg() < initialLon, "Longitude should decrease");
            assertEquals(49.0, coord.getLatitudeDeg(), 0.01);
        }

        @Test
        @DisplayName("update should handle zero speed")
        void testUpdateZeroSpeed() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLat = coord.getLatitudeDeg();
            double initialLon = coord.getLongitudeDeg();

            coord.update(0.0, 0.0, 3600.0);

            assertEquals(initialLat, coord.getLatitudeDeg(), DELTA);
            assertEquals(initialLon, coord.getLongitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("update should handle zero time")
        void testUpdateZeroTime() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLat = coord.getLatitudeDeg();
            double initialLon = coord.getLongitudeDeg();

            coord.update(100.0, Math.PI / 4, 0.0);

            assertEquals(initialLat, coord.getLatitudeDeg(), DELTA);
            assertEquals(initialLon, coord.getLongitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("update should handle fractional time (double precision)")
        void testUpdateFractionalTime() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLat = coord.getLatitudeDeg();

            // Move North for 0.5 seconds
            coord.update(100.0, 0.0, 0.5);

            assertTrue(coord.getLatitudeDeg() > initialLat, "Should move even with fractional time");
        }

        @Test
        @DisplayName("update should handle diagonal movement (NE)")
        void testUpdateDiagonal() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLat = coord.getLatitudeDeg();
            double initialLon = coord.getLongitudeDeg();

            // Move Northeast (direction = π/4) at 100 km/h for 1 hour
            coord.update(100.0, Math.PI / 4, 3600.0);

            assertTrue(coord.getLatitudeDeg() > initialLat, "Latitude should increase");
            assertTrue(coord.getLongitudeDeg() > initialLon, "Longitude should increase");
        }

        @Test
        @DisplayName("update multiple times should accumulate correctly")
        void testUpdateMultipleTimes() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);

            // Move North for 10 seconds, 10 times = 100 seconds total
            for (int i = 0; i < 10; i++) {
                coord.update(100.0, 0.0, 10.0);
            }

            Coordinates coordSingle = Coordinates.fromDegrees(49.0, 8.0);
            coordSingle.update(100.0, 0.0, 100.0);

            assertEquals(coordSingle.getLatitudeDeg(), coord.getLatitudeDeg(), STRICT_DELTA);
            assertEquals(coordSingle.getLongitudeDeg(), coord.getLongitudeDeg(), STRICT_DELTA);
        }

        @Test
        @DisplayName("update should clamp latitude at North Pole")
        void testUpdateNorthPoleClamp() {
            Coordinates coord = Coordinates.fromDegrees(89.0, 0.0);

            // Try to move North beyond pole
            coord.update(1000.0, 0.0, 3600.0);

            assertEquals(90.0, coord.getLatitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("update should clamp latitude at South Pole")
        void testUpdateSouthPoleClamp() {
            Coordinates coord = Coordinates.fromDegrees(-89.0, 0.0);

            // Try to move South beyond pole
            coord.update(1000.0, Math.PI, 3600.0);

            assertEquals(-90.0, coord.getLatitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("update should handle movement near poles gracefully")
        void testUpdateNearPole() {
            Coordinates coord = Coordinates.fromDegrees(89.9, 0.0);

            // Move East - longitude change should be handled gracefully
            coord.update(10.0, Math.PI / 2, 100.0);

            assertTrue(coord.getLatitudeDeg() >= -90.0 && coord.getLatitudeDeg() <= 90.0);
            assertTrue(coord.getLongitudeDeg() >= -180.0 && coord.getLongitudeDeg() <= 180.0);
        }

        @Test
        @DisplayName("update should wrap longitude when crossing Date Line eastward")
        void testUpdateCrossDateLineEast() {
            Coordinates coord = Coordinates.fromDegrees(0.0, 179.0);

            // Move East at high speed
            coord.update(500.0, Math.PI / 2, 3600.0);

            // Should have wrapped to negative longitude
            assertTrue(coord.getLongitudeDeg() >= -180.0 && coord.getLongitudeDeg() <= 180.0,
                    "Longitude should be normalized after crossing Date Line");
        }

        @Test
        @DisplayName("update should wrap longitude when crossing Date Line westward")
        void testUpdateCrossDateLineWest() {
            Coordinates coord = Coordinates.fromDegrees(0.0, -179.0);

            // Move West at high speed
            coord.update(500.0, 3 * Math.PI / 2, 3600.0);

            // Should have wrapped to positive longitude
            assertTrue(coord.getLongitudeDeg() >= -180.0 && coord.getLongitudeDeg() <= 180.0,
                    "Longitude should be normalized after crossing Date Line");
        }
    }

    // ==================== Input Validation Tests ====================

    @Nested
    @DisplayName("Input Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("update should throw exception for negative speed")
        void testUpdateNegativeSpeed() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);

            assertThrows(IllegalArgumentException.class,
                    () -> coord.update(-100.0, 0.0, 3600.0));
        }

        @Test
        @DisplayName("update should throw exception for negative time")
        void testUpdateNegativeTime() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);

            assertThrows(IllegalArgumentException.class,
                    () -> coord.update(100.0, 0.0, -3600.0));
        }

        @Test
        @DisplayName("update should throw exception for NaN speed")
        void testUpdateNaNSpeed() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);

            assertThrows(IllegalArgumentException.class,
                    () -> coord.update(Double.NaN, 0.0, 3600.0));
        }

        @Test
        @DisplayName("update should throw exception for NaN direction")
        void testUpdateNaNDirection() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);

            assertThrows(IllegalArgumentException.class,
                    () -> coord.update(100.0, Double.NaN, 3600.0));
        }

        @Test
        @DisplayName("update should throw exception for NaN time")
        void testUpdateNaNTime() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);

            assertThrows(IllegalArgumentException.class,
                    () -> coord.update(100.0, 0.0, Double.NaN));
        }

        @Test
        @DisplayName("update should throw exception for Infinite speed")
        void testUpdateInfiniteSpeed() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);

            assertThrows(IllegalArgumentException.class,
                    () -> coord.update(Double.POSITIVE_INFINITY, 0.0, 3600.0));
        }

        @Test
        @DisplayName("update should throw exception for Infinite direction")
        void testUpdateInfiniteDirection() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);

            assertThrows(IllegalArgumentException.class,
                    () -> coord.update(100.0, Double.NEGATIVE_INFINITY, 3600.0));
        }

        @Test
        @DisplayName("update should throw exception for Infinite time")
        void testUpdateInfiniteTime() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);

            assertThrows(IllegalArgumentException.class,
                    () -> coord.update(100.0, 0.0, Double.POSITIVE_INFINITY));
        }
    }

    // ==================== equals() and hashCode() Tests ====================

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("equals should return true for same coordinates")
        void testEqualsSameCoordinates() {
            Coordinates coord1 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates coord2 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);

            assertEquals(coord1, coord2);
        }

        @Test
        @DisplayName("equals should return false for different coordinates")
        void testEqualsDifferentCoordinates() {
            Coordinates coord1 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates coord2 = Coordinates.fromDegrees(HEIDELBERG_LAT_DEG, HEIDELBERG_LON_DEG);

            assertNotEquals(coord1, coord2);
        }

        @Test
        @DisplayName("equals should return true for same instance")
        void testEqualsSameInstance() {
            Coordinates coord = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);

            assertEquals(coord, coord);
        }

        @Test
        @DisplayName("equals should return false for null")
        void testEqualsNull() {
            Coordinates coord = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);

            assertNotEquals(null, coord);
        }

        @Test
        @DisplayName("equals should return false for different type")
        void testEqualsDifferentType() {
            Coordinates coord = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);

            assertNotEquals("not a coordinate", coord);
        }

        @Test
        @DisplayName("hashCode should be equal for equal coordinates")
        void testHashCodeEqual() {
            Coordinates coord1 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates coord2 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);

            assertEquals(coord1.hashCode(), coord2.hashCode());
        }

        @Test
        @DisplayName("hashCode should generally differ for different coordinates")
        void testHashCodeDifferent() {
            Coordinates coord1 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates coord2 = Coordinates.fromDegrees(HEIDELBERG_LAT_DEG, HEIDELBERG_LON_DEG);

            // Note: Different objects CAN have same hashCode, but usually shouldn't
            assertNotEquals(coord1.hashCode(), coord2.hashCode());
        }

        @Test
        @DisplayName("Coordinates should work correctly in HashSet")
        void testHashSetUsage() {
            Set<Coordinates> set = new HashSet<>();
            Coordinates coord1 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates coord2 = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates coord3 = Coordinates.fromDegrees(HEIDELBERG_LAT_DEG, HEIDELBERG_LON_DEG);

            set.add(coord1);
            set.add(coord2); // Should not add (duplicate)
            set.add(coord3);

            assertEquals(2, set.size());
            assertTrue(set.contains(coord1));
            assertTrue(set.contains(coord3));
        }
    }

    // ==================== Copy Method Tests ====================

    @Nested
    @DisplayName("Copy Method Tests")
    class CopyTests {

        @Test
        @DisplayName("copy should create independent copy")
        void testCopyIndependent() {
            Coordinates original = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates copy = original.copy();

            // Modify original
            original.setLatitude(0);

            // Copy should be unchanged
            assertEquals(MANNHEIM_LAT_DEG, copy.getLatitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("copy should have equal values")
        void testCopyEqual() {
            Coordinates original = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates copy = original.copy();

            assertEquals(original, copy);
        }

        @Test
        @DisplayName("copy should not be same instance")
        void testCopyNotSameInstance() {
            Coordinates original = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates copy = original.copy();

            assertNotSame(original, copy);
        }
    }

    // ==================== toString Tests ====================

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should format coordinates correctly")
        void testToString() {
            Coordinates coord = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            String result = coord.toString();

            assertTrue(result.contains("49.4875"), "Should contain latitude");
            assertTrue(result.contains("8.4660"), "Should contain longitude");
            assertTrue(result.contains("°"), "Should contain degree symbol");
        }

        @Test
        @DisplayName("toString should handle negative values")
        void testToStringNegative() {
            Coordinates coord = Coordinates.fromDegrees(-45.0, -90.0);
            String result = coord.toString();

            assertTrue(result.contains("-45"), "Should contain negative latitude");
            assertTrue(result.contains("-90"), "Should contain negative longitude");
        }

        @Test
        @DisplayName("toString should use Lon abbreviation")
        void testToStringFormat() {
            Coordinates coord = Coordinates.fromDegrees(0.0, 0.0);
            String result = coord.toString();

            assertTrue(result.contains("Lat:"), "Should contain Lat:");
            assertTrue(result.contains("Lon:"), "Should contain Lon:");
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Integration: Journey from Mannheim toward Heidelberg using calculated bearing")
        void testIntegrationMannheimToHeidelberg() {
            Coordinates start = Coordinates.fromDegrees(MANNHEIM_LAT_DEG, MANNHEIM_LON_DEG);
            Coordinates target = Coordinates.fromDegrees(HEIDELBERG_LAT_DEG, HEIDELBERG_LON_DEG);

            double initialDistance = start.calculateDistance(target);

            // Calculate actual bearing to Heidelberg
            double bearing = start.calculateBearing(target);

            // Move at 60 km/h for 10 minutes (600 seconds)
            start.update(60.0, bearing, 600.0);

            double finalDistance = start.calculateDistance(target);

            // After moving toward target, distance should decrease
            assertTrue(finalDistance < initialDistance,
                    "Distance should decrease when moving toward target. " +
                            "Initial: " + initialDistance + ", Final: " + finalDistance);
        }

        @Test
        @DisplayName("Integration: Round trip should return to approximately same location")
        void testIntegrationRoundTrip() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLat = coord.getLatitudeDeg();
            double initialLon = coord.getLongitudeDeg();

            // Move North for 1 hour
            coord.update(100.0, 0.0, 3600.0);
            // Move South for 1 hour
            coord.update(100.0, Math.PI, 3600.0);

            // Should return to approximately the same latitude
            assertEquals(initialLat, coord.getLatitudeDeg(), 0.01);
            assertEquals(initialLon, coord.getLongitudeDeg(), 0.01);
        }

        @Test
        @DisplayName("Integration: Moving in a square should return near start")
        void testIntegrationSquareMovement() {
            Coordinates coord = Coordinates.fromDegrees(49.0, 8.0);
            double initialLat = coord.getLatitudeDeg();
            double initialLon = coord.getLongitudeDeg();

            double speed = 50.0;
            double time = 1800.0; // 30 minutes

            // Move North, East, South, West
            coord.update(speed, 0.0, time);           // North
            coord.update(speed, Math.PI / 2, time);   // East
            coord.update(speed, Math.PI, time);       // South
            coord.update(speed, 3 * Math.PI / 2, time); // West

            // Should return close to start (some error due to Earth's curvature)
            assertEquals(initialLat, coord.getLatitudeDeg(), 0.1);
            assertEquals(initialLon, coord.getLongitudeDeg(), 0.1);
        }

        @Test
        @DisplayName("Integration: Distance traveled matches expected for known speed and time")
        void testIntegrationDistanceTraveled() {
            Coordinates start = Coordinates.fromDegrees(49.0, 8.0);
            Coordinates end = start.copy();

            // Move at 100 km/h for 1 hour = 100 km = 100,000 meters
            end.update(100.0, 0.0, 3600.0);

            double distance = start.calculateDistance(end);

            // Should be approximately 100 km
            assertEquals(100000.0, distance, 1000.0); // Allow 1 km tolerance
        }
    }
}