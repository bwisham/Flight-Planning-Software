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
    private double fuelBurn;
    private int airspeed;
    private transient Scanner scanner;
    private boolean dataSet = false;
    private static final String DB_FILE = "Airplanedb1.dat";
    private static final AirplaneDatabase airplaneDB = loadDatabase();

    public Airplane() {
        this.scanner = new Scanner(System.in);
        resetFields();
    }

    private void resetFields() {
        this.make = "";
        this.model = "";
        this.aircraftType = "";
        this.fuelSize = 0.0;
        this.fuelBurn = 0.0;
        this.airspeed = 0;
        this.dataSet = false;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.scanner = new Scanner(System.in);
    }

    private static AirplaneDatabase loadDatabase() {
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

    private static void saveDatabase() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DB_FILE))) {
            oos.writeObject(airplaneDB);
            System.out.println("Database saved successfully to " + DB_FILE);
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
        System.out.println("Fuel Size: " + fuelSize + " liters");
        System.out.println("Fuel Burn Rate: " + fuelBurn + " liters/hour");
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
        System.out.print("Press enter to continue...");
        scanner.nextLine();
    }

    public void saveToDatabase() {
        if (!dataSet) {
            System.out.println("\nERROR: Cannot save - no airplane data has been set yet!");
            System.out.print("Press enter to continue...");
            scanner.nextLine();
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
        System.out.print("Press enter to continue...");
        scanner.nextLine();
        
        resetFields();
    }

    public static void listAllAirplanes() {
        List<Airplane> airplanes = airplaneDB.getAllAirplanes();
        if (airplanes.isEmpty()) {
            System.out.println("\nNo airplanes in database!");
            System.out.print("Press enter to continue...");
            try {
                System.in.read();
            } catch (IOException e) {
            }
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
        System.out.print("Press enter to continue...");
        try {
            System.in.read();
        } catch (IOException e) {
        }
    }

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
                System.out.println("6. Exit");
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
                        case 6 -> {
                            System.out.println("Exiting...");
                            return;
                        }
                        default -> System.out.println("Invalid choice!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input! Please enter a number between 1-6.");
                }
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