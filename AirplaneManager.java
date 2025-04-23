/**
 * Airplane Management System
 * Provides a complete GUI-based system for managing airplane records including:
 * - Adding new airplanes
 * - Searching existing records
 * - Modifying airplane details
 * - Deleting records
 * - Listing all airplanes
 * @author Andrew
 */
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class AirplaneManager {
    // Database instance for storing airplane records
    public AirplaneDatabase planeDbase;
    
    // File name for persistent data storage
    public static String DATA_FILE = "airplanes.dat";

    /**
     * Default constructor initializes the database
     * and loads any existing airplane data
     */
    public AirplaneManager() {
        this.planeDbase = new AirplaneDatabase();
        loadAirplanes();
    }

    /**
     * Constructor with dependency injection (primarily for testing)
     * @param planeDbase Pre-configured airplane database instance
     */
    public AirplaneManager(AirplaneDatabase planeDbase) {
        this.planeDbase = planeDbase;
    }

    /**
     * Loads airplane records from persistent storage
     * Handles first-run scenario when no data file exists
     */
    @SuppressWarnings("unchecked")
    public void loadAirplanes() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            ArrayList<Airplane> loadedAirplanes = (ArrayList<Airplane>) ois.readObject();
            for (Airplane airplane : loadedAirplanes) {
                planeDbase.addAirplane(airplane);
                // Maintain nextKey integrity
                if (airplane.getKey() >= planeDbase.getNextKey()) {
                    planeDbase.setNextKey(airplane.getKey() + 1);
                }
            }
        } catch (FileNotFoundException e) {
            // Expected on first run
            System.out.println("No existing data file found. Starting with empty database.");
        } catch (IOException | ClassNotFoundException e) {
            showErrorDialog("Load Error", "Error loading airplane data: " + e.getMessage());
        }
    }

    /**
     * Saves current airplane records to persistent storage
     */
    public void saveAirplanes() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(new ArrayList<>(planeDbase.getAllAirplanes()));
        } catch (IOException e) {
            showErrorDialog("Save Error", "Error saving airplane data: " + e.getMessage());
        }
    }

    /**
     * Adds a new airplane record after validating all input fields
     * Shows appropriate error messages for invalid inputs
     */
    @SuppressWarnings("UseSpecificCatch")
    public void addAirplane() {
        try {
            // Collect and validate all required fields
            String make = showInputDialogWithValidation(
                "Enter airplane make (max 30 chars):",
                "Make must be ≤30 characters and cannot be empty",
                input -> input != null && !input.trim().isEmpty() && input.length() <= 30
            );

            String model = showInputDialogWithValidation(
                "Enter airplane model (max 30 chars):",
                "Model must be ≤30 characters and cannot be empty",
                input -> input != null && !input.trim().isEmpty() && input.length() <= 30
            );

            String aircraftType = showInputDialogWithValidation(
                "Enter aircraft type:",
                "Aircraft type cannot be empty (max 10 letters)",
                input -> input != null && input.matches("[A-Za-z ]+") && input.length() <= 10
            );

            double fuelSize = showNumericInputDialogWithValidation(
                "Enter fuel tank size (liters):",
                "Invalid fuel size. Must be positive.",
                0.1, Double.MAX_VALUE
            );

            int fuelType = showNumericInputDialogWithValidation(
                "Enter fuel type (1=Aviation, 2=Jet):",
                "Invalid fuel type. Must be 1 or 2.",
                1, 2
            );

            double fuelBurn = showNumericInputDialogWithValidation(
                "Enter fuel burn rate (liters/hour):",
                "Invalid fuel burn rate. Must be positive.",
                0.1, Double.MAX_VALUE
            );

            int airspeed = showNumericInputDialogWithValidation(
                "Enter cruising airspeed (knots):",
                "Invalid airspeed. Must be 50-1000 knots.",
                50, 1000
            );

            // Create and validate new airplane
            int key = planeDbase.getNextKey();
            Airplane airplane = new Airplane(make, model, aircraftType, fuelSize, fuelType, fuelBurn, airspeed, key);

            if (!validateAirplaneData(airplane)) {
                return;
            }

            planeDbase.addAirplane(airplane);
            saveAirplanes();
            JOptionPane.showMessageDialog(null, "Airplane added successfully with key: " + key);

        } catch (IllegalArgumentException e) {
            showErrorDialog("Input Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "Unexpected error adding airplane: " + e.getMessage());
        }
    }

    /**
     * Searches for airplanes by make, model, or type
     * Displays results in dialog box
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
            showErrorDialog("Error", "Unexpected error searching: " + e.getMessage());
        }
    }

    /**
     * Modifies existing airplane record
     * Allows partial updates (blank fields keep current values)
     */
    @SuppressWarnings("UseSpecificCatch")
    public void modifyAirplane() {
        try {
            // Create a panel with the list and input field
            JPanel panel = new JPanel(new BorderLayout());
            
            // Add the airplane list in a scrollable text area
            JTextArea listArea = new JTextArea(getCompactAirplaneList());
            listArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(listArea);
            scrollPane.setPreferredSize(new Dimension(500, 150));
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Add the input field below
            JPanel inputPanel = new JPanel();
            inputPanel.add(new JLabel("Enter airplane key to modify:"));
            JTextField keyField = new JTextField(10);
            inputPanel.add(keyField);
            panel.add(inputPanel, BorderLayout.SOUTH);
            
            // Show the dialog
            int result = JOptionPane.showConfirmDialog(
                null, 
                panel, 
                "Modify Airplane", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            
            // Validate the input
            int key;
            try {
                key = Integer.parseInt(keyField.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid airplane key: must be a number");
            }
            
            if (key < 1) {
                throw new IllegalArgumentException("Airplane key must be positive");
            }

            Airplane airplane = planeDbase.getAirplane(key);
            if (airplane == null) {
                throw new IllegalArgumentException("Airplane with key " + key + " not found");
            }

            JOptionPane.showMessageDialog(null, "Current details:\n" + airplane);

            // Field-by-field modification
            String make = showInputDialogWithValidation(
                "Enter new make (blank to keep current):\nCurrent: " + airplane.getMake(),
                "Make must be ≤30 chars",
                input -> input == null || input.isEmpty() || input.length() <= 30
            );
            if (make != null && !make.isEmpty()) airplane.setMake(make);

            String model = showInputDialogWithValidation(
                "Enter new model (blank to keep current):\nCurrent: " + airplane.getModel(),
                "Model must be ≤30 chars",
                input -> input == null || input.isEmpty() || input.length() <= 30
            );
            if (model != null && !model.isEmpty()) airplane.setModel(model);

            String aircraftType = showInputDialogWithValidation(
                "Enter new type (blank to keep current):\nCurrent: " + airplane.getAircraftType(),
                "Type must be ≤10 letters",
                input -> input == null || input.isEmpty() || (input.matches("[A-Za-z ]+") && input.length() <= 10)
            );
            if (aircraftType != null && !aircraftType.isEmpty()) airplane.setAircraftType(aircraftType);

            Double fuelSize = showNumericInputDialogWithValidation(
                "Enter new fuel size (0 to keep current):\nCurrent: " + airplane.getFuelSize(),
                "Fuel size must be positive",
                0.1, Double.MAX_VALUE,
                true
            );
            if (fuelSize != null && fuelSize != 0) airplane.setFuelSize(fuelSize);

            Integer fuelType = showNumericInputDialogWithValidation(
                "Enter new fuel type (0 to keep current):\nCurrent: " + 
                (airplane.getFuelType() == 1 ? "Aviation" : "Jet"),
                "Must be 1 or 2",
                1, 2,
                true
            );
            if (fuelType != null && fuelType != 0) airplane.setFuelType(fuelType);

            Double fuelBurn = showNumericInputDialogWithValidation(
                "Enter new fuel burn (0 to keep current):\nCurrent: " + airplane.getFuelBurn(),
                "Fuel burn must be positive",
                0.1, Double.MAX_VALUE,
                true
            );
            if (fuelBurn != null && fuelBurn != 0) airplane.setFuelBurn(fuelBurn);

            Integer airspeed = showNumericInputDialogWithValidation(
                "Enter new airspeed (0 to keep current):\nCurrent: " + airplane.getAirspeed(),
                "Airspeed must be 50-1000 knots",
                50, 1000,
                true
            );
            if (airspeed != null && airspeed != 0) airplane.setAirspeed(airspeed);

            if (!validateAirplaneData(airplane)) {
                throw new IllegalArgumentException("Validation failed");
            }

            planeDbase.updateAirplane(airplane);
            saveAirplanes();
            JOptionPane.showMessageDialog(null, "Airplane updated successfully.");

        } catch (IllegalArgumentException e) {
            showErrorDialog("Modification Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "Unexpected error modifying: " + e.getMessage());
        }
    }

    /**
     * Deletes airplane record after confirmation
     */
    @SuppressWarnings("UseSpecificCatch")
    public void deleteAirplane() {
        try {
            // Create a panel with the list and input field
            JPanel panel = new JPanel(new BorderLayout());
            
            // Add the airplane list in a scrollable text area
            JTextArea listArea = new JTextArea(getCompactAirplaneList());
            listArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(listArea);
            scrollPane.setPreferredSize(new Dimension(500, 150));
            panel.add(scrollPane, BorderLayout.CENTER);
            
            // Add the input field below
            JPanel inputPanel = new JPanel();
            inputPanel.add(new JLabel("Enter airplane key to delete:"));
            JTextField keyField = new JTextField(10);
            inputPanel.add(keyField);
            panel.add(inputPanel, BorderLayout.SOUTH);
            
            // Show the dialog
            int result = JOptionPane.showConfirmDialog(
                null, 
                panel, 
                "Delete Airplane", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            
            // Validate the input
            int key;
            try {
                key = Integer.parseInt(keyField.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid airplane key: must be a number");
            }
            
            if (key < 1) {
                throw new IllegalArgumentException("Airplane key must be positive");
            }

            Airplane airplane = planeDbase.getAirplane(key);
            if (airplane == null) {
                throw new IllegalArgumentException("Airplane with key " + key + " not found");
            }

            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Confirm delete this airplane?\n" + airplane,
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = planeDbase.deleteAirplane(key);
                if (deleted) {
                    saveAirplanes();
                    JOptionPane.showMessageDialog(null, "Airplane deleted successfully.");
                } else {
                    throw new IllegalStateException("Deletion failed");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Deletion cancelled.");
            }

        } catch (IllegalArgumentException e) {
            showErrorDialog("Deletion Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "Unexpected error deleting: " + e.getMessage());
        }
    }

    /**
     * Displays all airplanes in scrollable dialog
     */
    @SuppressWarnings("UseSpecificCatch")
    public void printAirplaneList() {
        try {
            Collection<Airplane> airplanes = planeDbase.getAllAirplanes();
            if (airplanes.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No airplanes in database.");
                return;
            }

            StringBuilder sb = new StringBuilder("Airplane List:\n");
            for (Airplane airplane : airplanes) {
                sb.append(airplane).append("\n\n");
            }

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(600, 400));
            JOptionPane.showMessageDialog(null, scrollPane, "Airplane List", JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            showErrorDialog("Error", "Error retrieving list: " + e.getMessage());
        }
    }

    /**
     * Shows compact list of airplanes with their keys
     * @return String containing formatted list
     */
    private String getCompactAirplaneList() {
        Collection<Airplane> airplanes = planeDbase.getAllAirplanes();
        if (airplanes.isEmpty()) {
            return "No airplanes in database.";
        }

        StringBuilder sb = new StringBuilder("Current Airplanes:\n");
        sb.append(String.format("%-5s %-15s %-15s %-10s\n", "Key", "Make", "Model", "Type"));
        sb.append("------------------------------------------------\n");
        for (Airplane airplane : airplanes) {
            sb.append(String.format("%-5d %-15s %-15s %-10s\n", 
                airplane.getKey(), 
                truncate(airplane.getMake(), 15),
                truncate(airplane.getModel(), 15),
                truncate(airplane.getAircraftType(), 10)));
        }
        return sb.toString();
    }

    private String truncate(String str, int length) {
        return str.length() > length ? str.substring(0, length-3) + "..." : str;
    }

    /**
     * Validates airplane data against business rules
     * @param airplane The airplane to validate
     * @return true if valid, false otherwise
     */
    private boolean validateAirplaneData(Airplane airplane) {
        try {
            // Validate all fields
            if (airplane.getMake() == null || airplane.getMake().trim().isEmpty()) {
                throw new IllegalArgumentException("Make cannot be empty");
            }
            if (airplane.getMake().length() > 30) {
                throw new IllegalArgumentException("Make cannot exceed 30 characters");
            }

            if (airplane.getModel() == null || airplane.getModel().trim().isEmpty()) {
                throw new IllegalArgumentException("Model cannot be empty");
            }
            if (airplane.getModel().length() > 30) {
                throw new IllegalArgumentException("Model cannot exceed 30 characters");
            }

            if (airplane.getAircraftType() == null || airplane.getAircraftType().trim().isEmpty()) {
                throw new IllegalArgumentException("Aircraft type required");
            }
            if (!airplane.getAircraftType().matches("[A-Za-z ]+")) {
                throw new IllegalArgumentException("Aircraft type must be letters only");
            }
            if (airplane.getAircraftType().length() > 10) {
                throw new IllegalArgumentException("Aircraft type must be ≤10 characters");
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
                throw new IllegalArgumentException("Airspeed must be 50-1000 knots");
            }

            // Check for duplicates
            for (Airplane existing : planeDbase.getAllAirplanes()) {
                if (existing.getKey() != airplane.getKey() &&
                    existing.getMake().equalsIgnoreCase(airplane.getMake()) &&
                    existing.getModel().equalsIgnoreCase(airplane.getModel()) &&
                    existing.getAircraftType().equalsIgnoreCase(airplane.getAircraftType()) &&
                    Math.abs(existing.getFuelSize() - airplane.getFuelSize()) < 0.001 &&
                    existing.getFuelType() == airplane.getFuelType() &&
                    Math.abs(existing.getFuelBurn() - airplane.getFuelBurn()) < 0.001 &&
                    existing.getAirspeed() == airplane.getAirspeed()) {
                    throw new IllegalArgumentException("Duplicate airplane exists");
                }
            }
            
            return true;
        } catch (IllegalArgumentException e) {
            showErrorDialog("Validation Error", e.getMessage());
            return false;
        }
    }

    // Helper methods for input validation

    private String showInputDialogWithValidation(String message, String errorMessage, 
            java.util.function.Predicate<String> validator) {
        while (true) {
            String input = JOptionPane.showInputDialog(message);
            if (input == null) throw new IllegalArgumentException("Operation cancelled");
            if (validator.test(input)) return input.trim();
            showErrorDialog("Invalid Input", errorMessage);
        }
    }

    private int showNumericInputDialogWithValidation(String message, String errorMessage, int min, int max) {
        while (true) {
            try {
                // Create a text area for the message
                JTextArea textArea = new JTextArea(message);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(textArea, BorderLayout.CENTER);
                panel.add(new JLabel("Enter value:"), BorderLayout.SOUTH);
                
                String input = JOptionPane.showInputDialog(null, panel, "Input", JOptionPane.QUESTION_MESSAGE);
                if (input == null) throw new IllegalArgumentException("Operation cancelled");
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
                showErrorDialog("Invalid Input", errorMessage);
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Please enter a valid integer");
            }
        }
    }

    private double showNumericInputDialogWithValidation(String message, String errorMessage, double min, double max) {
        while (true) {
            try {
                JTextArea textArea = new JTextArea(message);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(textArea, BorderLayout.CENTER);
                panel.add(new JLabel("Enter value:"), BorderLayout.SOUTH);
                
                String input = JOptionPane.showInputDialog(null, panel, "Input", JOptionPane.QUESTION_MESSAGE);
                if (input == null) throw new IllegalArgumentException("Operation cancelled");
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) return value;
                showErrorDialog("Invalid Input", errorMessage);
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Please enter a valid number");
            }
        }
    }

    private Integer showNumericInputDialogWithValidation(String message, String errorMessage, 
            int min, int max, boolean allowEmpty) {
        while (true) {
            try {
                JTextArea textArea = new JTextArea(message);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(textArea, BorderLayout.CENTER);
                panel.add(new JLabel("Enter value (0 to keep current):"), BorderLayout.SOUTH);
                
                String input = JOptionPane.showInputDialog(null, panel, "Input", JOptionPane.QUESTION_MESSAGE);
                if (input == null) throw new IllegalArgumentException("Operation cancelled");
                if (allowEmpty && input.trim().isEmpty()) return null;
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) return value;
                showErrorDialog("Invalid Input", errorMessage);
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Please enter a valid integer");
            }
        }
    }

    private Double showNumericInputDialogWithValidation(String message, String errorMessage, 
            double min, double max, boolean allowEmpty) {
        while (true) {
            try {
                JTextArea textArea = new JTextArea(message);
                textArea.setEditable(false);
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(textArea, BorderLayout.CENTER);
                panel.add(new JLabel("Enter value (0 to keep current):"), BorderLayout.SOUTH);
                
                String input = JOptionPane.showInputDialog(null, panel, "Input", JOptionPane.QUESTION_MESSAGE);
                if (input == null) throw new IllegalArgumentException("Operation cancelled");
                if (allowEmpty && input.trim().isEmpty()) return null;
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) return value;
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
     * Displays main menu and handles user navigation
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
                    default -> JOptionPane.showMessageDialog(null, "Invalid choice");
                }
            } catch (Exception e) {
                showErrorDialog("Error", "Unexpected error: " + e.getMessage());
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
 * Represents an airplane with specifications
 */
class Airplane implements Serializable {
    private String make;
    private String model;
    private String aircraftType;
    private double fuelSize;
    private int fuelType; // 1=Aviation, 2=Jet
    private double fuelBurn;
    private int airspeed;
    private final int key;

    /**
     * Constructs new Airplane instance
     */
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

    // Standard getters and setters
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
 
    /**
     * Returns formatted string representation
     */
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
 * In-memory database for airplane records
 */
class AirplaneDatabase extends HashMap<String, Airplane> {
    private Map<Integer, Airplane> airplanes = new HashMap<>();
    private int nextKey = 1;

    /**
     * Adds new airplane to database
     * @param airplane The airplane to add
     */
    public void addAirplane(Airplane airplane) {
        airplanes.put(airplane.getKey(), airplane);
    }

    /**
     * Retrieves airplane by key
     * @param key The airplane's unique key
     * @return The airplane or null if not found
     */
    public Airplane getAirplane(int key) {
        return airplanes.get(key);
    }

    /**
     * Searches for airplane by criteria
     * @param searchTerm The term to search for
     * @param searchType 0=make, 1=model, 2=type
     * @return Matching airplane or null
     */
    public Airplane searchAirplane(String searchTerm, int searchType) {
        for (Airplane airplane : airplanes.values()) {
            switch (searchType) {
                case 0 -> { if (airplane.getMake().equalsIgnoreCase(searchTerm)) return airplane; }
                case 1 -> { if (airplane.getModel().equalsIgnoreCase(searchTerm)) return airplane; }
                case 2 -> { if (airplane.getAircraftType().equalsIgnoreCase(searchTerm)) return airplane; }
            }
        }
        return null;
    }

    /**
     * Updates existing airplane record
     * @param airplane The airplane with updated values
     */
    public void updateAirplane(Airplane airplane) {
        airplanes.put(airplane.getKey(), airplane);
    }

    /**
     * Deletes airplane by key
     * @param key The airplane's unique key
     * @return true if deleted, false if not found
     */
    public boolean deleteAirplane(int key) {
        return airplanes.remove(key) != null;
    }

    /**
     * Gets all airplanes in database
     * @return Collection of all airplanes
     */
    public Collection<Airplane> getAllAirplanes() {
        return airplanes.values();
    }

    /**
     * Gets next available key
     * @return The next auto-increment key
     */
    public int getNextKey() {
        return nextKey++;
    }
    
    /**
     * Sets next key value
     * @param key The value to set
     */
    public void setNextKey(int key) {
        this.nextKey = key;
    }
}