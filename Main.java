import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Load databases from files
        Map<Integer, Airport> airports = loadAirportsFromFile("airports.dat");
        Map<Integer, Airplane> airplanes = loadAirplanesFromFile("airplanes.dat");
       
        if (airports == null || airplanes == null || airports.isEmpty() || airplanes.isEmpty()) {
            System.out.println("Error: Failed to load databases or databases are empty!");
            System.out.println("Airports loaded: " + (airports != null ? airports.size() : 0));
            System.out.println("Airplanes loaded: " + (airplanes != null ? airplanes.size() : 0));
            return;
        }
       
        FlightPlanner flightPlanner = new FlightPlanner();
        flightPlanner.createFlightPlan(airports, airplanes);
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Airport> loadAirportsFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Airport database file not found: " + filename);
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                return (Map<Integer, Airport>) obj;
            } else {
                System.out.println("Unexpected object type in airport file: " + obj.getClass());
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading airport database from " + filename + ": " + e.getMessage());
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Airplane> loadAirplanesFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Airplane database file not found: " + filename);
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            
            if (obj instanceof ArrayList) {
                // Handle AirplaneManager's ArrayList format
                ArrayList<Airplane> airplaneList = (ArrayList<Airplane>) obj;
                Map<Integer, Airplane> airplaneMap = new HashMap<>();
                for (Airplane airplane : airplaneList) {
                    airplaneMap.put(airplane.getKey(), airplane);
                }
                return airplaneMap;
            } else if (obj instanceof Map) {
                // Handle direct Map format
                return (Map<Integer, Airplane>) obj;
            } else {
                System.out.println("Unexpected object type in airplane file: " + obj.getClass());
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading airplane database from " + filename + ": " + e.getMessage());
            return new HashMap<>();
        }
    }
}

class FlightPlanner {
    private final Scanner scanner;

    public FlightPlanner() {
        this.scanner = new Scanner(System.in);
    }
   
    public void createFlightPlan(Map<Integer, Airport> airports, Map<Integer, Airplane> airplanes) {
        if (airports.isEmpty() || airplanes.isEmpty()) {
            System.out.println("Error: Airports or Airplanes database is empty!");
            System.out.println("Airports available: " + airports.size());
            System.out.println("Airplanes available: " + airplanes.size());
            return;
        }

        System.out.println("\nAvailable Airports:");
        for (Airport airport : airports.values()) {
            System.out.println("Key: " + airport.getKey() + " | Name: " + airport.getName() + 
                           " | ICAO: " + airport.getIcao() + " | Coordinates: " + 
                           airport.getLatitude() + ", " + airport.getLongitude());
        }

        System.out.print("\nEnter departure airport key: ");
        int departureKey = Integer.parseInt(scanner.nextLine());
        Airport departureAirport = airports.get(departureKey);
        if (departureAirport == null) {
            System.out.println("Invalid departure airport key!");
            return;
        }

        System.out.print("Enter destination airport key: ");
        int destinationKey = Integer.parseInt(scanner.nextLine());
        Airport destinationAirport = airports.get(destinationKey);
        if (destinationAirport == null) {
            System.out.println("Invalid destination airport key!");
            return;
        }

        System.out.println("\nAvailable Airplanes:");
        for (Airplane airplane : airplanes.values()) {
            System.out.println("Key: " + airplane.getKey() + " | Make: " + airplane.getMake() + 
                           " | Model: " + airplane.getModel() + " | Type: " + airplane.getAircraftType() +
                           " | Fuel: " + airplane.getFuelSize() + " liters (" + 
                           (airplane.getFuelType() == 1 ? "AVGAS" : "Jet") + ") | " +
                           "Burn: " + airplane.getFuelBurn() + " l/hr | Speed: " + 
                           airplane.getAirspeed() + " knots");
        }

        System.out.print("\nEnter airplane key to use: ");
        int airplaneKey = Integer.parseInt(scanner.nextLine());
        Airplane airplane = airplanes.get(airplaneKey);
        if (airplane == null) {
            System.out.println("Invalid airplane key!");
            return;
        }

        double distance = calculateDistance(departureAirport, destinationAirport);
        double flightTime = distance / airplane.getAirspeed();
        double fuelNeeded = flightTime * airplane.getFuelBurn();

        System.out.println("\n===== Flight Plan Summary =====");
        System.out.println("Departure: " + departureAirport.getName() + " (" + departureAirport.getIcao() + ")");
        System.out.println("Destination: " + destinationAirport.getName() + " (" + destinationAirport.getIcao() + ")");
        System.out.printf("Distance: %.2f nautical miles\n", distance);
        System.out.printf("Estimated Flight Time: %.2f hours\n", flightTime);
        System.out.printf("Estimated Fuel Needed: %.2f liters\n", fuelNeeded);
        System.out.printf("Airplane Fuel Capacity: %.2f liters\n", airplane.getFuelSize());

        if (fuelNeeded > airplane.getFuelSize()) {
            System.out.println("WARNING: This airplane will require refueling during the flight.");
            System.out.printf("Additional fuel needed: %.2f liters\n", fuelNeeded - airplane.getFuelSize());
            
            // Check if destination airport has the required fuel type
            if ((airplane.getFuelType() == 1 && destinationAirport.getFuelType() != 1 && destinationAirport.getFuelType() != 3) ||
                (airplane.getFuelType() == 2 && destinationAirport.getFuelType() != 2 && destinationAirport.getFuelType() != 3)) {
                System.out.println("WARNING: Destination airport doesn't have the required fuel type!");
            }
        } else {
            System.out.println("Flight can be completed without refueling.");
            System.out.printf("Remaining fuel after flight: %.2f liters\n", airplane.getFuelSize() - fuelNeeded);
        }
        System.out.println("===============================");
    }

