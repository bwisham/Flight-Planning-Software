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
    
    // Constants for aircraft types
    private static final String[] AIRCRAFT_TYPES = {"Jet", "Prop", "Turboprop"};

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
    public void addAirplane() {
        try {
            // Create a panel for all inputs
            JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
            
            // Make
            JTextField makeField = new JTextField(20);
            panel.add(new JLabel("Make (max 30 chars):"));
            panel.add(makeField);
            
            // Model
            JTextField modelField = new JTextField(20);
            panel.add(new JLabel("Model (max 30 chars):"));
            panel.add(modelField);
            
            // Aircraft Type (dropdown)
            JComboBox<String> typeCombo = new JComboBox<>(AIRCRAFT_TYPES);
            panel.add(new JLabel("Aircraft Type:"));
            panel.add(typeCombo);
            
            // Fuel Size
            JTextField fuelSizeField = new JTextField(10);
            panel.add(new JLabel("Fuel Tank Size (liters):"));
            panel.add(fuelSizeField);
            
            // Fuel Type
            JComboBox<String> fuelTypeCombo = new JComboBox<>(new String[]{"Aviation", "Jet"});
            panel.add(new JLabel("Fuel Type:"));
            panel.add(fuelTypeCombo);
            
            // Fuel Burn
            JTextField fuelBurnField = new JTextField(10);
            panel.add(new JLabel("Fuel Burn Rate (liters/hour):"));
            panel.add(fuelBurnField);
            
            // Airspeed
            JTextField airspeedField = new JTextField(10);
            panel.add(new JLabel("Cruising Airspeed (knots):"));
            panel.add(airspeedField);
            
            // Show the dialog
            int result = JOptionPane.showConfirmDialog(
                null, 
                panel, 
                "Add New Airplane", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            
            // Validate inputs one by one with separate error messages
            String make = makeField.getText().trim();
            if (make.isEmpty()) {
                showErrorDialog("Invalid Input", "Make cannot be empty");
                return;
            }
            if (make.length() > 30) {
                showErrorDialog("Invalid Input", "Make cannot exceed 30 characters");
                return;
            }
            
            String model = modelField.getText().trim();
            if (model.isEmpty()) {
                showErrorDialog("Invalid Input", "Model cannot be empty");
                return;
            }
            if (model.length() > 30) {
                showErrorDialog("Invalid Input", "Model cannot exceed 30 characters");
                return;
            }
            
            String aircraftType = (String)typeCombo.getSelectedItem();
            
            double fuelSize;
            try {
                fuelSize = Double.parseDouble(fuelSizeField.getText().trim());
                if (fuelSize <= 0) {
                    showErrorDialog("Invalid Input", "Fuel size must be positive");
                    return;
                }
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Fuel size must be a valid number");
                return;
            }
            
            int fuelType = fuelTypeCombo.getSelectedIndex() + 1; // 1=Aviation, 2=Jet
            
            double fuelBurn;
            try {
                fuelBurn = Double.parseDouble(fuelBurnField.getText().trim());
                if (fuelBurn <= 0) {
                    showErrorDialog("Invalid Input", "Fuel burn rate must be positive");
                    return;
                }
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Fuel burn rate must be a valid number");
                return;
            }
            
            int airspeed;
            try {
                airspeed = Integer.parseInt(airspeedField.getText().trim());
                if (airspeed < 50 || airspeed > 1000) {
                    showErrorDialog("Invalid Input", "Airspeed must be between 50-1000 knots");
                    return;
                }
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Airspeed must be a valid integer");
                return;
            }
            
            // Create and validate new airplane
            int key = planeDbase.getNextKey();
            Airplane airplane = new Airplane(make, model, aircraftType, fuelSize, fuelType, fuelBurn, airspeed, key);

            if (!validateAirplaneData(airplane)) {
                return;
            }

            planeDbase.addAirplane(airplane);
            saveAirplanes();
            JOptionPane.showMessageDialog(null, "Airplane added successfully with key: " + key);

        } catch (Exception e) {
            showErrorDialog("Error", "Unexpected error adding airplane: " + e.getMessage());
        }
    }

    /**
     * Searches for airplanes by make, model, or type
     * Displays results in dialog box
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

            String searchTerm = JOptionPane.showInputDialog(
                null,
                choice == 0 ? "Enter airplane make:" :
                choice == 1 ? "Enter airplane model:" : "Enter airplane type:",
                "Search",
                JOptionPane.QUESTION_MESSAGE
            );

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                showErrorDialog("Search Error", "Search term cannot be empty");
                return;
            }

            Airplane result = planeDbase.searchAirplane(searchTerm.trim(), choice);
            if (result != null) {
                JOptionPane.showMessageDialog(null, "Search result:\n" + result);
            } else {
                JOptionPane.showMessageDialog(null, "No matching airplane found.");
            }

        } catch (Exception e) {
            showErrorDialog("Error", "Unexpected error searching: " + e.getMessage());
        }
    }

    /**
     * Modifies existing airplane record
     * Allows partial updates (blank fields keep current values)
     */
    public void modifyAirplane() {
        try {
            // Create a panel with the list and input field
            JPanel panel = new JPanel(new BorderLayout());
            
            // Add the airplane list in a scrollable text area
            JTextArea listArea = new JTextArea(getDetailedAirplaneList());
            listArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(listArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
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
                showErrorDialog("Invalid Input", "Airplane key must be a number");
                return;
            }
            
            if (key < 1) {
                showErrorDialog("Invalid Input", "Airplane key must be positive");
                return;
            }

            Airplane airplane = planeDbase.getAirplane(key);
            if (airplane == null) {
                showErrorDialog("Not Found", "Airplane with key " + key + " not found");
                return;
            }

            // Create a panel for all inputs with current values
            JPanel editPanel = new JPanel(new GridLayout(0, 2, 5, 5));
            
            // Make
            JTextField makeField = new JTextField(airplane.getMake(), 20);
            editPanel.add(new JLabel("Make (max 30 chars):"));
            editPanel.add(makeField);
            
            // Model
            JTextField modelField = new JTextField(airplane.getModel(), 20);
            editPanel.add(new JLabel("Model (max 30 chars):"));
            editPanel.add(modelField);
            
            // Aircraft Type (dropdown with current selection)
            JComboBox<String> typeCombo = new JComboBox<>(AIRCRAFT_TYPES);
            typeCombo.setSelectedItem(airplane.getAircraftType());
            editPanel.add(new JLabel("Aircraft Type:"));
            editPanel.add(typeCombo);
            
            // Fuel Size
            JTextField fuelSizeField = new JTextField(String.valueOf(airplane.getFuelSize()), 10);
            editPanel.add(new JLabel("Fuel Tank Size (liters):"));
            editPanel.add(fuelSizeField);
            
            // Fuel Type
            JComboBox<String> fuelTypeCombo = new JComboBox<>(new String[]{"Aviation", "Jet"});
            fuelTypeCombo.setSelectedIndex(airplane.getFuelType() - 1);
            editPanel.add(new JLabel("Fuel Type:"));
            editPanel.add(fuelTypeCombo);
            
            // Fuel Burn
            JTextField fuelBurnField = new JTextField(String.valueOf(airplane.getFuelBurn()), 10);
            editPanel.add(new JLabel("Fuel Burn Rate (liters/hour):"));
            editPanel.add(fuelBurnField);
            
            // Airspeed
            JTextField airspeedField = new JTextField(String.valueOf(airplane.getAirspeed()), 10);
            editPanel.add(new JLabel("Cruising Airspeed (knots):"));
            editPanel.add(airspeedField);
            
            // Show the dialog
            result = JOptionPane.showConfirmDialog(
                null, 
                editPanel, 
                "Modify Airplane", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            
            // Validate inputs one by one with separate error messages
            String make = makeField.getText().trim();
            if (!make.isEmpty() && make.length() > 30) {
                showErrorDialog("Invalid Input", "Make cannot exceed 30 characters");
                return;
            }
            
            String model = modelField.getText().trim();
            if (!model.isEmpty() && model.length() > 30) {
                showErrorDialog("Invalid Input", "Model cannot exceed 30 characters");
                return;
            }
            
            String aircraftType = (String)typeCombo.getSelectedItem();
            
            double fuelSize = airplane.getFuelSize();
            if (!fuelSizeField.getText().trim().isEmpty()) {
                try {
                    fuelSize = Double.parseDouble(fuelSizeField.getText().trim());
                    if (fuelSize <= 0) {
                        showErrorDialog("Invalid Input", "Fuel size must be positive");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid Input", "Fuel size must be a valid number");
                    return;
                }
            }
            
            int fuelType = airplane.getFuelType();
            if (fuelTypeCombo.getSelectedIndex() != airplane.getFuelType() - 1) {
                fuelType = fuelTypeCombo.getSelectedIndex() + 1;
            }
            
            double fuelBurn = airplane.getFuelBurn();
            if (!fuelBurnField.getText().trim().isEmpty()) {
                try {
                    fuelBurn = Double.parseDouble(fuelBurnField.getText().trim());
                    if (fuelBurn <= 0) {
                        showErrorDialog("Invalid Input", "Fuel burn rate must be positive");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid Input", "Fuel burn rate must be a valid number");
                    return;
                }
            }
            
            int airspeed = airplane.getAirspeed();
            if (!airspeedField.getText().trim().isEmpty()) {
                try {
                    airspeed = Integer.parseInt(airspeedField.getText().trim());
                    if (airspeed < 50 || airspeed > 1000) {
                        showErrorDialog("Invalid Input", "Airspeed must be between 50-1000 knots");
                        return;
                    }
                } catch (NumberFormatException e) {
                    showErrorDialog("Invalid Input", "Airspeed must be a valid integer");
                    return;
                }
            }
            
            // Update the airplane object
            if (!make.isEmpty()) airplane.setMake(make);
            if (!model.isEmpty()) airplane.setModel(model);
            airplane.setAircraftType(aircraftType);
            airplane.setFuelSize(fuelSize);
            airplane.setFuelType(fuelType);
            airplane.setFuelBurn(fuelBurn);
            airplane.setAirspeed(airspeed);

            if (!validateAirplaneData(airplane)) {
                return;
            }

            planeDbase.updateAirplane(airplane);
            saveAirplanes();
            JOptionPane.showMessageDialog(null, "Airplane updated successfully.");

        } catch (Exception e) {
            showErrorDialog("Error", "Unexpected error modifying: " + e.getMessage());
        }
    }

    /**
     * Deletes airplane record after confirmation
     */
    public void deleteAirplane() {
        try {
            // Create a panel with the list and input field
            JPanel panel = new JPanel(new BorderLayout());
            
            // Add the airplane list in a scrollable text area
            JTextArea listArea = new JTextArea(getDetailedAirplaneList());
            listArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(listArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
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
                showErrorDialog("Invalid Input", "Airplane key must be a number");
                return;
            }
            
            if (key < 1) {
                showErrorDialog("Invalid Input", "Airplane key must be positive");
                return;
            }

            Airplane airplane = planeDbase.getAirplane(key);
            if (airplane == null) {
                showErrorDialog("Not Found", "Airplane with key " + key + " not found");
                return;
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
                    showErrorDialog("Error", "Deletion failed");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Deletion cancelled.");
            }

        } catch (Exception e) {
            showErrorDialog("Error", "Unexpected error deleting: " + e.getMessage());
        }
    }

    /**
     * Displays all airplanes in scrollable dialog
     */
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
            scrollPane.setPreferredSize(new Dimension(600, 400));
            JOptionPane.showMessageDialog(null, scrollPane, "Airplane List", JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            showErrorDialog("Error", "Error retrieving list: " + e.getMessage());
        }
    }

    /**
     * Shows detailed list of airplanes matching the print format
     * @return String containing formatted list
     */
    private String getDetailedAirplaneList() {
        Collection<Airplane> airplanes = planeDbase.getAllAirplanes();
        if (airplanes.isEmpty()) {
            return "No airplanes in database.";
        }

        StringBuilder sb = new StringBuilder("Current Airplanes:\n");
        for (Airplane airplane : airplanes) {
            sb.append(airplane).append("\n\n");
        }
        return sb.toString();
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
            
            boolean validType = false;
            for (String type : AIRCRAFT_TYPES) {
                if (type.equals(airplane.getAircraftType())) {
                    validType = true;
                    break;
                }
            }
            if (!validType) {
                throw new IllegalArgumentException("Aircraft type must be one of: " + String.join(", ", AIRCRAFT_TYPES));
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

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays main menu and handles user navigation
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