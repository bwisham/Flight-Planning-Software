import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Airplane implements Serializable {
    private static final long serialVersionUID = 1L;
    private String make;
    private String model;
    private String aircraftType;
    private double fuelSize;
    private String fuel;
    private double fuelBurn;
    private int airspeed;
    private final Scanner scanner;
    private boolean dataSet = false;
    private static final String DB_FILE = "airplanedb.dat";
    private static AirplaneDatabase airplaneDB;

    public Airplane() {
        this.scanner = new Scanner(System.in);
        this.make = "";
        this.model = "";
        this.aircraftType = "";
        this.fuelSize = 0.0;
        this.fuel = "";
        this.fuelBurn = 0.0;
        this.airspeed = 0;
        Airplane.airplaneDB = loadDatabase();
    }

    private AirplaneDatabase loadDatabase() {
        File dbFile = new File(DB_FILE);
        System.out.println("Database file location: " + dbFile.getAbsolutePath());
        
        if (!dbFile.exists()) {
            System.out.println("No database found. Creating new database file...");
            return new AirplaneDatabase();
        }
        
        if (dbFile.length() == 0) {
            System.out.println("Empty database file detected. Creating new database...");
            return new AirplaneDatabase();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DB_FILE))) {
            return (AirplaneDatabase) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading database: " + e.getMessage());
            System.out.println("Creating new database as recovery...");
            return new AirplaneDatabase();
        }
    }

    private void saveDatabase() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DB_FILE))) {
            oos.writeObject(airplaneDB);
            System.out.println("Database saved successfully");
        } catch (IOException e) {
            System.out.println("Error saving database: " + e.getMessage());
        }
    }

    public void setMake() {
        System.out.print("Enter airplane make: ");
        this.make = scanner.nextLine();
    }

    public void setModel() {
        System.out.print("Enter airplane model: ");
        this.model = scanner.nextLine();
    }

    public void setAircraftType() {
        while (true) {
            System.out.print("Enter aircraft type (prop/turboprop/jet): ");
            String input = scanner.nextLine().toLowerCase();
            if (input.equals("prop") || input.equals("turboprop") || input.equals("jet")) {
                this.aircraftType = input;
                break;
            }
            System.out.println("Invalid type! Must be prop, turboprop, or jet.");
        }
    }

    public void setFuelSize() {
        System.out.print("Enter fuel size (in gallons): ");
        while (!scanner.hasNextDouble()) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.next();
        }
        this.fuelSize = scanner.nextDouble();
        scanner.nextLine();
    }

    public void setFuel() {
        System.out.print("Enter fuel type: ");
        this.fuel = scanner.nextLine();
    }

    public void setFuelBurn() {
        System.out.print("Enter fuel burn rate (gallons/hour): ");
        while (!scanner.hasNextDouble()) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.next();
        }
        this.fuelBurn = scanner.nextDouble();
        scanner.nextLine();
    }

    public void setAirSpeed() {
        System.out.print("Enter airspeed (knots): ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input! Please enter a whole number.");
            scanner.next();
        }
        this.airspeed = scanner.nextInt();
        scanner.nextLine();
    }

    public void print() {
        if (!dataSet) {
            System.out.println("\nERROR: No airplane data has been set yet!");
            System.out.println("Please use option 1 to set airplane information first.");
            System.out.print("Press enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nCurrent Airplane Information:");
        System.out.println("Make: " + make);
        System.out.println("Model: " + model);
        System.out.println("Aircraft Type: " + aircraftType);
        System.out.println("Fuel Size: " + fuelSize + " gallons");
        System.out.println("Fuel Type: " + fuel);
        System.out.println("Fuel Burn Rate: " + fuelBurn + " gallons/hour");
        System.out.println("Airspeed: " + airspeed + " knots");
        
        System.out.print("\nPress enter to continue...");
        scanner.nextLine();
    }

    public void modify() {
        if (!dataSet) {
            System.out.println("\nERROR: Cannot modify - no airplane data has been set yet!");
            System.out.print("Press enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nCurrent Airplane Information:");
        print();
        
        System.out.println("\nEnter new values (leave blank to keep current):");
        
        System.out.print("Make (" + make + "): ");
        String makeInput = scanner.nextLine();
        if (!makeInput.isEmpty()) make = makeInput;
        
        System.out.print("Model (" + model + "): ");
        String modelInput = scanner.nextLine();
        if (!modelInput.isEmpty()) model = modelInput;
        
        System.out.print("Aircraft Type (" + aircraftType + "): ");
        String typeInput = scanner.nextLine().toLowerCase();
        if (!typeInput.isEmpty()) {
            while (!typeInput.equals("prop") && !typeInput.equals("turboprop") && !typeInput.equals("jet")) {
                System.out.println("Invalid type! Must be prop, turboprop, or jet.");
                System.out.print("Aircraft Type (" + aircraftType + "): ");
                typeInput = scanner.nextLine().toLowerCase();
                if (typeInput.isEmpty()) break;
            }
            if (!typeInput.isEmpty()) aircraftType = typeInput;
        }
        
        System.out.print("Fuel Size (" + fuelSize + "): ");
        String sizeInput = scanner.nextLine();
        if (!sizeInput.isEmpty()) fuelSize = Double.parseDouble(sizeInput);
        
        System.out.print("Fuel Type (" + fuel + "): ");
        String fuelInput = scanner.nextLine();
        if (!fuelInput.isEmpty()) fuel = fuelInput;
        
        System.out.print("Fuel Burn Rate (" + fuelBurn + "): ");
        String burnInput = scanner.nextLine();
        if (!burnInput.isEmpty()) fuelBurn = Double.parseDouble(burnInput);
        
        System.out.print("Airspeed (" + airspeed + "): ");
        String speedInput = scanner.nextLine();
        if (!speedInput.isEmpty()) airspeed = Integer.parseInt(speedInput);
        
        System.out.println("\nAirplane information updated successfully!");
        System.out.print("Press enter to continue...");
        scanner.nextLine();
    }

    public boolean isValid() {
        if (!dataSet) {
            System.out.println("\nERROR: No airplane data has been set yet!");
            System.out.println("Please use option 1 to set airplane information first.");
            System.out.print("Press enter to continue...");
            scanner.nextLine();
            return false;
        }
        
        if (make.isEmpty() || model.isEmpty() || aircraftType.isEmpty() || fuel.isEmpty()) {
            System.out.println("ERROR: Missing required fields!");
            System.out.print("Press enter to continue...");
            scanner.nextLine();
            return false;
        }
        if (fuelSize <= 0 || fuelBurn <= 0 || airspeed <= 0) {
            System.out.println("ERROR: Numerical values must be positive!");
            System.out.print("Press enter to continue...");
            scanner.nextLine();
            return false;
        }
        if (!aircraftType.equals("prop") && !aircraftType.equals("turboprop") && !aircraftType.equals("jet")) {
            System.out.println("ERROR: Invalid aircraft type!");
            System.out.print("Press enter to continue...");
            scanner.nextLine();
            return false;
        }
        System.out.println("All required fields are complete and valid!");
        System.out.print("Press enter to continue...");
        scanner.nextLine();
        return true;
    }

    public void saveToDatabase() {
        if (!dataSet) {
            System.out.println("\nERROR: Cannot save - no airplane data has been set yet!");
            System.out.print("Press enter to continue...");
            scanner.nextLine();
            return;
        }
        
        airplaneDB.addAirplane(this);
        saveDatabase();
        System.out.println("\nAirplane saved to database successfully!");
        System.out.print("Press enter to continue...");
        scanner.nextLine();
    }

    public void listAllAirplanes() {
        if (airplaneDB.getAllAirplanes().isEmpty()) {
            System.out.println("\nNo airplanes in database!");
            System.out.print("Press enter to continue...");
            scanner.nextLine();
            return;
        }
        
        System.out.println("\nAll Airplanes in Database:");
        System.out.printf("%-15s %-15s %-10s %-10s %-10s %-10s %-10s%n",
                        "Make", "Model", "Type", "Fuel", "Size", "Burn", "Speed");
        System.out.println("----------------------------------------------------------------");
        
        for (Airplane plane : airplaneDB.getAllAirplanes()) {
            System.out.printf("%-15s %-15s %-10s %-10s %-10.1f %-10.1f %-10d%n",
                            plane.make, plane.model, plane.aircraftType, plane.fuel,
                            plane.fuelSize, plane.fuelBurn, plane.airspeed);
        }
        
        System.out.print("\nPress enter to continue...");
        scanner.nextLine();
    }

    public static void main(String[] args) {
        Airplane airplane = new Airplane();
        Scanner menuScanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\nAirplane Management System");
            System.out.println("1. Set Airplane Information");
            System.out.println("2. View Current Airplane");
            System.out.println("3. Modify Current Airplane");
            System.out.println("4. Validate Information");
            System.out.println("5. Save to Database");
            System.out.println("6. List All Airplanes");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");
            
            try {
                int choice = Integer.parseInt(menuScanner.nextLine());
                
                switch (choice) {
                    case 1 -> {
                        airplane.setMake();
                        airplane.setModel();
                        airplane.setAircraftType();
                        airplane.setFuelSize();
                        airplane.setFuel();
                        airplane.setFuelBurn();
                        airplane.setAirSpeed();
                        airplane.dataSet = true;
                    }
                    case 2 -> airplane.print();
                    case 3 -> airplane.modify();
                    case 4 -> airplane.isValid();
                    case 5 -> airplane.saveToDatabase();
                    case 6 -> airplane.listAllAirplanes();
                    case 7 -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number between 1-7.");
            }
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
    
    public List<Airplane> getAllAirplanes() {
        return new ArrayList<>(airplanes);
    }
}