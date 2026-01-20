package dhbw.trasima;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration tests for the VirtualVehicle class.
 */
class VirtualVehicleTest {

    private static final double DELTA = 0.0001;

    // Test locations
    private static final double START_LAT = 49.4747;
    private static final double START_LON = 8.5344;
    private static final double TARGET_LAT = 49.4757; // ~100m north
    private static final double TARGET_LON = 8.5344;

    private Coordinates startLocation;
    private Coordinates targetLocation;
    private MockPublisher publisher;

    @BeforeEach
    void setUp() {
        startLocation = Coordinates.fromDegrees(START_LAT, START_LON);
        targetLocation = Coordinates.fromDegrees(TARGET_LAT, TARGET_LON);
        publisher = new MockPublisher();
    }

    // ==================== Mock Publisher ====================

    /**
     * Simple mock publisher that does nothing (for testing).
     */
    static class MockPublisher implements IPublisher<VirtualVehicle> {
        @Override
        public void publish(VirtualVehicle vehicle) {
            // Do nothing - just for testing
        }
    }

    // ==================== Unit Tests ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor should initialize with correct values")
        void testConstructorInitialization() {
            VirtualVehicle v2 = new VirtualVehicle(startLocation, 50.0, 100, 1000, publisher);

            assertNotNull(v2.getId(), "ID should be generated");
            assertEquals(startLocation, v2.getLocation(), "Location should match start");
            assertNull(v2.getTargetLocation(), "Target should be null initially");
            assertEquals(0.0, v2.getSpeed(), DELTA, "Speed should be 0 initially");
            assertEquals(50.0, v2.getMaxSpeed(), DELTA, "MaxSpeed should be set");
            assertEquals(0.0, v2.getDirection(), DELTA, "Direction should be 0 initially");
            assertFalse(v2.isDriving(), "Should not be driving initially");
        }

        @Test
        @DisplayName("Constructor should generate unique IDs")
        void testUniqueIds() {
            VirtualVehicle v1 = new VirtualVehicle(startLocation, 50.0, 100, 1000, publisher);
            VirtualVehicle v2 = new VirtualVehicle(startLocation, 50.0, 100, 1000, publisher);

            assertNotEquals(v1.getId(), v2.getId(), "Each vehicle should have unique ID");
        }

        @Test
        @DisplayName("Constructor should throw exception for negative maxSpeed")
        void testConstructorNegativeMaxSpeed() {
            assertThrows(IllegalArgumentException.class,
                    () -> new VirtualVehicle(startLocation, -50.0, 100, 1000, publisher));
        }

        @Test
        @DisplayName("Constructor should throw exception for zero maxSpeed")
        void testConstructorZeroMaxSpeed() {
            assertThrows(IllegalArgumentException.class,
                    () -> new VirtualVehicle(startLocation, 0.0, 100, 1000, publisher));
        }

        @Test
        @DisplayName("Constructor should throw exception for NaN maxSpeed")
        void testConstructorNaNMaxSpeed() {
            assertThrows(IllegalArgumentException.class,
                    () -> new VirtualVehicle(startLocation, Double.NaN, 100, 1000, publisher));
        }

