import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Airplane implements Serializable {
    // Serialization version ID for compatibility
    public static final long serialVersionUID = 1L;
    
    // Airplane attributes
    public String make;             // Manufacturer of the airplane
    public String model;            // Model name/number
    public String aircraftType;     // Type: prop, turboprop, or jet
    public double fuelSize;         // Total fuel capacity in liters
    public double fuelBurn;         // Fuel consumption rate in liters/hour
    public int airspeed;            // Cruising speed in knots
    
    // Transient fields (not serialized)
    public transient Scanner scanner;  // Scanner for user input
    public boolean dataSet = false;    // Flag to track if data has been entered
    
    // Database configuration
    public static String DB_FILE = "Airplanedb1.dat";       // Database filename
    public static AirplaneDatabase airplaneDB = loadDatabase();  // Database instance

    // Default constructor
    public Airplane() {
        this.scanner = new Scanner(System.in);
        resetFields();
    }

    // Resets all fields to default values
    public void resetFields() {
        this.make = "";
        this.model = "";
        this.aircraftType = "";
        this.fuelSize = 0.0;
        this.fuelBurn = 0.0;
        this.airspeed = 0;
        this.dataSet = false;
    }

    // Custom deserialization to reinitialize transient fields
    public void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.scanner = new Scanner(System.in);
    }

    // Loads airplane database from file or creates new if none exists
    public static AirplaneDatabase loadDatabase() {
        File dbFile = new File(DB_FILE);
        System.out.println("Database file location: " + dbFile.getAbsolutePath());
        
        if (!dbFile.exists() || dbFile.length() == 0) {
            System.out.println("No database found. Creating new database.");
            return new AirplaneDatabase();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DB_FILE))) {
            return (AirplaneDatabase) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading database. Creating new one.");
            return new AirplaneDatabase();
        }
    }

    // Saves current database to file
    public static void saveDatabase() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DB_FILE))) {
            oos.writeObject(airplaneDB);
            System.out.println("Database saved successfully to " + DB_FILE);
        } catch (IOException e) {
            System.out.println("Error saving database: " + e.getMessage());
        }
    }

    // Setter methods with user input prompts
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
        System.out.print("Enter fuel size (in liters): ");
        while (!scanner.hasNextDouble()) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.next();
        }
        this.fuelSize = scanner.nextDouble();
        scanner.nextLine();
    }

    public void setFuelBurn() {
        System.out.print("Enter fuel burn rate (liters/hour): ");
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

    // Displays current airplane information
    public void print() {
        if (!dataSet) {
            System.out.println("\nERROR: No airplane data has been set yet!");
            System.out.println("Please use option 1 to set airplane information first.");
            waitForEnter();
            return;
        }
        
        System.out.println("\nCurrent Airplane Information:");
        System.out.println("Make: " + make);
        System.out.println("Model: " + model);
        System.out.println("Aircraft Type: " + aircraftType);
        System.out.println("Fuel Size: " + fuelSize + " liters");
        System.out.println("Fuel Burn Rate: " + fuelBurn + " liters/hour");
        System.out.println("Airspeed: " + airspeed + " knots");
        
        waitForEnter();
    }

    // Allows modification of existing airplane data
    public void modify() {
        if (!dataSet) {
            System.out.println("\nERROR: Cannot modify - no airplane data has been set yet!");
            waitForEnter();
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
        if (!sizeInput.isEmpty()) {
            try {
                fuelSize = Double.parseDouble(sizeInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Keeping old value.");
            }
        }
        
        System.out.print("Fuel Burn Rate (" + fuelBurn + "): ");
        String burnInput = scanner.nextLine();
        if (!burnInput.isEmpty()) {
            try {
                fuelBurn = Double.parseDouble(burnInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Keeping old value.");
            }
        }
        
        System.out.print("Airspeed (" + airspeed + "): ");
        String speedInput = scanner.nextLine();
        if (!speedInput.isEmpty()) {
            try {
                airspeed = Integer.parseInt(speedInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Keeping old value.");
            }
        }
        
        System.out.println("\nAirplane information updated successfully!");
        waitForEnter();
    }

    // Saves current airplane to database
    public void saveToDatabase() {
        if (!dataSet) {
            System.out.println("\nERROR: Cannot save - no airplane data has been set yet!");
            waitForEnter();
            return;
        }
        
        Airplane airplaneToSave = new Airplane();
        airplaneToSave.make = this.make;
        airplaneToSave.model = this.model;
        airplaneToSave.aircraftType = this.aircraftType;
        airplaneToSave.fuelSize = this.fuelSize;
        airplaneToSave.fuelBurn = this.fuelBurn;
        airplaneToSave.airspeed = this.airspeed;
        airplaneToSave.dataSet = true;
        
        airplaneDB.addAirplane(airplaneToSave);
        saveDatabase();
        System.out.println("\nAirplane saved to database successfully!");
        waitForEnter();
        
        resetFields();
    }

    // Lists all airplanes in database in formatted table
    public static void listAllAirplanes() {
        List<Airplane> airplanes = airplaneDB.getAllAirplanes();
        if (airplanes.isEmpty()) {
            System.out.println("\nNo airplanes in database!");
            waitForEnter();
            return;
        }
        
        System.out.println("\nAll Airplanes in Database:");
        System.out.println("==================================================================");
        System.out.printf("%-3s %-15s %-15s %-10s %-12s %-12s %-10s%n",
                       "#", "Make", "Model", "Type", "Fuel Size", "Fuel Burn", "Speed");
        System.out.println("==================================================================");
        
        int index = 1;
        for (Airplane plane : airplanes) {
            System.out.printf("%-3d %-15s %-15s %-10s %-12.1f %-12.1f %-10d%n",
                           index++,
                           plane.make,
                           plane.model,
                           plane.aircraftType,
                           plane.fuelSize,
                           plane.fuelBurn,
                           plane.airspeed);
        }
        
        System.out.println("==================================================================");
        waitForEnter();
    }

    // Modifies existing airplane in database
    public static void modifyDatabaseAirplane() {
        List<Airplane> airplanes = airplaneDB.getAllAirplanes();
        if (airplanes.isEmpty()) {
            System.out.println("\nNo airplanes in database to modify!");
            waitForEnter();
            return;
        }
        
        listAllAirplanes();
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number of the airplane to modify: ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > airplanes.size()) {
                System.out.println("Invalid selection!");
                return;
            }
            
            Airplane selected = airplanes.get(choice - 1);
            Airplane modified = new Airplane();
            modified.make = selected.make;
            modified.model = selected.model;
            modified.aircraftType = selected.aircraftType;
            modified.fuelSize = selected.fuelSize;
            modified.fuelBurn = selected.fuelBurn;
            modified.airspeed = selected.airspeed;
            modified.dataSet = true;
            
            System.out.println("\nModifying Airplane #" + choice);
            modified.modify();
            
            airplanes.set(choice - 1, modified);
            saveDatabase();
            System.out.println("\nAirplane modified successfully!");
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a valid number.");
        }
    }

    // Removes airplane from database with confirmation
    public static void removeFromDatabase() {
        List<Airplane> airplanes = airplaneDB.getAllAirplanes();
        if (airplanes.isEmpty()) {
            System.out.println("\nNo airplanes in database to remove!");
            waitForEnter();
            return;
        }

        System.out.println("\nAll Airplanes in Database:");
        System.out.println("==================================================================");
        System.out.printf("%-3s %-15s %-15s %-10s %-12s %-12s %-10s%n",
                       "#", "Make", "Model", "Type", "Fuel Size", "Fuel Burn", "Speed");
        System.out.println("==================================================================");
        
        for (int i = 0; i < airplanes.size(); i++) {
            Airplane plane = airplanes.get(i);
            System.out.printf("%-3d %-15s %-15s %-10s %-12.1f %-12.1f %-10d%n",
                           (i + 1),
                           plane.make,
                           plane.model,
                           plane.aircraftType,
                           plane.fuelSize,
                           plane.fuelBurn,
                           plane.airspeed);
        }
        System.out.println("==================================================================");

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the number of the airplane to remove (0 to cancel): ");
        try {
            int choice = Integer.parseInt(scanner.nextLine());
            
            if (choice == 0) {
                System.out.println("\nRemoval canceled.");
                return;
            }
            
            if (choice < 1 || choice > airplanes.size()) {
                System.out.println("Invalid selection! Please enter a number between 1 and " + airplanes.size());
                return;
            }
            
            Airplane toRemove = airplanes.get(choice - 1);
            
            System.out.println("\nAirplane selected for removal:");
            System.out.println("Make: " + toRemove.make);
            System.out.println("Model: " + toRemove.model);
            System.out.println("Type: " + toRemove.aircraftType);
            System.out.println("Fuel Size: " + toRemove.fuelSize + " liters");
            System.out.println("Fuel Burn: " + toRemove.fuelBurn + " liters/hour");
            System.out.println("Airspeed: " + toRemove.airspeed + " knots");
            
            System.out.print("\nCONFIRM: Are you sure you want to remove this airplane? (y/n): ");
            String confirm = scanner.nextLine().toLowerCase();
            
            if (confirm.equals("y")) {
                airplanes.remove(choice - 1);
                saveDatabase();
                System.out.println("\nAirplane removed successfully!");
            } else {
                System.out.println("\nRemoval canceled.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a number.");
        }
        
        waitForEnter();
    }

    // Utility method to wait for user to press Enter
    public static void waitForEnter() {
        System.out.print("Press enter to continue...");
        try {
            System.in.read();
        } catch (IOException e) {
            // Ignore
        }
    }

    // Main program entry point
    public static void main(String[] args) {
        try (Scanner menuScanner = new Scanner(System.in)) {
            Airplane airplane = new Airplane();
            
            while (true) {
                System.out.println("\nAirplane Management System");
                System.out.println("1. Set Airplane Information");
                System.out.println("2. View Current Airplane");
                System.out.println("3. Modify Current Airplane");
                System.out.println("4. Save to Database");
                System.out.println("5. List All Airplanes");
                System.out.println("6. Modify Airplane in Database");
                System.out.println("7. Remove Airplane from Database");
                System.out.println("8. Exit");
                System.out.print("Enter your choice: ");
                
                try {
                    int choice = Integer.parseInt(menuScanner.nextLine());
                    
                    switch (choice) {
                        case 1 -> {
                            airplane.setMake();
                            airplane.setModel();
                            airplane.setAircraftType();
                            airplane.setFuelSize();
                            airplane.setFuelBurn();
                            airplane.setAirSpeed();
                            airplane.dataSet = true;
                        }
                        case 2 -> airplane.print();
                        case 3 -> airplane.modify();
                        case 4 -> airplane.saveToDatabase();
                        case 5 -> listAllAirplanes();
                        case 6 -> modifyDatabaseAirplane();
                        case 7 -> removeFromDatabase();
                        case 8 -> {
                            System.out.println("Exiting...");
                            return;
                        }
                        default -> System.out.println("Invalid choice!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input! Please enter a number between 1-8.");
                }
            }
        }
    }
}

// Database class for storing airplane objects
class AirplaneDatabase implements Serializable {
    public static long serialVersionUID = 1L;
    public List<Airplane> airplanes;
    
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