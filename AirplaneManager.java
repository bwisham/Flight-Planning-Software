/*
 * Airplane Management System
 * This class manages airplane operations including adding, searching, modifying,
 * deleting airplanes, and displaying the airplane list.
 */

 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.*;
 
 public class AirplaneManager {
     private final AirplaneDatabase planeDbase; // Database instance for airplanes
 
     // Constructor initializes the airplane database
     public AirplaneManager() {
         this.planeDbase = new AirplaneDatabase();
     }
 
     /**
      * Adds a new airplane to the database after validating all input fields.
      * Collects airplane details through dialog boxes.
      */
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
                 "Enter fuel tank size (in gallons):",
                 "Invalid fuel size. Must be positive.",
                 0.1, Double.MAX_VALUE
             );
 
             // Validate and get fuel type
             String fuelType = showInputDialogWithValidation(
                 "Input fuel type:",
                 "Fuel Type cannot be empty or longer than 10 characters. You also must make sure to only use letters too.",
                 input -> input != null && input.matches("[A-Za-z ]+") && input.length() <= 10
             );
 
             // Validate and get fuel burn rate
             double fuelBurn = showNumericInputDialogWithValidation(
                 "Enter fuel burn rate (gallons/hour):",
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
                 throw new IllegalArgumentException("Airplane data validation failed");
             }
 
             planeDbase.addAirplane(airplane);
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
             if (!make.isEmpty()) {
                 airplane.setMake(make);
             }
 
             // Update model if provided
             String model = showInputDialogWithValidation(
                 "Enter new model (letters and numbers only, max 30 chars, leave blank to keep current):",
                 "Model must contain only letters, numbers and spaces and be ≤100 characters",
                 input -> input == null || input.isEmpty() || (input.matches("[A-Za-z0-9 ]+") && input.length() <= 30)
             );
             if (!model.isEmpty()) {
                 airplane.setModel(model);
             }
 
             // Update aircraft type if provided
             String aircraftType = showInputDialogWithValidation(
                 "Enter aircraft type:",
                 "Aircraft type cannot be empty and can be no longer than 10 characters. You also must make sure to only use letters too.:",
                 input -> input != null && input.matches("[A-Za-z ]+") && input.length() <= 10
             );
             if (aircraftType != null) {
                 airplane.setAircraftType(aircraftType);
             }
 
             // Update fuel size if provided
             Double fuelSize = showNumericInputDialogWithValidation(
                 "Enter new fuel tank size (or 0 to keep current):",
                 "Invalid fuel size. Must be positive.",
                 0.1, Double.MAX_VALUE,
                 true
             );
             if (fuelSize != null && fuelSize != 0) {
                 airplane.setFuelSize(fuelSize);
             }
 
             // Update fuel type if provided
             String fuelType = showInputDialogWithValidation(
                 "Input fuel type:",
                 "Fuel Type cannot be empty or longer than 10 characters. You also must make sure to only use letters too.",
                 input -> input != null && input.matches("[A-Za-z ]+") && input.length() <= 10
             );
             if (fuelType != null) {
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
      * Validates airplane data against business rules.
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
             if (airplane.getAircraftType() == null) {
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
             if (airplane.getFuelType() == null) {
                 throw new IllegalArgumentException("Fuel type must be selected");
             }
             if (!airplane.getFuelType().matches("[A-Za-z ]+")) {
                 throw new IllegalArgumentException("Fuel type must be letters only.");
             }
             if (airplane.getFuelType().length() > 10) {
                 throw new IllegalArgumentException("Fuel type must not be greater than 10 characters.");
             }
             if (airplane.getFuelBurn() <= 0) {
                 throw new IllegalArgumentException("Fuel burn rate must be positive");
             }
             if (airplane.getAirspeed() < 50 || airplane.getAirspeed() > 1000) {
                 throw new IllegalArgumentException("Airspeed must be between 50 and 1000 knots");
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
 class Airplane {
     private String make;
     private String model;
     private String aircraftType;
     private double fuelSize;
     private String fuelType;
     private double fuelBurn;
     private int airspeed;
     private final int key;
 
     public Airplane(String make, String model, String aircraftType, double fuelSize,
                    String fuelType, double fuelBurn, int airspeed, int key) {
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
     public String getFuelType() { return fuelType; }
     public void setFuelType(String fuelType) { this.fuelType = fuelType; }
     public double getFuelBurn() { return fuelBurn; }
     public void setFuelBurn(double fuelBurn) { this.fuelBurn = fuelBurn; }
     public int getAirspeed() { return airspeed; }
     public void setAirspeed(int airspeed) { this.airspeed = airspeed; }
     public int getKey() { return key; }
 
     @Override
     public String toString() {
         return String.format(
             "Airplane [Key: %d, Make: %s, Model: %s, Type: %s\n" +
             "Fuel: %s (%.1f gal), Burn Rate: %.1f gal/hr, Cruising Speed: %d knots]",
             key, make, model, aircraftType, fuelType, fuelSize, fuelBurn, airspeed
         );
     }
 }
 
 /**
  * In-memory database for storing and managing airplane data.
  */
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
                 case 0: // By Make
                     if (airplane.getMake().equalsIgnoreCase(searchTerm)) {
                         return airplane;
                     }
                     break;
                 case 1: // By Model
                     if (airplane.getModel().equalsIgnoreCase(searchTerm)) {
                         return airplane;
                     }
                     break;
                 case 2: // By Type
                     if (airplane.getAircraftType().equalsIgnoreCase(searchTerm)) {
                         return airplane;
                     }
                     break;
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
 }