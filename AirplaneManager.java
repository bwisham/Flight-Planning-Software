/**
 *
 * @author Andrew
 */
/*
 * Airplane Management System
 * This class manages airplane operations including adding, searching, modifying,
 * deleting airplanes, and displaying the airplane list.
 */


 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.*;
 
 public class AirplaneManager {
     public AirplaneDatabase planeDbase; // Database instance for airplanes
     public static String DATA_FILE = "airplanes.dat";
 
     // Constructor initializes the airplane database
     public AirplaneManager() {
         this.planeDbase = new AirplaneDatabase();
         loadAirplanes(); // Load saved data when starting
     }

    public AirplaneManager(AirplaneDatabase planeDbase) {
        this.planeDbase = planeDbase;
    }
 
     /**
      * Loads airplanes from file
      */
     @SuppressWarnings("unchecked")
     public void loadAirplanes() {
         try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
             ArrayList<Airplane> loadedAirplanes = (ArrayList<Airplane>) ois.readObject();
             for (Airplane airplane : loadedAirplanes) {
                 planeDbase.addAirplane(airplane);
                 // Ensure nextKey is always higher than any existing key
                 if (airplane.getKey() >= planeDbase.getNextKey()) {
                     planeDbase.setNextKey(airplane.getKey() + 1);
                 }
             }
         } catch (FileNotFoundException e) {
             // First run - no data file exists yet
             System.out.println("No existing data file found. Starting with empty database.");
         } catch (IOException | ClassNotFoundException e) {
             showErrorDialog("Load Error", "Error loading airplane data: " + e.getMessage());
         }
     }
 
     /**
      * Saves airplanes to file
      */
     public void saveAirplanes() {
         try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
             oos.writeObject(new ArrayList<>(planeDbase.getAllAirplanes()));
         } catch (IOException e) {
             showErrorDialog("Save Error", "Error saving airplane data: " + e.getMessage());
         }
     }
 
     /**
      * Adds a new airplane to the database after validating all input fields.
      * Collects airplane details through dialog boxes.
      */
     @SuppressWarnings("UseSpecificCatch")
     public void addAirplane() {
         try {
             // Validate and get airplane make
             String make = showInputDialogWithValidation(
                 "Enter airplane make (letters only, max 30 chars):",
                 "Make must contain only letters and spaces and be ≤30 characters",
                 input -> input != null && input.matches("[A-Za-z ]+") && input.length() <= 30
             );
 
             // Validate and get airplane model
             String model = showInputDialogWithValidation(
                 "Enter airplane model (letters and numbers only, max 30 chars):",
                 "Model must contain only letters, numbers and spaces and be ≤30 characters",
                 input -> input != null && input.matches("[A-Za-z0-9 ]+") && input.length() <= 30
             );
 
             // Validate and get aircraft type
             String aircraftType = showInputDialogWithValidation(
                 "Enter aircraft type:",
                 "Aircraft type cannot be empty and can be no longer than 10 characters. You also must make sure to only use letters too.:",
                 input -> input != null && input.matches("[A-Za-z ]+") && input.length() <= 10
             );
 
             // Validate and get fuel tank size
             double fuelSize = showNumericInputDialogWithValidation(
                 "Enter fuel tank size (in liters):",
                 "Invalid fuel size. Must be positive.",
                 0.1, Double.MAX_VALUE
             );
 
             // Validate and get fuel type (1 for Aviation, 2 for Jet)
             int fuelType = showNumericInputDialogWithValidation(
                 "Enter fuel type (1 for Aviation fuel, 2 for Jet fuel):",
                 "Invalid fuel type. Must be 1 or 2.",
                 1, 2
             );
 
             // Validate and get fuel burn rate
             double fuelBurn = showNumericInputDialogWithValidation(
                 "Enter fuel burn rate (liters/hour):",
                 "Invalid fuel burn rate. Must be positive.",
                 0.1, Double.MAX_VALUE
             );
 
             // Validate and get cruising airspeed
             int airspeed = showNumericInputDialogWithValidation(
                 "Enter cruising airspeed (knots):",
                 "Invalid airspeed. Must be between 50 and 1000 knots.",
                 50, 1000
             );
 
             // Create new airplane object
             int key = planeDbase.getNextKey();
             Airplane airplane = new Airplane(make, model, aircraftType, fuelSize, fuelType, fuelBurn, airspeed, key);
 
             // Validate airplane data before adding to database
             if (!validateAirplaneData(airplane)) {
                 return; // Validation failed, error message already shown
             }
 
             planeDbase.addAirplane(airplane);
             saveAirplanes(); // Save after adding
             JOptionPane.showMessageDialog(null, "Airplane added successfully with key: " + key);
 
         } catch (IllegalArgumentException e) {
             showErrorDialog("Input Error", e.getMessage());
         } catch (Exception e) {
             showErrorDialog("Error", "An unexpected error occurred while adding airplane: " + e.getMessage());
         }
     }
 
     /**
      * Searches for an airplane by make, model, or type.
      * Displays search results in a dialog box.
      */
     @SuppressWarnings("UseSpecificCatch")
     public void searchAirplane() {
         try {
             String[] options = {"By Make", "By Model", "By Type"};
             int choice = JOptionPane.showOptionDialog(
                 null,
                 "Search by:",
                 "Search Airplane",
                 JOptionPane.DEFAULT_OPTION,
                 JOptionPane.QUESTION_MESSAGE,
                 null,
                 options,
                 options[0]
             );
 
             if (choice == JOptionPane.CLOSED_OPTION) {
                 return;
             }
 
             String searchTerm = showInputDialogWithValidation(
                 choice == 0 ? "Enter airplane make:" :
                 choice == 1 ? "Enter airplane model:" : "Enter airplane type:",
                 "Search term cannot be empty",
                 input -> input != null && !input.trim().isEmpty()
             );
 
             Airplane result = planeDbase.searchAirplane(searchTerm, choice);
             if (result != null) {
                 JOptionPane.showMessageDialog(null, "Search result:\n" + result);
             } else {
                 JOptionPane.showMessageDialog(null, "No matching airplane found.");
             }
 
         } catch (IllegalArgumentException e) {
             showErrorDialog("Search Error", e.getMessage());
         } catch (Exception e) {
             showErrorDialog("Error", "An unexpected error occurred while searching: " + e.getMessage());
         }
     }
 
     /**
      * Modifies an existing airplane's details.
      * Allows partial updates by keeping current values if fields are left blank.
      */
     @SuppressWarnings("UseSpecificCatch")
     public void modifyAirplane() {
         try {
             Integer key = showNumericInputDialogWithValidation(
                 "Enter key of airplane to modify:",
                 "Invalid airplane key",
                 1, Integer.MAX_VALUE
             );
 
             Airplane airplane = planeDbase.getAirplane(key);
             if (airplane == null) {
                 throw new IllegalArgumentException("Airplane with key " + key + " not found");
             }
 
             JOptionPane.showMessageDialog(null, "Current airplane details:\n" + airplane);
 
             // Update make if provided
             String make = showInputDialogWithValidation(
                 "Enter new make (letters only, max 30 chars, leave blank to keep current):",
                 "Make must contain only letters and spaces and be ≤30 characters",
                 input -> input == null || input.isEmpty() || (input.matches("[A-Za-z ]+") && input.length() <= 30)
             );
             if (make != null && !make.isEmpty()) {
                 airplane.setMake(make);
             }
 
             // Update model if provided
             String model = showInputDialogWithValidation(
                 "Enter new model (letters and numbers only, max 30 chars, leave blank to keep current):",
                 "Model must contain only letters, numbers and spaces and be ≤100 characters",
                 input -> input == null || input.isEmpty() || (input.matches("[A-Za-z0-9 ]+") && input.length() <= 30)
             );
             if (model != null && !model.isEmpty()) {
                 airplane.setModel(model);
             }
 
             // Update aircraft type if provided
             String aircraftType = showInputDialogWithValidation(
                 "Enter aircraft type (leave blank to keep current):",
                 "Aircraft type cannot be empty and can be no longer than 10 characters. You also must make sure to only use letters too.:",
                 input -> input == null || input.isEmpty() || (input.matches("[A-Za-z ]+") && input.length() <= 10)
             );
             if (aircraftType != null && !aircraftType.isEmpty()) {
                 airplane.setAircraftType(aircraftType);
             }
 
             // Update fuel size if provided
             Double fuelSize = showNumericInputDialogWithValidation(
                 "Enter new fuel tank size in liters (or 0 to keep current):",
                 "Invalid fuel size. Must be positive.",
                 0.1, Double.MAX_VALUE,
                 true
             );
             if (fuelSize != null && fuelSize != 0) {
                 airplane.setFuelSize(fuelSize);
             }
 
             // Update fuel type if provided (1 for Aviation, 2 for Jet)
             Integer fuelType = showNumericInputDialogWithValidation(
                 "Enter new fuel type (1 for Aviation fuel, 2 for Jet fuel, or 0 to keep current):",
                 "Invalid fuel type. Must be 1 or 2.",
                 1, 2,
                 true
             );
             if (fuelType != null && fuelType != 0) {
                 airplane.setFuelType(fuelType);
             }
 
             // Update fuel burn rate if provided
             Double fuelBurn = showNumericInputDialogWithValidation(
                 "Enter new fuel burn rate (or 0 to keep current):",
                 "Invalid fuel burn rate. Must be positive.",
                 0.1, Double.MAX_VALUE,
                 true
             );
             if (fuelBurn != null && fuelBurn != 0) {
                 airplane.setFuelBurn(fuelBurn);
             }
 
             // Update airspeed if provided
             Integer airspeed = showNumericInputDialogWithValidation(
                 "Enter new cruising airspeed (or 0 to keep current, 50-1000 knots):",
                 "Invalid airspeed. Must be between 50 and 1000 knots.",
                 50, 1000,
                 true
             );
             if (airspeed != null && airspeed != 0) {
                 airplane.setAirspeed(airspeed);
             }
 
             if (!validateAirplaneData(airplane)) {
                 throw new IllegalArgumentException("Modified airplane data validation failed");
             }
 
             planeDbase.updateAirplane(airplane);
             saveAirplanes(); // Save after modifying
             JOptionPane.showMessageDialog(null, "Airplane updated successfully.");
 
         } catch (IllegalArgumentException e) {
             showErrorDialog("Modification Error", e.getMessage());
         } catch (Exception e) {
             showErrorDialog("Error", "An unexpected error occurred while modifying airplane: " + e.getMessage());
         }
     }
 
     /**
      * Deletes an airplane from the database after confirmation.
      */
     @SuppressWarnings("UseSpecificCatch")
     public void deleteAirplane() {
         try {
             Integer key = showNumericInputDialogWithValidation(
                 "Enter key of airplane to delete:",
                 "Invalid airplane key",
                 1, Integer.MAX_VALUE
             );
 
             Airplane airplane = planeDbase.getAirplane(key);
             if (airplane == null) {
                 throw new IllegalArgumentException("Airplane with key " + key + " not found");
             }
 
             int confirm = JOptionPane.showConfirmDialog(
                 null,
                 "Are you sure you want to delete this airplane?\n" + airplane,
                 "Confirm Deletion",
                 JOptionPane.YES_NO_OPTION
             );
 
             if (confirm == JOptionPane.YES_OPTION) {
                 boolean deleted = planeDbase.deleteAirplane(key);
                 if (deleted) {
                     saveAirplanes(); // Save after deleting
                     JOptionPane.showMessageDialog(null, "Airplane deleted successfully.");
                 } else {
                     throw new IllegalStateException("Failed to delete airplane");
                 }
             } else {
                 JOptionPane.showMessageDialog(null, "Deletion cancelled.");
             }
 
         } catch (IllegalArgumentException e) {
             showErrorDialog("Deletion Error", e.getMessage());
         } catch (Exception e) {
             showErrorDialog("Error", "An unexpected error occurred while deleting airplane: " + e.getMessage());
         }
     }
 
     /**
      * Displays a scrollable list of all airplanes in the database.
      */
     @SuppressWarnings("UseSpecificCatch")
     public void printAirplaneList() {
         try {
             Collection<Airplane> airplanes = planeDbase.getAllAirplanes();
             if (airplanes.isEmpty()) {
                 JOptionPane.showMessageDialog(null, "No airplanes in the database.");
                 return;
             }
 
             StringBuilder sb = new StringBuilder("Current Airplane List:\n");
             for (Airplane airplane : airplanes) {
                 sb.append(airplane).append("\n\n");
             }
 
             JTextArea textArea = new JTextArea(sb.toString());
             textArea.setEditable(false);
             JScrollPane scrollPane = new JScrollPane(textArea);
             scrollPane.setPreferredSize(new java.awt.Dimension(600, 400));
             JOptionPane.showMessageDialog(null, scrollPane, "Airplane List", JOptionPane.PLAIN_MESSAGE);
 
         } catch (Exception e) {
             showErrorDialog("Error", "An error occurred while retrieving airplane list: " + e.getMessage());
         }
     }
 
     /**
      * Validates airplane data against business rules and checks for duplicates.
      * @param airplane The airplane object to validate
      * @return true if valid, false otherwise
      */
     private boolean validateAirplaneData(Airplane airplane) {
         try {
             // Validate make (letters only, max 30 chars)
             if (airplane.getMake() == null || airplane.getMake().trim().isEmpty()) {
                 throw new IllegalArgumentException("Make cannot be empty");
             }
             if (!airplane.getMake().matches("[A-Za-z ]+")) {
                 throw new IllegalArgumentException("Make must contain only letters and spaces");
             }
             if (airplane.getMake().length() > 30) {
                 throw new IllegalArgumentException("Make cannot exceed 30 characters");
             }
 
             // Validate model (letters only, max 30 chars)
             if (airplane.getModel() == null || airplane.getModel().trim().isEmpty()) {
                 throw new IllegalArgumentException("Model cannot be empty");
             }
             if (!airplane.getModel().matches("[A-Za-z0-9 ]+")) {
                 throw new IllegalArgumentException("Model must contain only letters, numbers and spaces");
             }
             if (airplane.getModel().length() > 30) {
                 throw new IllegalArgumentException("Model cannot exceed 30 characters");
             }
             if (airplane.getAircraftType() == null || airplane.getAircraftType().trim().isEmpty()) {
                 throw new IllegalArgumentException("Aircraft type must be selected");
             }
             if (!airplane.getAircraftType().matches("[A-Za-z ]+")) {
                 throw new IllegalArgumentException("Aircraft type must be letters only");
             }
             if (airplane.getAircraftType().length() > 10) {
                 throw new IllegalArgumentException("Aircraft type must not be greater than 10 characters");
             }
             if (airplane.getFuelSize() <= 0) {
                 throw new IllegalArgumentException("Fuel size must be positive");
             }
             if (airplane.getFuelType() != 1 && airplane.getFuelType() != 2) {
                 throw new IllegalArgumentException("Fuel type must be 1 (Aviation) or 2 (Jet)");
             }
             if (airplane.getFuelBurn() <= 0) {
                 throw new IllegalArgumentException("Fuel burn rate must be positive");
             }
             if (airplane.getAirspeed() < 50 || airplane.getAirspeed() > 1000) {
                 throw new IllegalArgumentException("Airspeed must be between 50 and 1000 knots");
             }
 
             // Check for duplicate airplane (all properties except key must not match)
             for (Airplane existing : planeDbase.getAllAirplanes()) {
                 if (existing.getKey() != airplane.getKey() && // Skip checking against itself
                     existing.getMake().equalsIgnoreCase(airplane.getMake()) &&
                     existing.getModel().equalsIgnoreCase(airplane.getModel()) &&
                     existing.getAircraftType().equalsIgnoreCase(airplane.getAircraftType()) &&
                     Math.abs(existing.getFuelSize() - airplane.getFuelSize()) < 0.001 && // Account for floating point precision
                     existing.getFuelType() == airplane.getFuelType() &&
                     Math.abs(existing.getFuelBurn() - airplane.getFuelBurn()) < 0.001 &&
                     existing.getAirspeed() == airplane.getAirspeed()) {
                     throw new IllegalArgumentException("An identical airplane already exists in the system");
                 }
             }
             
             return true;
         } catch (IllegalArgumentException e) {
             showErrorDialog("Validation Error", e.getMessage());
             return false;
         }
     }
 
     // Helper methods for input validation
     private String showInputDialogWithValidation(String message, String errorMessage, java.util.function.Predicate<String> validator) {
         while (true) {
             String input = JOptionPane.showInputDialog(message);
             if (input == null) {
                 throw new IllegalArgumentException("Operation cancelled");
             }
             if (validator.test(input)) {
                 return input.trim();
             }
             if (errorMessage != null) {
                 showErrorDialog("Invalid Input", errorMessage);
             }
         }
     }
 
     private int showNumericInputDialogWithValidation(String message, String errorMessage, int min, int max) {
         while (true) {
             try {
                 String input = JOptionPane.showInputDialog(message);
                 if (input == null) {
                     throw new IllegalArgumentException("Operation cancelled");
                 }
                 int value = Integer.parseInt(input);
                 if (value >= min && value <= max) {
                     return value;
                 }
                 showErrorDialog("Invalid Input", errorMessage);
             } catch (NumberFormatException e) {
                 showErrorDialog("Invalid Input", "Please enter a valid integer");
             }
         }
     }
 
     private double showNumericInputDialogWithValidation(String message, String errorMessage, double min, double max) {
         while (true) {
             try {
                 String input = JOptionPane.showInputDialog(message);
                 if (input == null) {
                     throw new IllegalArgumentException("Operation cancelled");
                 }
                 double value = Double.parseDouble(input);
                 if (value >= min && value <= max) {
                     return value;
                 }
                 showErrorDialog("Invalid Input", errorMessage);
             } catch (NumberFormatException e) {
                 showErrorDialog("Invalid Input", "Please enter a valid number");
             }
         }
     }
 
     private Integer showNumericInputDialogWithValidation(String message, String errorMessage, int min, int max, boolean allowEmpty) {
         while (true) {
             try {
                 String input = JOptionPane.showInputDialog(message);
                 if (input == null) {
                     throw new IllegalArgumentException("Operation cancelled");
                 }
                 if (allowEmpty && input.trim().isEmpty()) {
                     return null;
                 }
                 int value = Integer.parseInt(input);
                 if (value >= min && value <= max) {
                     return value;
                 }
                 showErrorDialog("Invalid Input", errorMessage);
             } catch (NumberFormatException e) {
                 showErrorDialog("Invalid Input", "Please enter a valid integer");
             }
         }
     }
 
     private Double showNumericInputDialogWithValidation(String message, String errorMessage, double min, double max, boolean allowEmpty) {
         while (true) {
             try {
                 String input = JOptionPane.showInputDialog(message);
                 if (input == null) {
                     throw new IllegalArgumentException("Operation cancelled");
                 }
                 if (allowEmpty && input.trim().isEmpty()) {
                     return null;
                 }
                 double value = Double.parseDouble(input);
                 if (value >= min && value <= max) {
                     return value;
                 }
                 showErrorDialog("Invalid Input", errorMessage);
             } catch (NumberFormatException e) {
                 showErrorDialog("Invalid Input", "Please enter a valid number");
             }
         }
     }
 
     private void showErrorDialog(String title, String message) {
         JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
     }
 
     /**
      * Displays the main menu and handles user choices.
      */
     @SuppressWarnings("UseSpecificCatch")
     public void showMenu() {
         String[] options = {
             "Add Airplane",
             "Search Airplane",
             "Modify Airplane",
             "Delete Airplane",
             "Print Airplane List",
             "Exit"
         };
 
         while (true) {
             try {
                 int choice = JOptionPane.showOptionDialog(
                     null,
                     "Airplane Management System",
                     "Main Menu",
                     JOptionPane.DEFAULT_OPTION,
                     JOptionPane.PLAIN_MESSAGE,
                     null,
                     options,
                     options[0]
                 );
 
                 if (choice == JOptionPane.CLOSED_OPTION || choice == 5) {
                     JOptionPane.showMessageDialog(null, "Exiting...");
                     return;
                 }
 
                 switch (choice) {
                     case 0 -> addAirplane();
                     case 1 -> searchAirplane();
                     case 2 -> modifyAirplane();
                     case 3 -> deleteAirplane();
                     case 4 -> printAirplaneList();
                     default -> JOptionPane.showMessageDialog(null, "Invalid choice. Please try again.");
                 }
             } catch (Exception e) {
                 showErrorDialog("Error", "An unexpected error occurred: " + e.getMessage());
             }
         }
     }
 
     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
             AirplaneManager manager = new AirplaneManager();
             manager.showMenu();
         });
     }
 }
 
 /**
  * Represents an airplane with all its properties.
  */
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
 
 /**
  * In-memory database for storing and managing airplane data.
  */
 class AirplaneDatabase {
     private Map<Integer, Airplane> airplanes = new HashMap<>();
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
