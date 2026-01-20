package dhbw.trasima;

import java.util.Objects;

/**
 * Represents geographic coordinates (latitude/longitude) and handles position updates.
 * Implements physics calculations using Euler approximation from TRASIMA slide 44.
 *
 * NOTE: All internal storage and angular inputs are in RADIANS.
 *
 * Coordinate system (Radians):
 * - Latitude: -π/2 (-90°) to +π/2 (+90°)
 * - Longitude: -π (-180°) to +π (+180°)
 *
 * Direction convention (Radians):
 * - 0 (0°)       = North
 * - π/2 (90°)    = East
 * - π (180°)     = South
 * - 3π/2 (270°)  = West
 */
public class Coordinates {

    /** Earth's mean radius in kilometers (WGS84 standard) */
    private static final double EARTH_RADIUS_KM = 6370.0;

    /** Threshold for detecting pole proximity to avoid division by zero */
    private static final double POLE_THRESHOLD = 0.0001;

    /** Tolerance for floating-point comparisons */
    private static final double EPSILON = 1e-10;

    private double latitude;  // stored in Radians
    private double longitude; // stored in Radians

    public static class Suppliers extends CoordinateSuppliers {}

    /**
     * Creates a new Coordinates object with the given position.
     *
     * @param latRad  Initial latitude in Radians
     * @param lonRad  Initial longitude in Radians
     */
    public Coordinates(double latRad, double lonRad) {
        this.setLatitude(latRad);
        this.setLongitude(lonRad);
    }

    /**
     * Factory method to create Coordinates from degrees (for convenience).
     * Automatically converts degrees to radians for internal storage.
     *
     * @param latDeg  Initial latitude in degrees (-90 to +90)
     * @param lonDeg  Initial longitude in degrees (-180 to +180)
     * @return New Coordinates object
     */
    public static Coordinates fromDegrees(double latDeg, double lonDeg) {
        return new Coordinates(Math.toRadians(latDeg), Math.toRadians(lonDeg));
    }

    /**
     * Calculates the distance between two Coordinates using Haversine formula.
     * More accurate than simple Euclidean distance for spherical coordinates.
     *
     * @param a Coordinate A
     * @param b Coordinate B
     * @return Distance between A and B in Meters
     * @throws IllegalArgumentException if either coordinate is null
     */
    public static double calculateDistance(Coordinates a, Coordinates b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }

        // Haversine formula
        double dLat = b.latitude - a.latitude;
        double dLon = b.longitude - a.longitude;

