package dhbw.trasima;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Factory for creating VirtualVehicle instances with configurable defaults.
 * Uses a builder pattern for flexible configuration.
 *
 * <p>Example usage:</p>
 * <pre>
 * VirtualVehicleFactory factory = new VirtualVehicleFactory()
 *     .withLocation(CoordinateSuppliers.randomInMannheim())
 *     .withTargetLocation(schloss)
 *     .withMaxSpeed(50.0);
 *
 * List&lt;VirtualVehicle&gt; vehicles = factory.createAndDrive(10);
 * </pre>
 */
public class VirtualVehicleFactory {

    // Defaults
    private static final double DEFAULT_MAX_SPEED = 50.0;
    private static final int DEFAULT_DELTA_T = 50;
    private static final int DEFAULT_PUBLISH_INTERVAL = 1000;

    // Configuration
    private Supplier<Coordinates> locationSupplier;
    private Supplier<Coordinates> targetLocationSupplier;
    private Supplier<Double> maxSpeedSupplier;
    private int deltaT;
    private int publishInterval;
    private IPublisher<VirtualVehicle> publisher;

    /**
     * Creates a new factory with default values.
     * Defaults: maxSpeed=50 km/h, deltaT=50ms, publishInterval=1000ms, publisher=V2ConsolePublisher
     */
    public VirtualVehicleFactory() {
        this.locationSupplier = null;
        this.targetLocationSupplier = null;
        this.maxSpeedSupplier = () -> DEFAULT_MAX_SPEED;
        this.deltaT = DEFAULT_DELTA_T;
        this.publishInterval = DEFAULT_PUBLISH_INTERVAL;
        this.publisher = new V2ConsolePublisher();
    }

    /**
     * Creates a new factory with a fixed start location.
     *
     * @param location The start location for created vehicles
     */
    public VirtualVehicleFactory(Coordinates location) {
        this();
        this.locationSupplier = () -> location;
    }

    /**
     * Creates a new factory with a location supplier.
     *
     * @param locationSupplier Supplier that provides start locations
     */
    public VirtualVehicleFactory(Supplier<Coordinates> locationSupplier) {
        this();
        this.locationSupplier = locationSupplier;
    }

    // ==================== Builder Methods ====================

    /**
     * Sets a fixed start location for created vehicles.
     *
     * @param location The start location
     * @return This factory for chaining
     */
    public VirtualVehicleFactory withLocation(Coordinates location) {
        this.locationSupplier = () -> location;
        return this;
    }

    /**
     * Sets a location supplier for created vehicles.
     *
     * @param locationSupplier Supplier that provides start locations
     * @return This factory for chaining
     */
    public VirtualVehicleFactory withLocation(Supplier<Coordinates> locationSupplier) {
        this.locationSupplier = locationSupplier;
        return this;
    }

    /**
     * Sets a fixed target location for created vehicles.
     *
     * @param targetLocation The target location
     * @return This factory for chaining
     */
    public VirtualVehicleFactory withTargetLocation(Coordinates targetLocation) {
        this.targetLocationSupplier = () -> targetLocation;
        return this;
    }

    /**
     * Sets a target location supplier for created vehicles.
     *
     * @param targetLocationSupplier Supplier that provides target locations
     * @return This factory for chaining
     */
    public VirtualVehicleFactory withTargetLocation(Supplier<Coordinates> targetLocationSupplier) {
        this.targetLocationSupplier = targetLocationSupplier;
        return this;
    }

    /**
     * Sets a fixed max speed for created vehicles.
     *
     * @param maxSpeed The max speed in km/h
     * @return This factory for chaining
     */
    public VirtualVehicleFactory withMaxSpeed(double maxSpeed) {
        this.maxSpeedSupplier = () -> maxSpeed;
        return this;
    }

    /**
     * Sets a max speed supplier for created vehicles.
     *
     * @param maxSpeedSupplier Supplier that provides max speeds
     * @return This factory for chaining
     */
    public VirtualVehicleFactory withMaxSpeed(Supplier<Double> maxSpeedSupplier) {
        this.maxSpeedSupplier = maxSpeedSupplier;
        return this;
    }

    /**
     * Sets the simulation update interval.
     *
     * @param deltaT Time between updates in milliseconds
     * @return This factory for chaining
     */
    public VirtualVehicleFactory withDeltaT(int deltaT) {
        this.deltaT = deltaT;
        return this;
    }

    /**
     * Sets the publish interval.
     *
     * @param publishInterval Time between publish calls in milliseconds
     * @return This factory for chaining
     */
    public VirtualVehicleFactory withPublishInterval(int publishInterval) {
        this.publishInterval = publishInterval;
        return this;
    }

    /**
     * Sets the publisher for created vehicles.
     *
     * @param publisher Publisher for vehicle output
     * @return This factory for chaining
     */
    public VirtualVehicleFactory withPublisher(IPublisher<VirtualVehicle> publisher) {
        this.publisher = publisher;
        return this;
    }

    // ==================== Factory Methods ====================

    /**
     * Creates a single VirtualVehicle without starting it.
     *
     * @return The created vehicle
     * @throws IllegalStateException if location is not set
     */
    public VirtualVehicle create() {
        validateForCreate();

        Coordinates start = locationSupplier.get();
        double maxSpeed = maxSpeedSupplier.get();

        return new VirtualVehicle(start, maxSpeed, deltaT, publishInterval, publisher);
    }

    /**
     * Creates multiple VirtualVehicles without starting them.
     *
     * @param count Number of vehicles to create
     * @return List of created vehicles
     * @throws IllegalStateException    if location is not set
     * @throws IllegalArgumentException if count is less than 1
     */
    public List<VirtualVehicle> create(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Count must be at least 1");
        }

        List<VirtualVehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            vehicles.add(create());
        }
        return vehicles;
    }

    /**
     * Creates a single VirtualVehicle and starts driving to target.
     *
     * @return The created vehicle (already driving)
     * @throws IllegalStateException if location or targetLocation is not set
     */
    public VirtualVehicle createAndDrive() {
        validateForCreateAndDrive();

        VirtualVehicle vehicle = create();
        Coordinates target = targetLocationSupplier.get();
        vehicle.drive(target);

        return vehicle;
    }

    /**
     * Creates multiple VirtualVehicles and starts them driving to target.
     *
     * @param count Number of vehicles to create
     * @return List of created vehicles (already driving)
     * @throws IllegalStateException    if location or targetLocation is not set
     * @throws IllegalArgumentException if count is less than 1
     */
    public List<VirtualVehicle> createAndDrive(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Count must be at least 1");
        }

        validateForCreateAndDrive();

        List<VirtualVehicle> vehicles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            VirtualVehicle vehicle = create();
            Coordinates target = targetLocationSupplier.get();
            vehicle.drive(target);
            vehicles.add(vehicle);
        }
        return vehicles;
    }

    // ==================== Validation ====================

    /**
     * Validates that all required fields for create() are set.
     *
     * @throws IllegalStateException if validation fails
     */
    private void validateForCreate() {
        if (locationSupplier == null) {
            throw new IllegalStateException("Location must be set before creating vehicles");
        }
    }

    /**
     * Validates that all required fields for createAndDrive() are set.
     *
     * @throws IllegalStateException if validation fails
     */
    private void validateForCreateAndDrive() {
        validateForCreate();
        if (targetLocationSupplier == null) {
            throw new IllegalStateException("Target location must be set before driving");
        }
    }
}