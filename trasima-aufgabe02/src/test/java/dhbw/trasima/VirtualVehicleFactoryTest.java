package dhbw.trasima;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Unit tests for the VirtualVehicleFactory class.
 */
class VirtualVehicleFactoryTest {

    private static final double DELTA = 0.0001;

    private Coordinates startLocation;
    private Coordinates targetLocation;

    @BeforeEach
    void setUp() {
        startLocation = Coordinates.fromDegrees(49.47, 8.53);
        targetLocation = Coordinates.fromDegrees(49.48, 8.54);
    }

    // ==================== Constructor Tests ====================

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor with Coordinates should set location")
        void testConstructorWithCoordinates_SetsLocation() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation);

            VirtualVehicle vehicle = factory.create();

            assertEquals(startLocation.getLatitudeDeg(), vehicle.getLocation().getLatitudeDeg(), DELTA);
            assertEquals(startLocation.getLongitudeDeg(), vehicle.getLocation().getLongitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("Constructor with Supplier should use supplier")
        void testConstructorWithSupplier_SetsLocationSupplier() {
            Supplier<Coordinates> supplier = () -> Coordinates.fromDegrees(49.50, 8.50);
            VirtualVehicleFactory factory = new VirtualVehicleFactory(supplier);

            VirtualVehicle vehicle = factory.create();

            assertEquals(49.50, vehicle.getLocation().getLatitudeDeg(), DELTA);
            assertEquals(8.50, vehicle.getLocation().getLongitudeDeg(), DELTA);
        }
    }

    // ==================== Builder Method Tests ====================

    @Nested
    @DisplayName("Builder Method Tests")
    class BuilderMethodTests {

        @Test
        @DisplayName("withLocation(Coordinates) should set fixed location")
        void testWithLocation_Coordinates() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory()
                    .withLocation(startLocation);

            VirtualVehicle vehicle = factory.create();

            assertEquals(startLocation.getLatitudeDeg(), vehicle.getLocation().getLatitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("withLocation(Supplier) should set location supplier")
        void testWithLocation_Supplier() {
            Supplier<Coordinates> supplier = () -> Coordinates.fromDegrees(49.50, 8.50);
            VirtualVehicleFactory factory = new VirtualVehicleFactory()
                    .withLocation(supplier);

            VirtualVehicle vehicle = factory.create();

            assertEquals(49.50, vehicle.getLocation().getLatitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("withTargetLocation(Coordinates) should set fixed target")
        void testWithTargetLocation_Coordinates() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withTargetLocation(targetLocation);

            VirtualVehicle vehicle = factory.createAndDrive();

            // Vehicle is driving, so it has a target (checked via isDriving)
            assertTrue(vehicle.isDriving());
        }

        @Test
        @DisplayName("withTargetLocation(Supplier) should set target supplier")
        void testWithTargetLocation_Supplier() {
            Supplier<Coordinates> targetSupplier = () -> Coordinates.fromDegrees(49.48, 8.54);
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withTargetLocation(targetSupplier);

            VirtualVehicle vehicle = factory.createAndDrive();

            assertTrue(vehicle.isDriving());
        }

        @Test
        @DisplayName("withMaxSpeed(double) should set fixed max speed")
        void testWithMaxSpeed_Constant() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withMaxSpeed(80.0);

            VirtualVehicle vehicle = factory.create();

            assertEquals(80.0, vehicle.getMaxSpeed(), DELTA);
        }

        @Test
        @DisplayName("withMaxSpeed(Supplier) should set max speed supplier")
        void testWithMaxSpeed_Supplier() {
            Supplier<Double> speedSupplier = () -> 100.0;
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withMaxSpeed(speedSupplier);

            VirtualVehicle vehicle = factory.create();

            assertEquals(100.0, vehicle.getMaxSpeed(), DELTA);
        }

        @Test
        @DisplayName("withDeltaT should set deltaT")
        void testWithDeltaT() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withDeltaT(200);

            // deltaT is not exposed via getter, but we can verify vehicle is created
            VirtualVehicle vehicle = factory.create();
            assertNotNull(vehicle);
        }

        @Test
        @DisplayName("withPublishInterval should set publish interval")
        void testWithPublishInterval() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withPublishInterval(2000);

            // publishInterval is not exposed via getter, but we can verify vehicle is created
            VirtualVehicle vehicle = factory.create();
            assertNotNull(vehicle);
        }

        @Test
        @DisplayName("Builder methods should be chainable")
        void testBuilderChaining() {
            VirtualVehicle vehicle = new VirtualVehicleFactory()
                    .withLocation(startLocation)
                    .withTargetLocation(targetLocation)
                    .withMaxSpeed(60.0)
                    .withDeltaT(100)
                    .withPublishInterval(500)
                    .createAndDrive();

            assertNotNull(vehicle);
            assertEquals(60.0, vehicle.getMaxSpeed(), DELTA);
            assertTrue(vehicle.isDriving());
        }
    }

    // ==================== Factory Method Tests ====================

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("create() should return a vehicle")
        void testCreate_ReturnsVehicle() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation);

            VirtualVehicle vehicle = factory.create();

            assertNotNull(vehicle);
        }

        @Test
        @DisplayName("create() should return vehicle with correct location")
        void testCreate_VehicleHasCorrectLocation() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation);

            VirtualVehicle vehicle = factory.create();

            assertEquals(startLocation.getLatitudeDeg(), vehicle.getLocation().getLatitudeDeg(), DELTA);
            assertEquals(startLocation.getLongitudeDeg(), vehicle.getLocation().getLongitudeDeg(), DELTA);
        }

        @Test
        @DisplayName("create() should return vehicle with correct maxSpeed")
        void testCreate_VehicleHasCorrectMaxSpeed() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withMaxSpeed(75.0);

            VirtualVehicle vehicle = factory.create();

            assertEquals(75.0, vehicle.getMaxSpeed(), DELTA);
        }

        @Test
        @DisplayName("create() should use supplier for each vehicle")
        void testCreate_UsesSupplierForEachVehicle() {
            Supplier<Coordinates> randomSupplier = CoordinateSuppliers.randomInArea(49.0, 50.0, 8.0, 9.0);
            VirtualVehicleFactory factory = new VirtualVehicleFactory(randomSupplier);

            Set<Double> latitudes = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                VirtualVehicle vehicle = factory.create();
                latitudes.add(vehicle.getLocation().getLatitudeDeg());
            }

            assertTrue(latitudes.size() > 1, "Each vehicle should get different coordinates from supplier");
        }

        @Test
        @DisplayName("create(count) should return correct number of vehicles")
        void testCreateCount_ReturnsCorrectNumber() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation);

            List<VirtualVehicle> vehicles = factory.create(5);

            assertEquals(5, vehicles.size());
        }

        @Test
        @DisplayName("createAndDrive() should return driving vehicle")
        void testCreateAndDrive_VehicleIsDriving() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withTargetLocation(targetLocation);

            VirtualVehicle vehicle = factory.createAndDrive();

            assertTrue(vehicle.isDriving());
        }

        @Test
        @DisplayName("createAndDrive(count) should return all driving vehicles")
        void testCreateAndDriveCount_AllDriving() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withTargetLocation(targetLocation);

            List<VirtualVehicle> vehicles = factory.createAndDrive(5);

            assertEquals(5, vehicles.size());
            for (VirtualVehicle vehicle : vehicles) {
                assertTrue(vehicle.isDriving(), "All vehicles should be driving");
            }
        }
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("create() should throw if no location set")
        void testCreate_ThrowsIfNoLocation() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory();

            assertThrows(IllegalStateException.class, () -> factory.create());
        }

        @Test
        @DisplayName("createAndDrive() should throw if no location set")
        void testCreateAndDrive_ThrowsIfNoLocation() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory()
                    .withTargetLocation(targetLocation);

            assertThrows(IllegalStateException.class, () -> factory.createAndDrive());
        }

        @Test
        @DisplayName("createAndDrive() should throw if no target set")
        void testCreateAndDrive_ThrowsIfNoTarget() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation);

            assertThrows(IllegalStateException.class, () -> factory.createAndDrive());
        }

        @Test
        @DisplayName("create(count) should throw if count less than 1")
        void testCreateCount_ThrowsIfCountLessThanOne() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation);

            assertThrows(IllegalArgumentException.class, () -> factory.create(0));
            assertThrows(IllegalArgumentException.class, () -> factory.create(-1));
        }

        @Test
        @DisplayName("createAndDrive(count) should throw if count less than 1")
        void testCreateAndDriveCount_ThrowsIfCountLessThanOne() {
            VirtualVehicleFactory factory = new VirtualVehicleFactory(startLocation)
                    .withTargetLocation(targetLocation);

            assertThrows(IllegalArgumentException.class, () -> factory.createAndDrive(0));
            assertThrows(IllegalArgumentException.class, () -> factory.createAndDrive(-1));
        }
    }
}