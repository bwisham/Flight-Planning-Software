import java.util.Scanner;

public class FlightPlanner {
    private final Scanner scanner;

    public FlightPlanner() {
        this.scanner = new Scanner(System.in);
    }

    public void createFlightPlan(AirportDatabase airportDB, AirplaneDatabase airplaneDB) {
        if (airportDB.getAllAirports().isEmpty() || airplaneDB.getAllAirplanes().isEmpty()) {
            System.out.println("Error: Airports or Airplanes database is empty!");
            return;
        }

        System.out.println("\nAvailable Airports:");
        for (Airport airport : airportDB.getAllAirports()) {
            System.out.println("Key: " + airport.getKey() + " | Name: " + airport.getName() + " | ICAO: " + airport.getIcao());
        }

        System.out.print("\nEnter departure airport key: ");
        int departureKey = Integer.parseInt(scanner.nextLine());
        Airport departureAirport = airportDB.getAirport(departureKey);
        if (departureAirport == null) {
            System.out.println("Invalid departure airport key!");
            return;
        }

        System.out.print("Enter destination airport key: ");
        int destinationKey = Integer.parseInt(scanner.nextLine());
        Airport destinationAirport = airportDB.getAirport(destinationKey);
        if (destinationAirport == null) {
            System.out.println("Invalid destination airport key!");
            return;
        }

        System.out.println("\nAvailable Airplanes:");
        for (Airplane airplane : airplaneDB.getAllAirplanes()) {
            System.out.println("Key: " + airplane.getKey() + " | Make: " + airplane.getMake() + " | Model: " + airplane.getModel());
        }

        System.out.print("\nEnter airplane key to use: ");
        int airplaneKey = Integer.parseInt(scanner.nextLine());
        Airplane airplane = airplaneDB.getAirplane(airplaneKey);  // Fixed to match your method name
        if (airplane == null) {
            System.out.println("Invalid airplane key!");
            return;
        }

        double distance = calculateDistance(departureAirport, destinationAirport);
        double fuelNeeded = (distance / airplane.getAirspeed()) * airplane.getFuelBurn();

        System.out.println("\n===== Flight Plan Summary =====");
        System.out.println("Departure: " + departureAirport.getName() + " (" + departureAirport.getIcao() + ")");
        System.out.println("Destination: " + destinationAirport.getName() + " (" + destinationAirport.getIcao() + ")");
        System.out.printf("Distance: %.2f nautical miles\n", distance);
        System.out.printf("Estimated Fuel Needed: %.2f gallons\n", fuelNeeded);
        System.out.printf("Airplane Fuel Capacity: %.2f gallons\n", airplane.getFuelSize());

        if (fuelNeeded > airplane.getFuelSize()) {
            System.out.println("WARNING: This airplane will require refueling during the flight.");
        } else {
            System.out.println("Flight can be completed without refueling.");
        }
        System.out.println("===============================");
    }

    private double calculateDistance(Airport a1, Airport a2) {
        double latDifference = a2.getLatitude() - a1.getLatitude();
        double longDifference = a2.getLongitude() - a1.getLongitude();
        return Math.sqrt(Math.pow(latDifference, 2) + Math.pow(longDifference, 2)) * 60;
    }
}