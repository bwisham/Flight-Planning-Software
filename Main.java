import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Load databases from files
        AirportDatabase airportDB = AirportDatabase.loadFromFile("airports.dat");
        AirplaneDatabase airplaneDB = AirplaneDatabase.loadFromFile("airplanes.dat");
       
        if (airportDB == null || airplaneDB == null) {
            System.out.println("Failed to load databases. Exiting...");
            return;
        }
       
        FlightPlanner flightPlanner = new FlightPlanner();
        flightPlanner.createFlightPlan(airportDB, airplaneDB);
    }
}

class FlightPlanner {
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
        Airplane airplane = airplaneDB.getAirplane(airplaneKey);
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

class AirportDatabase implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Airport> airports;

    public AirportDatabase() {
        this.airports = new ArrayList<>();
    }

    public void addAirport(Airport airport) {
        airports.add(airport);
    }

    public Airport getAirport(int key) {
        return airports.stream().filter(a -> a.getKey() == key).findFirst().orElse(null);
    }

    public List<Airport> getAllAirports() {
        return new ArrayList<>(airports);
    }

    public static AirportDatabase loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Database file not found: " + filename);
            return new AirportDatabase();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (AirportDatabase) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading database from " + filename + ": " + e.getMessage());
            return new AirportDatabase();
        }
    }

    public void saveToFile(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error saving database to " + filename + ": " + e.getMessage());
        }
    }
}

class AirplaneDatabase implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<Airplane> airplanes;

    public AirplaneDatabase() {
        this.airplanes = new ArrayList<>();
    }

    public void addAirplane(Airplane airplane) {
        airplanes.add(airplane);
    }

    public Airplane getAirplane(int key) {
        return airplanes.stream().filter(a -> a.getKey() == key).findFirst().orElse(null);
    }

    public List<Airplane> getAllAirplanes() {
        return new ArrayList<>(airplanes);
    }

    public static AirplaneDatabase loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Database file not found: " + filename);
            return new AirplaneDatabase();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return (AirplaneDatabase) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading database from " + filename + ": " + e.getMessage());
            return new AirplaneDatabase();
        }
    }

    public void saveToFile(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error saving database to " + filename + ": " + e.getMessage());
        }
    }
}

class Airport implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int key;
    private final String name;
    private final String icao;
    private final double latitude;
    private final double longitude;

    public Airport(int key, String name, String icao, double latitude, double longitude) {
        this.key = key;
        this.name = name;
        this.icao = icao;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getKey() { return key; }
    public String getName() { return name; }
    public String getIcao() { return icao; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}

class Airplane implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int key;
    private final String make;
    private final String model;
    private final double airspeed;
    private final double fuelBurn;
    private final double fuelSize;

    public Airplane(int key, String make, String model, double airspeed, double fuelBurn, double fuelSize) {
        this.key = key;
        this.make = make;
        this.model = model;
        this.airspeed = airspeed;
        this.fuelBurn = fuelBurn;
        this.fuelSize = fuelSize;
    }

    public int getKey() { return key; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public double getAirspeed() { return airspeed; }
    public double getFuelBurn() { return fuelBurn; }
    public double getFuelSize() { return fuelSize; }
}