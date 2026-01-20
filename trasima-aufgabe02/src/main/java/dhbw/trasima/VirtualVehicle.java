package dhbw.trasima;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Represents a Virtual Vehicle (V2) that can drive to target coordinates.
 * Implements Runnable for concurrent simulation in separate threads.
 *
 * The vehicle drives at a constant speed from its current position
 * to a target position, then stops automatically.
 */
public class VirtualVehicle implements Runnable {

    /** Distance in meters at which the vehicle is considered "arrived" */
    private static final double ARRIVAL_THRESHOLD = 10.0;

    /** Unique identifier for this vehicle */
    private final UUID id;

    /** Current position of the vehicle */
    private Coordinates location;

    /** Target position to drive to (null if not driving) */
    private Coordinates targetLocation;

    /** Current direction in radians (0 = North, π/2 = East, etc.) */
    private double direction;

    /** Current speed in km/h (0 when stopped) */
    private double speed;

    /** Whether the vehicle is currently driving to a target */
    private boolean isDriving;

    /** Maximum speed in km/h */
    private final double maxSpeed;

    /** Time between position updates in milliseconds */
    private final int deltaT;

    /** Time between publish calls in milliseconds */
    private final int publishInterval;

    /** Publisher for outputting vehicle state */
    private final IPublisher<VirtualVehicle> publisher;

    /**
     * Creates a new VirtualVehicle.
     *
     * @param start           Starting coordinates
     * @param maxSpeed        Maximum speed in km/h when driving
     * @param deltaT          Time between simulation updates in milliseconds
     * @param publishInterval Time between publish calls in milliseconds
     * @param publisher       Publisher for output
     */
    public VirtualVehicle(Coordinates start, double maxSpeed, int deltaT, int publishInterval, IPublisher<VirtualVehicle> publisher) {
        if (maxSpeed<=0 || Double.isNaN(maxSpeed) || Double.isInfinite(maxSpeed)){
            throw new IllegalArgumentException("MaxSpeed cannot be Zero, negative, NaN or Infinite");
        }

        this.id = UUID.randomUUID();
        this.location = start;
        this.targetLocation = null;
        this.direction = 0.0;
        this.speed = 0.0;
        this.isDriving = false;
        this.maxSpeed = maxSpeed;
        this.deltaT = deltaT;
        this.publishInterval = publishInterval;
        this.publisher = publisher;
    }

    /**
     * Creates a new factory for building VirtualVehicles.
     *
     * @return A new VirtualVehicleFactory with default settings
     */
    public static VirtualVehicleFactory factory() {
        return new VirtualVehicleFactory();
    }

    /**
     * Creates a new factory with a fixed start location.
     *
     * @param location The start location for created vehicles
     * @return A new VirtualVehicleFactory with the specified location
     */
    public static VirtualVehicleFactory factory(Coordinates location) {
        return new VirtualVehicleFactory(location);
    }

    /**
     * Creates a new factory with a location supplier.
     *
     * @param locationSupplier Supplier that provides start locations
     * @return A new VirtualVehicleFactory with the specified supplier
     */
    public static VirtualVehicleFactory factory(Supplier<Coordinates> locationSupplier) {
        return new VirtualVehicleFactory(locationSupplier);
    }

    /**
     * Starts driving to the specified target coordinates.
     * Spawns a new thread that runs until the vehicle arrives.
     *
     * @param target The target coordinates to drive to
     */
    public void drive(Coordinates target) {
        this.targetLocation = target;
        this.isDriving = true;
        new Thread(this).start();
    }

    /**
     * Main simulation loop. Runs until the vehicle arrives at target.
     * Called automatically when thread starts.
     */
    @Override
    public void run() {
        speed = maxSpeed;
        publisher.publish(this);
        long lastPublishTime = System.currentTimeMillis();

        while (isDriving) {
            try {
                Thread.sleep(deltaT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                speed = 0.0;
                isDriving = false;
                return;
            }

            direction = location.calculateBearing(targetLocation);
            location.update(speed, direction, deltaT / 1000.0);

            // Check if arrived
            if (location.calculateDistance(targetLocation) < ARRIVAL_THRESHOLD) {
                isDriving = false;
                targetLocation = null;
            }

            if (System.currentTimeMillis() - lastPublishTime >= publishInterval) {
                publisher.publish(this);
                lastPublishTime = System.currentTimeMillis();
            }
        }

        speed = 0.0;
        publisher.publish(this);
    }

    /**
     * Gets the unique identifier of this vehicle.
     *
     * @return The UUID of this vehicle
     */
    public UUID getId() {
        return id;
    }

    /**
     * Checks if the vehicle is currently driving to a target.
     *
     * @return true if driving, false if stopped or arrived
     */
    public boolean isDriving() {
        return isDriving;
    }

    /**
     * Gets the current location of this vehicle.
     *
     * @return The current location
     */
    public Coordinates getLocation() {
        return location;
    }

    /**
     * Gets the target location of this vehicle.
     *
     * @return The target location, or null if not driving
     */
    public Coordinates getTargetLocation() {
        return targetLocation;
    }

    /**
     * Gets the current direction of this vehicle.
     *
     * @return Direction in radians (0 = North, π/2 = East, etc.)
     */
    public double getDirection() {
        return direction;
    }

    /**
     * Gets the current speed of this vehicle.
     *
     * @return Current speed in km/h (0 when stopped)
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Gets the maximum speed of this vehicle.
     *
     * @return Maximum speed in km/h
     */
    public double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * Returns a string representation of the vehicle's current state.
     *
     * @return String with id, position, target, and speed
     */
    @Override
    public String toString() {
        String targetStr = targetLocation != null ? targetLocation.toString() : "none";
        double distanceToTarget = targetLocation != null
                ? location.calculateDistance(targetLocation)
                : 0.0;

        // 1. changed format to: ... -%skm-> ...
        // 2. Used %03d for speed (3 chars, zero padded)
        return String.format("V2[%s]  (%s) -%skm-> (%s)  %03d km/h",
                id.toString().substring(0, 8),
                location.toString(),
                // Inline: Format to 7.3f, then replace spaces with dashes
                String.format("%7.3f", distanceToTarget / 1000.0).replace(' ', '-'),
                targetStr,
                (int) speed);
    }
}