        double haversine = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(a.latitude) * Math.cos(b.latitude) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));

        // Distance in kilometers, convert to meters
        return EARTH_RADIUS_KM * c * 1000.0;
    }

    /**
     * Calculates the distance between this Coordinate and a target Coordinate using Haversine formula.
     *
     * @param target Target Coordinate
     * @return Distance to Target in Meters
     * @throws IllegalArgumentException if target is null
     */
    public double calculateDistance(Coordinates target) {
        return calculateDistance(this, target);
    }

    /**
     * Calculates the initial bearing from this coordinate to the target coordinate.
     *
     * @param target Target Coordinate
     * @return Bearing in Radians (0 = North, π/2 = East, etc.)
     * @throws IllegalArgumentException if target is null
     */
    public double calculateBearing(Coordinates target) {
        if (target == null) {
            throw new IllegalArgumentException("Target coordinate cannot be null");
        }

        double dLon = target.longitude - this.longitude;

        double x = Math.sin(dLon) * Math.cos(target.latitude);
        double y = Math.cos(this.latitude) * Math.sin(target.latitude) -
                Math.sin(this.latitude) * Math.cos(target.latitude) * Math.cos(dLon);

        double bearing = Math.atan2(x, y);

        // Normalize to [0, 2π)
        return (bearing + 2 * Math.PI) % (2 * Math.PI);
    }

    /**
     * Updates the position based on speed, direction, and time interval.
     * Uses Euler approximation with formulas from TRASIMA slide 44 (adapted for Radians):
     *
     * Δlat  ≈ (v / R) · cos(θ) · (Δt/3600)
     * Δlong ≈ (v / (R · cos(lat))) · sin(θ) · (Δt/3600)
     *
     * @param speedKmH    Speed in km/h (must be non-negative)
     * @param directionRad Direction in Radians (0 = North, π/2 = East, etc.)
     * @param deltaTSec   Time interval in seconds (must be non-negative)
     * @throws IllegalArgumentException if speed or time is negative, or if values are NaN/Infinite
     */
    public void update(double speedKmH, double directionRad, double deltaTSec) {
        validateUpdateParameters(speedKmH, directionRad, deltaTSec);

        if (speedKmH == 0 || deltaTSec == 0) {
            return; // No movement
        }

        double deltaLat = calculateDeltaLatitude(speedKmH, directionRad, deltaTSec);
        double deltaLon = calculateDeltaLongitude(speedKmH, directionRad, deltaTSec);

        this.setLatitude(this.latitude + deltaLat);
        this.setLongitude(this.longitude + deltaLon);
    }

    /**
     * Gets the current latitude.
     *
     * @return Latitude in Radians
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * Gets the current latitude in degrees (for convenience).
     *
     * @return Latitude in Degrees
     */
    public double getLatitudeDeg() {
        return Math.toDegrees(this.latitude);
    }

    /**
     * Sets the current latitude.
     * Validates and clamps input to range [-π/2, +π/2].
     *
     * @param latRad Latitude in Radians
     * @throws IllegalArgumentException if latitude is NaN or Infinite
     */
    public void setLatitude(double latRad) {
        if (Double.isNaN(latRad) || Double.isInfinite(latRad)) {
            throw new IllegalArgumentException("Latitude cannot be NaN or Infinite");
        }

        // Clamp latitude: cannot go beyond North/South pole
        if (latRad > Math.PI / 2) {
            this.latitude = Math.PI / 2;
        } else if (latRad < -Math.PI / 2) {
            this.latitude = -Math.PI / 2;
        } else {
            this.latitude = latRad;
        }
    }

    /**
     * Gets the current longitude.
     *
     * @return Longitude in Radians
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * Gets the current longitude in degrees (for convenience).
     *
     * @return Longitude in Degrees
     */
    public double getLongitudeDeg() {
        return Math.toDegrees(this.longitude);
    }

    /**
     * Sets the current longitude.
     * Automatically normalizes input to range [-π, +π].
     * Example: 190° becomes -170°.
     *
     * @param lonRad Longitude in Radians
     * @throws IllegalArgumentException if longitude is NaN or Infinite
     */
    public void setLongitude(double lonRad) {
        if (Double.isNaN(lonRad) || Double.isInfinite(lonRad)) {
            throw new IllegalArgumentException("Longitude cannot be NaN or Infinite");
        }
        this.longitude = normalizeAngle(lonRad);
    }

    /**
     * Returns a string representation of the coordinates.
     * Converts internal Radians back to Degrees for readability.
     *
     * @return String in format "Lat: XX.XXXX°, Lon: XX.XXXX°"
     */
    @Override
    public String toString() {
        return String.format("Lat: %.4f°, Lon: %.4f°", getLatitudeDeg(), getLongitudeDeg());
    }

    /**
     * Checks equality based on latitude and longitude values.
     *
     * @param obj Object to compare
     * @return true if coordinates are equal within floating-point tolerance
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Coordinates other = (Coordinates) obj;
        return Math.abs(this.latitude - other.latitude) < EPSILON &&
                Math.abs(this.longitude - other.longitude) < EPSILON;
    }

    /**
     * Generates hash code based on latitude and longitude.
     *
     * @return Hash code
     */
    @Override
    public int hashCode() {
        // Round to avoid hash inconsistency due to floating-point precision
        long latBits = Double.doubleToLongBits(Math.round(latitude * 1e8) / 1e8);
        long lonBits = Double.doubleToLongBits(Math.round(longitude * 1e8) / 1e8);
        return Objects.hash(latBits, lonBits);
    }

    /**
     * Creates a copy of this Coordinates object.
     *
     * @return New Coordinates object with same latitude and longitude
     */
    public Coordinates copy() {
        return new Coordinates(this.latitude, this.longitude);
    }

    /**
     * Validates parameters for the update method.
     *
     * @param speedKmH     Speed in km/h
     * @param directionRad Direction in Radians
     * @param deltaTSec    Time interval in seconds
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validateUpdateParameters(double speedKmH, double directionRad, double deltaTSec) {
        if (Double.isNaN(speedKmH) || Double.isInfinite(speedKmH)) {
            throw new IllegalArgumentException("Speed cannot be NaN or Infinite");
        }
        if (Double.isNaN(directionRad) || Double.isInfinite(directionRad)) {
            throw new IllegalArgumentException("Direction cannot be NaN or Infinite");
        }
        if (Double.isNaN(deltaTSec) || Double.isInfinite(deltaTSec)) {
            throw new IllegalArgumentException("Time delta cannot be NaN or Infinite");
        }
        if (speedKmH < 0) {
            throw new IllegalArgumentException("Speed cannot be negative: " + speedKmH);
        }
        if (deltaTSec < 0) {
            throw new IllegalArgumentException("Time delta cannot be negative: " + deltaTSec);
        }
    }

    /**
     * Calculates the change in latitude based on movement.
     * Formula: Δlat ≈ (v / R) · cos(θ) · (Δt/3600)
     *
     * @param speedKmH     Speed in km/h
     * @param directionRad Direction in Radians
     * @param deltaTSec    Time interval in seconds
     * @return Change in latitude in Radians
     */
    private double calculateDeltaLatitude(double speedKmH, double directionRad, double deltaTSec) {
        // v_N = v * cos(θ) - velocity component in North direction
        double vNorth = speedKmH * Math.cos(directionRad);

        // Δlat = (v_N / R) * (Δt / 3600)
        // Δt/3600 converts seconds to hours (to match km/h)
        return (vNorth / EARTH_RADIUS_KM) * (deltaTSec / 3600.0);
    }

    /**
     * Calculates the change in longitude based on movement.
     * Formula: Δlong ≈ (v / (R · cos(lat))) · sin(θ) · (Δt/3600)
     *
     * @param speedKmH     Speed in km/h
     * @param directionRad Direction in Radians
     * @param deltaTSec    Time interval in seconds
     * @return Change in longitude in Radians
     */
    private double calculateDeltaLongitude(double speedKmH, double directionRad, double deltaTSec) {
        // v_E = v * sin(θ) - velocity component in East direction
        double vEast = speedKmH * Math.sin(directionRad);

        // cos(latitude) accounts for Earth's curvature - at poles, longitude changes much faster
        double cosLat = Math.cos(this.latitude);

        // Avoid division by zero at poles
        if (Math.abs(cosLat) < POLE_THRESHOLD) {
            return 0.0; // At poles, longitude change is undefined
        }

        // Δlong = (v_E / (R * cos(lat))) * (Δt / 3600)
        return (vEast / (EARTH_RADIUS_KM * cosLat)) * (deltaTSec / 3600.0);
    }

    /**
     * Helper method to normalize an angle to the range [-π, π].
     * Uses Math.IEEEremainder for better precision with large angles.
     *
     * @param angleRad Angle in Radians
     * @return Normalized angle in Radians
     */
    private double normalizeAngle(double angleRad) {
        return Math.IEEEremainder(angleRad, 2 * Math.PI);
    }
}