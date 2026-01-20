package dhbw.trasima;

import java.util.List;
import dhbw.trasima.V2ConsolePublisher;

/**
 * Main application for testing VirtualVehicle simulation.
 */
public class App {

    public static void main(String[] args) {
        // DHBW Mannheim (center for random spawning)
        Coordinates dhbw = Coordinates.fromDegrees(49.4747448220157, 8.534389984805555);

        // SAP Arena
        Coordinates sapArena = Coordinates.fromDegrees(49.46487350889947, 8.518883113896758);

        // Print info
        System.out.println("=== TRASIMA Test ===");
        System.out.println("Start:  Random around DHBW Mannheim");
        System.out.println("Ziel:   SAP Arena " + sapArena);
        System.out.println("Fahrzeuge: 5");
        System.out.println("====================\n");

        // Create 5 vehicles with random positions around DHBW, all driving to SAP Arena
        List<VirtualVehicle> vehicles = VirtualVehicle
                .factory(Coordinates.Suppliers.randomInRadius(dhbw, 0.5))
                .withPublisher(new V2ConsolePublisher())
                .withTargetLocation(sapArena)
                .withMaxSpeed(200.0)
                .createAndDrive(1);

        System.out.println("Created " + vehicles.size() + " vehicles, all driving to SAP Arena.\n");
    }
}