    private double calculateDistance(Airport a1, Airport a2) {
        // Using Haversine formula for accurate distance calculation
        double lat1 = Math.toRadians(a1.getLatitude());
        double lon1 = Math.toRadians(a1.getLongitude());
        double lat2 = Math.toRadians(a2.getLatitude());
        double lon2 = Math.toRadians(a2.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2) + 
                   Math.cos(lat1) * Math.cos(lat2) * 
                   Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Earth radius in nautical miles (3440.1 NM)
        return 3440.1 * c;
    }
}

class Airport implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String icao;
    private double latitude;
    private double longitude;
    private int fuelType;
    private final int key;
    private String radioType;
    private double radioFrequency;

    public Airport(String name, String icao, double latitude, double longitude, 
                  int fuelType, int key, String radioType, double radioFrequency) {
        this.name = name;
        this.icao = icao;
        this.latitude = latitude;
        this.longitude = longitude;
        this.fuelType = fuelType;
        this.key = key;
        this.radioType = radioType;
        this.radioFrequency = radioFrequency;
    }

    public String getName() { return name; }
    public String getIcao() { return icao; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getFuelType() { return fuelType; }
    public int getKey() { return key; }
    public String getRadioType() { return radioType; }
    public double getRadioFrequency() { return radioFrequency; }
}

class Airplane implements Serializable {
    private static final long serialVersionUID = -7817292208027958121L; // Matches AirplaneManager's version
    private String make;
    private String model;
    private String aircraftType;
    private double fuelSize;
    private int fuelType; // 1 = AVGAS, 2 = Jet
    private double fuelBurn;
    private int airspeed;
    private final int key;

    public Airplane(String make, String model, String aircraftType, double fuelSize,
                   int fuelType, double fuelBurn, int airspeed, int key) {
        this.make = make;
        this.model = model;
        this.aircraftType = aircraftType;
        this.fuelSize = fuelSize;
        this.fuelType = fuelType;
        this.fuelBurn = fuelBurn;
        this.airspeed = airspeed;
        this.key = key;
    }

    public String getMake() { return make; }
    public String getModel() { return model; }
    public String getAircraftType() { return aircraftType; }
    public double getFuelSize() { return fuelSize; }
    public int getFuelType() { return fuelType; }
    public double getFuelBurn() { return fuelBurn; }
    public int getAirspeed() { return airspeed; }
    public int getKey() { return key; }
}