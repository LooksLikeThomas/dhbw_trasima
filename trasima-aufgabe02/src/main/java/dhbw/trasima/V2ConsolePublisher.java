package dhbw.trasima;

/**
 * Console publisher implementation for VirtualVehicle (V2).
 * Outputs vehicle data to System.out (stdout).
 */
public class V2ConsolePublisher implements IPublisher<VirtualVehicle> {

    /**
     * Publishes vehicle data to the console.
     *
     * @param vehicle The VirtualVehicle to publish
     */
    @Override
    public void publish(VirtualVehicle vehicle) {
        System.out.println(vehicle.toString());
    }
}