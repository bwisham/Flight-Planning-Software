<<<<<<< HEAD
import java.util.*;

public class FlightPlanner {
    private final Scanner scanner;
   /* public interface AirportDatabase {
        List<Airport> getAllAirports();
        Airport getAirport(int key);
    }
    
    public interface AirplaneDatabase {
        List<Airplane> getAllAirplanes();
        Airplane getAirplane(int key);
    } */
=======
import java.util.Scanner;

public class FlightPlanner {
    private final Scanner scanner;

>>>>>>> 5ae4c7179169afbf730c158719f1cb6267d25625
    public FlightPlanner() {
        this.scanner = new Scanner(System.in);
    }
// might need to add methods to aiport/airplane classes
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
<<<<<<< HEAD
/*    class Airport implements Serializable {
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
    public void setName(String name) { this.name = name; }
    public String getIcao() { return icao; }
    public void setIcao(String icao) { this.icao = icao; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public int getFuelType() { return fuelType; }
    public void setFuelType(int fuelType) { this.fuelType = fuelType; }
    public int getKey() { return key; }
    public String getRadioType() { return radioType; }
    public void setRadioType(String radioType) { this.radioType = radioType; }
    public double getRadioFrequency() { return radioFrequency; }
    public void setRadioFrequency(double radioFrequency) { this.radioFrequency = radioFrequency; }

    @Override
    public String toString() {
        return String.format("Airport [Key: %d, Name: %s, ICAO: %s, Latitude: %.4f, Longitude: %.4f, Fuel Type: %d, Radio Type: %s, Radio Frequency: %.4f]",
                key, name, icao, latitude, longitude, fuelType, radioType, radioFrequency);
    }
    class AirportDatabase {
    private Map<Integer, Airport> airports = new HashMap<>();
    private int nextKey = 1;
    private static final String DATA_FILE = "airports.dat";

    public AirportDatabase() {
        loadAirports();
    }

    private void loadAirports() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                airports = (HashMap<Integer, Airport>) ois.readObject();
                nextKey = airports.keySet().stream().max(Integer::compare).orElse(0) + 1;
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Error loading airport data: " + e.getMessage(), 
                                           "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveAirports() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(airports);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving airport data: " + e.getMessage(), 
                                       "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addAirport(Airport airport) {
        airports.put(airport.getKey(), airport);
        saveAirports();
    }

    public Airport getAirport(int key) {
        return airports.get(key);
    }

    public Airport searchAirport(String searchTerm) {
        return airports.values().stream()
            .filter(a -> a.getName().equalsIgnoreCase(searchTerm) || 
                        a.getIcao().equalsIgnoreCase(searchTerm))
            .findFirst()
            .orElse(null);
    }

    public boolean airportNameExists(String name) {
        return airports.values().stream()
            .anyMatch(a -> a.getName().equalsIgnoreCase(name));
    }

    public void updateAirport(Airport airport) {
        airports.put(airport.getKey(), airport);
        saveAirports();
    }

    public boolean deleteAirport(int key) {
        boolean removed = airports.remove(key) != null;
        if (removed) saveAirports();
        return removed;
    }

    public Collection<Airport> getAllAirports() {
        return airports.values();
    }

    public int getNextKey() {
        return nextKey++;
    }
}
    class Airplane implements Serializable {
        private String make;
        private String model;
        private String aircraftType;
        private double fuelSize;
        private int fuelType; // 1 for Aviation fuel, 2 for Jet fuel
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
    
        // Getters and setters
        public String getMake() { return make; }
        public void setMake(String make) { this.make = make; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getAircraftType() { return aircraftType; }
        public void setAircraftType(String aircraftType) { this.aircraftType = aircraftType; }
        public double getFuelSize() { return fuelSize; }
        public void setFuelSize(double fuelSize) { this.fuelSize = fuelSize; }
        public int getFuelType() { return fuelType; }
        public void setFuelType(int fuelType) { this.fuelType = fuelType; }
        public double getFuelBurn() { return fuelBurn; }
        public void setFuelBurn(double fuelBurn) { this.fuelBurn = fuelBurn; }
        public int getAirspeed() { return airspeed; }
        public void setAirspeed(int airspeed) { this.airspeed = airspeed; }
        public int getKey() { return key; }
     
        @Override
        public String toString() {
            String fuelTypeString = (fuelType == 1) ? "Aviation fuel" : "Jet fuel";
            return String.format(
                "Airplane [Key: %d, Make: %s, Model: %s, Type: %s\n" +
                "Fuel: %s (%.1f liters), Burn Rate: %.1f liters/hr, Cruising Speed: %d knots]",
                key, make, model, aircraftType, fuelTypeString, fuelSize, fuelBurn, airspeed
            );
        }
    }
    class AirplaneDatabase {
    private final Map<Integer, Airplane> airplanes = new HashMap<>();
    private int nextKey = 1;

    public void addAirplane(Airplane airplane) {
        airplanes.put(airplane.getKey(), airplane);
    }

    public Airplane getAirplane(int key) {
        return airplanes.get(key);
    }

    public Airplane searchAirplane(String searchTerm, int searchType) {
        for (Airplane airplane : airplanes.values()) {
            switch (searchType) {
                case 0 -> {
                    // By Make
                    if (airplane.getMake().equalsIgnoreCase(searchTerm)) {
                        return airplane;
                    }
                }
                case 1 -> {
                    // By Model
                    if (airplane.getModel().equalsIgnoreCase(searchTerm)) {
                        return airplane;
                    }
                }
                case 2 -> {
                    // By Type
                    if (airplane.getAircraftType().equalsIgnoreCase(searchTerm)) {
                        return airplane;
                    }
                }
            }
        }
        return null;
    }

    public void updateAirplane(Airplane airplane) {
        airplanes.put(airplane.getKey(), airplane);
    }

    public boolean deleteAirplane(int key) {
        return airplanes.remove(key) != null;
    }

    public Collection<Airplane> getAllAirplanes() {
        return airplanes.values();
    }

    public int getNextKey() {
        return nextKey++;
    }
    
    public void setNextKey(int key) {
        this.nextKey = key;
    }
}    
}
    /* class Airport {
        private int key;
        private String name;
        private String icao;
        private double latitude;
        private double longitude;
    
        public Airport(int key, String name, String icao, double latitude, double longitude) {
            this.key = key;
            this.name = name;
            this.icao = icao;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    
        // Getters
        public int getKey() { return key; }
        public String getName() { return name; }
        public String getIcao() { return icao; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
    class Airplane {
        private int key;
        private String make;
        private String model;
        private double airspeed;
        private double fuelBurn;
        private double fuelSize;
    
        public Airplane(int key, String make, String model, double airspeed, double fuelBurn, double fuelSize) {
            this.key = key;
            this.make = make;
            this.model = model;
            this.airspeed = airspeed;
            this.fuelBurn = fuelBurn;
            this.fuelSize = fuelSize;
        }
    
        // Getters
        public int getKey() { return key; }
        public String getMake() { return make; }
        public String getModel() { return model; }
        public double getAirspeed() { return airspeed; }
        public double getFuelBurn() { return fuelBurn; }
        public double getFuelSize() { return fuelSize; } 
    } */
}
=======
}
>>>>>>> 5ae4c7179169afbf730c158719f1cb6267d25625