        @Test
        @DisplayName("Constructor should throw exception for Infinite maxSpeed")
        void testConstructorInfiniteMaxSpeed() {
            assertThrows(IllegalArgumentException.class,
                    () -> new VirtualVehicle(startLocation, Double.POSITIVE_INFINITY, 100, 1000, publisher));
            assertThrows(IllegalArgumentException.class,
                    () -> new VirtualVehicle(startLocation, Double.NEGATIVE_INFINITY, 100, 1000, publisher));
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("getLocation should return current location")
        void testGetLocation() {
            VirtualVehicle v2 = new VirtualVehicle(startLocation, 50.0, 100, 1000, publisher);

            assertEquals(START_LAT, v2.getLocation().getLatitudeDeg(), DELTA);
            assertEquals(START_LON, v2.getLocation().getLongitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("getMaxSpeed should return configured max speed")
        void testGetMaxSpeed() {
            VirtualVehicle v2 = new VirtualVehicle(startLocation, 75.0, 100, 1000, publisher);

            assertEquals(75.0, v2.getMaxSpeed(), DELTA);
        }
    }

    @Nested
    @DisplayName("toString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString without target should show 'none'")
        void testToStringWithoutTarget() {
            VirtualVehicle v2 = new VirtualVehicle(startLocation, 50.0, 100, 1000, publisher);

            // Check actual values via getters
            assertEquals(0.0, v2.getSpeed(), DELTA, "Speed should be 0");
            assertNull(v2.getTargetLocation(), "Target should be null");

            // Check string format
            String result = v2.toString();
            assertTrue(result.contains("V2["), "Should contain V2 prefix");
            assertTrue(result.contains("none"), "Should show 'none' for target");
        }

        @Test
        @DisplayName("toString should contain vehicle ID")
        void testToStringContainsId() {
            VirtualVehicle v2 = new VirtualVehicle(startLocation, 50.0, 100, 1000, publisher);
            String id = v2.getId().toString().substring(0, 8);
            String result = v2.toString();

            assertTrue(result.contains(id), "Should contain first 8 chars of UUID");
        }
    }

    // ==================== Integration Tests ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("drive() should set isDriving to true and start movement")
        void testDriveStartsThread() throws InterruptedException {
            VirtualVehicle v2 = new VirtualVehicle(startLocation, 50.0, 100, 1000, publisher);

            assertFalse(v2.isDriving(), "Should not be driving before drive()");
            assertEquals(0.0, v2.getSpeed(), DELTA, "Speed should be 0 before drive()");

            v2.drive(targetLocation);

            // Give thread time to start
            Thread.sleep(50);

            assertTrue(v2.isDriving(), "Should be driving after drive()");
            assertEquals(50.0, v2.getSpeed(), DELTA, "Speed should be maxSpeed while driving");
            assertNotNull(v2.getTargetLocation(), "Target should be set");
        }

        @Test
        @DisplayName("Vehicle should arrive at target and stop")
        void testDriveToTarget() throws InterruptedException {
            // Use high speed for faster test
            VirtualVehicle v2 = new VirtualVehicle(startLocation, 360.0, 100, 500, publisher);

            v2.drive(targetLocation);

            // Wait for arrival (max 5 seconds)
            long timeout = System.currentTimeMillis() + 5000;
            while (v2.isDriving() && System.currentTimeMillis() < timeout) {
                Thread.sleep(100);
            }

            assertFalse(v2.isDriving(), "Should have stopped driving");
            assertEquals(0.0, v2.getSpeed(), DELTA, "Speed should be 0 after arrival");
            assertNull(v2.getTargetLocation(), "Target should be null after arrival");
        }

        @Test
        @DisplayName("Location should change while driving")
        void testLocationChangesWhileDriving() throws InterruptedException {
            VirtualVehicle v2 = new VirtualVehicle(startLocation, 50.0, 100, 1000, publisher);

            double initialLat = v2.getLocation().getLatitudeDeg();

            v2.drive(targetLocation);

            // Wait for a few updates
            Thread.sleep(500);

            double currentLat = v2.getLocation().getLatitudeDeg();

            assertTrue(v2.isDriving(), "Should still be driving");
            assertNotEquals(initialLat, currentLat, "Location should have changed");
            assertTrue(currentLat > initialLat, "Should have moved north (latitude increased)");
        }

        @Test
        @DisplayName("Multiple vehicles should drive independently")
        void testMultipleVehicles() throws InterruptedException {
            Coordinates start1 = Coordinates.fromDegrees(49.47, 8.53);
            Coordinates start2 = Coordinates.fromDegrees(49.48, 8.54);
            Coordinates target = Coordinates.fromDegrees(49.50, 8.56);

            VirtualVehicle v1 = new VirtualVehicle(start1, 100.0, 100, 500, publisher);
            VirtualVehicle v2 = new VirtualVehicle(start2, 150.0, 100, 500, publisher);

            v1.drive(target);
            v2.drive(target);

            Thread.sleep(200);

            assertTrue(v1.isDriving(), "V1 should be driving");
            assertTrue(v2.isDriving(), "V2 should be driving");
            assertEquals(100.0, v1.getSpeed(), DELTA, "V1 should have correct speed");
            assertEquals(150.0, v2.getSpeed(), DELTA, "V2 should have correct speed");
        }
    }
}