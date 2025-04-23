/**
 * Airport Management System
 * Provides a complete GUI-based system for managing airport records including:
 * - Adding new airports
 * - Searching existing records
 * - Modifying airport details
 * - Deleting records
 * - Listing all airports
 */
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class AirportManager {
    // Database instance for storing airport records
    public AirportDatabase portDbase;

    /**
     * Default constructor initializes the airport database
     */
    public AirportManager() {
        this.portDbase = new AirportDatabase();
    }

    /**
     * Adds a new airport record after validating all input fields
     * Shows appropriate error messages for invalid inputs
     */
    @SuppressWarnings("UseSpecificCatch")
    public void addAirport() {
        try {
            // Validate and collect airport name
            String name = showInputDialogWithValidation(
                "Enter the name of the airport (max 40 chars):",
                "Airport name cannot exceed 40 characters",
                input -> input != null && input.length() <= 40
            );

            // Check for duplicate airport names
            if (portDbase.airportNameExists(name)) {
                throw new IllegalArgumentException("An airport with this name already exists.");
            }

            // Validate and collect ICAO code
            String icao = showInputDialogWithValidation(
                "Enter the ICAO Identifier of the airport (exactly 4 characters):",
                "ICAO must be exactly 4 characters",
                input -> input != null && input.length() == 4
            ).toUpperCase();

            // Check for duplicate ICAO codes
            if (portDbase.icaoExists(icao)) {
                throw new IllegalArgumentException("An airport with this ICAO code already exists.");
            }

            // Validate and collect latitude
            double latitude = showNumericInputDialogWithValidation(
                "Enter the latitude of the airport (-90 to 90):",
                "Latitude must be between -90 and 90",
                -90.0, 90.0,
                false
            ).doubleValue();

            // Validate and collect longitude
            double longitude = showNumericInputDialogWithValidation(
                "Enter the longitude of the airport (-180 to 180):",
                "Longitude must be between -180 and 180",
                -180.0, 180.0,
                false
            ).doubleValue();

            // Check for duplicate coordinates
            if (portDbase.coordinatesExist(latitude, longitude)) {
                throw new IllegalArgumentException("An airport already exists at these exact coordinates.");
            }

            // Validate and collect fuel type
            int fuelType = showIntegerInputDialogWithValidation(
                "Enter the fuel types the airport supports:\n" +
                "1 = AVGAS\n2 = JA-1 / JP-8\n3 = Both",
                "Fuel type must be 1, 2, or 3",
                1, 3,
                false
            );

            // Validate and collect radio type (restricted to UHF, VHF, or HF)
            String radioType = showInputDialogWithValidation(
                "Enter the radio type (must be UHF, VHF, or HF):",
                "Radio type must be UHF, VHF, or HF",
                input -> input != null && 
                        (input.equalsIgnoreCase("UHF") || 
                         input.equalsIgnoreCase("VHF") || 
                         input.equalsIgnoreCase("HF"))
            ).toUpperCase();

            // Validate and collect radio frequency based on radio type
            double radioFrequency;
            if (radioType.equalsIgnoreCase("VHF")) {
                radioFrequency = showNumericInputDialogWithValidation(
                    "Enter the VHF radio frequency (118.0 to 136.975):",
                    "VHF frequency must be between 118.0 and 136.975 MHz",
                    118.0, 136.975,
                    false
                ).doubleValue();
            } else if (radioType.equalsIgnoreCase("UHF")) {
                radioFrequency = showNumericInputDialogWithValidation(
                    "Enter the UHF radio frequency (225 to 399.95):",
                    "UHF frequency must be between 225 and 399.95 MHz",
                    225.0, 399.95,
                    false
                ).doubleValue();
            } else { // HF
                radioFrequency = showNumericInputDialogWithValidation(
                    "Enter the HF radio frequency (2 to 30):",
                    "HF frequency must be between 2 and 30 MHz",
                    2.0, 30.0,
                    false
                ).doubleValue();
            }

            // Create new airport record
            int key = portDbase.getNextKey();
            Airport airport = new Airport(name, icao, latitude, longitude, fuelType, key, radioType, radioFrequency);

            // Final validation before adding
            if (!validateAirportData(airport)) {
                throw new IllegalArgumentException("Airport data validation failed");
            }

            // Add to database and show success message
            portDbase.addAirport(airport);
            JOptionPane.showMessageDialog(null, "Airport added successfully with key: " + key);

        } catch (IllegalArgumentException e) {
            showErrorDialog("Input Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "An unexpected error occurred while adding airport: " + e.getMessage());
        }
    }

    /**
     * Searches for airports by name or ICAO code
     * Displays results in dialog box
     */
    @SuppressWarnings("UseSpecificCatch")
    public void searchAirport() {
        try {
            // Present search options to user
            String[] options = {"By Name", "By ICAO"};
            int choice = JOptionPane.showOptionDialog(
                null,
                "Search by:",
                "Search Airport",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (choice == JOptionPane.CLOSED_OPTION) {
                return;
            }

            // Get search term based on selected option
            String searchTerm = showInputDialogWithValidation(
                choice == 0 ? "Enter airport name:" : "Enter ICAO identifier (4 characters):",
                choice == 0 ? "Airport name cannot be blank" : "ICAO code must be 4 characters",
                input -> input != null && !input.trim().isEmpty()
            );

            // Additional validation for ICAO search
            if (choice == 1 && searchTerm.length() != 4) {
                throw new IllegalArgumentException("ICAO must be exactly 4 characters");
            }

            // Perform search and display results
            Airport result = portDbase.searchAirport(searchTerm);
            if (result != null) {
                JOptionPane.showMessageDialog(null, "Search result:\n" + result);
            } else {
                JOptionPane.showMessageDialog(null, "No matching airport found.");
            }

        } catch (IllegalArgumentException e) {
            showErrorDialog("Search Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "An unexpected error occurred while searching: " + e.getMessage());
        }
    }

    /**
     * Modifies existing airport record with field-by-field updates
     * Preserves current values when fields are left blank
     * Shows list of airports with key input on same screen
     */
    @SuppressWarnings({"UnnecessaryUnboxing", "UseSpecificCatch"})
    public void modifyAirport() {
        try {
            // First get all airports
            Collection<Airport> allAirports = portDbase.getAllAirports();
            if (allAirports.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No airports available to modify.");
                return;
            }

            // Build the list of airports with keys
            StringBuilder airportList = new StringBuilder("Available Airports:\n");
            airportList.append(String.format("%-6s %-40s %-6s\n", "Key", "Airport Name", "ICAO"));
            airportList.append("------------------------------------------------\n");
            for (Airport airport : allAirports) {
                airportList.append(String.format("%-6d %-40s %-6s\n", 
                    airport.getKey(), 
                    airport.getName(), 
                    airport.getIcao()));
            }

            // Create panel with both the list and input field
            JPanel panel = new JPanel(new BorderLayout());
            
            // Add the airport list in a scrollable text area
            JTextArea textArea = new JTextArea(airportList.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(600, 300));
            
            // Add the input field for the key
            JTextField keyField = new JTextField(10);
            JPanel inputPanel = new JPanel();
            inputPanel.add(new JLabel("Enter airport key to modify:"));
            inputPanel.add(keyField);
            
            // Add components to main panel
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(inputPanel, BorderLayout.SOUTH);
            
            // Show the combined dialog
            int result = JOptionPane.showConfirmDialog(
                null, 
                panel, 
                "Modify Airport", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (result != JOptionPane.OK_OPTION) {
                return; // User cancelled
            }
            
            // Validate the entered key
            String keyText = keyField.getText();
            if (keyText == null || keyText.trim().isEmpty()) {
                throw new IllegalArgumentException("No key entered");
            }
            
            int key;
            try {
                key = Integer.parseInt(keyText.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid key format. Please enter a number.");
            }
            
            Airport airport = portDbase.getAirport(key);
            if (airport == null) {
                throw new IllegalArgumentException("Airport with key " + key + " not found");
            }

            // Show current details before modification
            JOptionPane.showMessageDialog(null, "Current airport details:\n" + airport);
            
            // Field-by-field modification with preservation of existing values
            
            // Update name if provided
            String name = showInputDialogWithValidation(
                "Enter new name (max 40 chars, leave blank to keep current):\n" +
                "Current: " + airport.getName(),
                "Airport name must be ≤40 characters",
                input -> input.isEmpty() || (!input.trim().isEmpty() && input.length() <= 40)
            );
            if (!name.isEmpty()) {
                if (!name.equalsIgnoreCase(airport.getName())) {
                    if (portDbase.airportNameExists(name)) {
                        throw new IllegalArgumentException("An airport with this name already exists.");
                    }
                    airport.setName(name);
                }
            }

            // Update ICAO if provided
            String icao = showInputDialogWithValidation(
                "Enter new ICAO (exactly 4 chars, leave blank to keep current):\n" +
                "Current: " + airport.getIcao(),
                "ICAO must be exactly 4 characters",
                input -> input.isEmpty() || input.length() == 4
            );
            if (!icao.isEmpty()) {
                icao = icao.toUpperCase();
                if (!icao.equalsIgnoreCase(airport.getIcao())) {
                    if (portDbase.icaoExists(icao)) {
                        throw new IllegalArgumentException("An airport with this ICAO code already exists.");
                    }
                    airport.setIcao(icao);
                }
            }

            // Update latitude if provided
            Double latitude = showNumericInputDialogWithValidation(
                "Enter new latitude (-90 to 90, or 0 to keep current):\n" +
                "Current: " + airport.getLatitude(),
                "Invalid latitude. Must be between -90 and 90.",
                -90.0, 90.0,
                true
            );
            if (latitude != null && latitude != 0) {
                if (portDbase.coordinatesExist(latitude, airport.getLongitude()) && 
                    !(airport.getLatitude() == latitude && airport.getLongitude() == airport.getLongitude())) {
                    throw new IllegalArgumentException("An airport already exists at these exact coordinates.");
                }
                airport.setLatitude(latitude);
            }

            // Update longitude if provided
            Double longitude = showNumericInputDialogWithValidation(
                "Enter new longitude (-180 to 180, or 0 to keep current):\n" +
                "Current: " + airport.getLongitude(),
                "Invalid longitude. Must be between -180 and 180.",
                -180.0, 180.0,
                true
            );
            if (longitude != null && longitude != 0) {
                if (portDbase.coordinatesExist(airport.getLatitude(), longitude) && 
                    !(airport.getLatitude() == airport.getLatitude() && airport.getLongitude() == longitude)) {
                    throw new IllegalArgumentException("An airport already exists at these exact coordinates.");
                }
                airport.setLongitude(longitude);
            }

            // Update fuel type if provided
            Integer fuelType = showIntegerInputDialogWithValidation(
                "Enter new fuel type (or 0 to keep current):\n" +
                "Current: " + getFuelTypeDescription(airport.getFuelType()) + "\n" +
                "1 = AVGAS\n2 = JA-1 / JP-8\n3 = Both",
                "Invalid fuel type. Must be between 1 and 3.",
                1, 3,
                true
            );
            if (fuelType != null && fuelType != 0) {
                airport.setFuelType(fuelType);
            }

            // Update radio type if provided (restricted to UHF, VHF, or HF)
            String radioType = showInputDialogWithValidation(
                "Enter new radio type (must be UHF, VHF, or HF, leave blank to keep current):\n" +
                "Current: " + airport.getRadioType(),
                "Radio type must be exactly UHF, VHF, or HF (case insensitive).",
                input -> input.isEmpty() || 
                        (input.equalsIgnoreCase("UHF") || 
                         input.equalsIgnoreCase("VHF") || 
                         input.equalsIgnoreCase("HF"))
            );
            if (!radioType.isEmpty()) {
                airport.setRadioType(radioType.toUpperCase());
            }

            // Update radio frequency if provided
            Double radioFrequency = null;
            if (airport.getRadioType().equalsIgnoreCase("VHF")) {
                radioFrequency = showNumericInputDialogWithValidation(
                    "Enter new VHF radio frequency (118.0 to 136.975, or 0 to keep current):\n" +
                    "Current: " + airport.getRadioFrequency(),
                    "Invalid VHF frequency. Must be between 118.0 and 136.975.",
                    118.0, 136.975,
                    true
                );
            } else if (airport.getRadioType().equalsIgnoreCase("UHF")) {
                radioFrequency = showNumericInputDialogWithValidation(
                    "Enter new UHF radio frequency (225 to 399.95, or 0 to keep current):\n" +
                    "Current: " + airport.getRadioFrequency(),
                    "Invalid UHF frequency. Must be between 225 and 399.95.",
                    225.0, 399.95,
                    true
                );
            } else { // HF
                radioFrequency = showNumericInputDialogWithValidation(
                    "Enter new HF radio frequency (2 to 30, or 0 to keep current):\n" +
                    "Current: " + airport.getRadioFrequency(),
                    "Invalid HF frequency. Must be between 2 and 30.",
                    2.0, 30.0,
                    true
                );
            }
            if (radioFrequency != null && radioFrequency != 0) {
                airport.setRadioFrequency(radioFrequency);
            }

            // Final validation before updating
            if (!validateAirportData(airport)) {
                throw new IllegalArgumentException("Modified airport data validation failed");
            }

            // Update database and show success message
            portDbase.updateAirport(airport);
            JOptionPane.showMessageDialog(null, "Airport updated successfully.");

        } catch (IllegalArgumentException e) {
            showErrorDialog("Modification Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "An unexpected error occurred while modifying airport: " + e.getMessage());
        }
    }

    /**
     * Deletes an airport record after confirmation
     * Shows list of airports with key input on same screen
     */
    @SuppressWarnings("UseSpecificCatch")
    public void deleteAirport() {
        try {
            // First get all airports
            Collection<Airport> allAirports = portDbase.getAllAirports();
            if (allAirports.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No airports available to delete.");
                return;
            }

            // Build the list of airports with keys
            StringBuilder airportList = new StringBuilder("Available Airports:\n");
            airportList.append(String.format("%-6s %-40s %-6s\n", "Key", "Airport Name", "ICAO"));
            airportList.append("------------------------------------------------\n");
            for (Airport airport : allAirports) {
                airportList.append(String.format("%-6d %-40s %-6s\n", 
                    airport.getKey(), 
                    airport.getName(), 
                    airport.getIcao()));
            }

            // Create panel with both the list and input field
            JPanel panel = new JPanel(new BorderLayout());
            
            // Add the airport list in a scrollable text area
            JTextArea textArea = new JTextArea(airportList.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(600, 300));
            
            // Add the input field for the key
            JTextField keyField = new JTextField(10);
            JPanel inputPanel = new JPanel();
            inputPanel.add(new JLabel("Enter airport key to delete:"));
            inputPanel.add(keyField);
            
            // Add components to main panel
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(inputPanel, BorderLayout.SOUTH);
            
            // Show the combined dialog
            int result = JOptionPane.showConfirmDialog(
                null, 
                panel, 
                "Delete Airport", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (result != JOptionPane.OK_OPTION) {
                return; // User cancelled
            }
            
            // Validate the entered key
            String keyText = keyField.getText();
            if (keyText == null || keyText.trim().isEmpty()) {
                throw new IllegalArgumentException("No key entered");
            }
            
            int key;
            try {
                key = Integer.parseInt(keyText.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid key format. Please enter a number.");
            }
            
            Airport airport = portDbase.getAirport(key);
            if (airport == null) {
                throw new IllegalArgumentException("Airport with key " + key + " not found");
            }

            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(
                null,
                "Are you sure you want to delete this airport?\n" + airport,
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                boolean deleted = portDbase.deleteAirport(key);
                if (deleted) {
                    JOptionPane.showMessageDialog(null, "Airport deleted successfully.");
                } else {
                    throw new IllegalStateException("Failed to delete airport");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Deletion cancelled.");
            }

        } catch (IllegalArgumentException e) {
            showErrorDialog("Deletion Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "An unexpected error occurred while deleting airport: " + e.getMessage());
        }
    }

    /**
     * Helper method to get descriptive fuel type string
     * @param fuelType The numeric fuel type code
     * @return Descriptive string for the fuel type
     */
    private String getFuelTypeDescription(int fuelType) {
        switch (fuelType) {
            case 1: return "AVGAS";
            case 2: return "JA-1 / JP-8";
            case 3: return "Both";
            default: return "Unknown";
        }
    }

    /**
     * Displays all airport records in a scrollable dialog
     */
    @SuppressWarnings("UseSpecificCatch")
    public void printAirportList() {
        try {
            Collection<Airport> airports = portDbase.getAllAirports();
            if (airports.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No airports in the database.");
                return;
            }

            // Build formatted list
            StringBuilder sb = new StringBuilder("Current Airport List:\n");
            for (Airport airport : airports) {
                sb.append(airport).append("\n\n");
            }

            // Display in scrollable pane
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
            JOptionPane.showMessageDialog(null, scrollPane, "Airport List", JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            showErrorDialog("Error", "An error occurred while retrieving airport list: " + e.getMessage());
        }
    }

    /**
     * Validates airport data against business rules with specific error messages
     * @param airport The airport to validate
     * @return true if valid, false otherwise
     */
    private boolean validateAirportData(Airport airport) {
        try {
            // Validate name
            if (airport.getName() == null || airport.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Airport name cannot be empty");
            }
            if (airport.getName().length() > 40) {
                throw new IllegalArgumentException("Airport name cannot exceed 40 characters");
            }
            
            // Validate ICAO
            if (airport.getIcao() == null) {
                throw new IllegalArgumentException("ICAO code cannot be null");
            }
            if (airport.getIcao().length() != 4) {
                throw new IllegalArgumentException("ICAO code must be exactly 4 characters");
            }
            
            // Validate coordinates
            if (airport.getLatitude() < -90) {
                throw new IllegalArgumentException("Latitude must be -90 or higher");
            }
            if (airport.getLatitude() > 90) {
                throw new IllegalArgumentException("Latitude must be 90 or lower");
            }
            if (airport.getLongitude() < -180) {
                throw new IllegalArgumentException("Longitude must be -180 or higher");
            }
            if (airport.getLongitude() > 180) {
                throw new IllegalArgumentException("Longitude must be 180 or lower");
            }
            
            // Validate fuel type
            if (airport.getFuelType() < 1) {
                throw new IllegalArgumentException("Fuel type must be 1 or higher");
            }
            if (airport.getFuelType() > 3) {
                throw new IllegalArgumentException("Fuel type must be 3 or lower");
            }
            
            // Validate radio type
            if (airport.getRadioType() == null || airport.getRadioType().trim().isEmpty()) {
                throw new IllegalArgumentException("Radio type cannot be empty");
            }
            if (!airport.getRadioType().equalsIgnoreCase("UHF") && 
                !airport.getRadioType().equalsIgnoreCase("VHF") && 
                !airport.getRadioType().equalsIgnoreCase("HF")) {
                throw new IllegalArgumentException("Radio type must be UHF, VHF, or HF");
            }
            
            // Validate radio frequency
            if (airport.getRadioFrequency() <= 0) {
                throw new IllegalArgumentException("Radio frequency must be positive");
            }
            
            // Validate frequency ranges based on radio type
            if (airport.getRadioType().equalsIgnoreCase("VHF")) {
                if (airport.getRadioFrequency() < 118.0) {
                    throw new IllegalArgumentException("VHF frequency must be 118.0 MHz or higher");
                }
                if (airport.getRadioFrequency() > 136.975) {
                    throw new IllegalArgumentException("VHF frequency must be 136.975 MHz or lower");
                }
            } else if (airport.getRadioType().equalsIgnoreCase("UHF")) {
                if (airport.getRadioFrequency() < 225) {
                    throw new IllegalArgumentException("UHF frequency must be 225 MHz or higher");
                }
                if (airport.getRadioFrequency() > 399.95) {
                    throw new IllegalArgumentException("UHF frequency must be 399.95 MHz or lower");
                }
            } else if (airport.getRadioType().equalsIgnoreCase("HF")) {
                if (airport.getRadioFrequency() < 2) {
                    throw new IllegalArgumentException("HF frequency must be 2 MHz or higher");
                }
                if (airport.getRadioFrequency() > 30) {
                    throw new IllegalArgumentException("HF frequency must be 30 MHz or lower");
                }
            }
            
            return true;
        } catch (IllegalArgumentException e) {
            showErrorDialog("Validation Error", e.getMessage());
            return false;
        }
    }

    /**
     * Shows input dialog with validation and specific error messages
     */
    private String showInputDialogWithValidation(String message, String errorMessage, 
            java.util.function.Predicate<String> validator) {
        while (true) {
            String input = JOptionPane.showInputDialog(message);
            if (input == null) {
                throw new IllegalArgumentException("Operation cancelled by user");
            }
            input = input.trim();
            
            if (input.isEmpty()) {
                showErrorDialog("Invalid Input", "Field cannot be blank");
                continue;
            }
            
            if (!validator.test(input)) {
                showErrorDialog("Invalid Input", errorMessage);
                continue;
            }
            
            return input;
        }
    }

    /**
     * Shows numeric input dialog with validation and specific error messages
     */
    private Double showNumericInputDialogWithValidation(String message, String errorMessage, 
            double min, double max, boolean allowEmpty) {
        while (true) {
            try {
                String input = JOptionPane.showInputDialog(message);
                if (input == null) {
                    throw new IllegalArgumentException("Operation cancelled by user");
                }
                
                if (allowEmpty && input.trim().isEmpty()) {
                    return null;
                }
                
                if (input.trim().isEmpty()) {
                    showErrorDialog("Invalid Input", "Field cannot be blank");
                    continue;
                }
                
                double value = Double.parseDouble(input);
                
                if (value < min) {
                    showErrorDialog("Invalid Input", "Value must be at least " + min);
                    continue;
                }
                
                if (value > max) {
                    showErrorDialog("Invalid Input", "Value must be at most " + max);
                    continue;
                }
                
                return value;
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Please enter a valid number");
            }
        }
    }

    /**
     * Shows integer input dialog with validation and specific error messages
     */
    private Integer showIntegerInputDialogWithValidation(String message, String errorMessage, 
            int min, int max, boolean allowEmpty) {
        while (true) {
            try {
                String input = JOptionPane.showInputDialog(message);
                if (input == null) {
                    throw new IllegalArgumentException("Operation cancelled by user");
                }
                
                if (allowEmpty && input.trim().isEmpty()) {
                    return null;
                }
                
                if (input.trim().isEmpty()) {
                    showErrorDialog("Invalid Input", "Field cannot be blank");
                    continue;
                }
                
                int value = Integer.parseInt(input);
                
                if (value < min) {
                    showErrorDialog("Invalid Input", "Value must be at least " + min);
                    continue;
                }
                
                if (value > max) {
                    showErrorDialog("Invalid Input", "Value must be at most " + max);
                    continue;
                }
                
                return value;
            } catch (NumberFormatException e) {
                showErrorDialog("Invalid Input", "Please enter a whole number");
            }
        }
    }

    /**
     * Displays error message dialog
     * @param title Dialog window title
     * @param message Error message content
     */
    public void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays main menu and handles user navigation
     */
    @SuppressWarnings("UseSpecificCatch")
    public void showMenu() {
        String[] options = {
            "Add Airport",
            "Search Airport",
            "Modify Airport",
            "Delete Airport",
            "Print Airport List",
            "Exit"
        };

        while (true) {
            try {
                int choice = JOptionPane.showOptionDialog(
                    null,
                    "Airport Management System",
                    "Main Menu",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
                );

                // Handle exit or menu selection
                if (choice == JOptionPane.CLOSED_OPTION || choice == 5) {
                    JOptionPane.showMessageDialog(null, "Exiting...");
                    return;
                }

                switch (choice) {
                    case 0 -> addAirport();
                    case 1 -> searchAirport();
                    case 2 -> modifyAirport();
                    case 3 -> deleteAirport();
                    case 4 -> printAirportList();
                    default -> JOptionPane.showMessageDialog(null, "Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                showErrorDialog("Error", "An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    /**
     * Entry point for the application
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AirportManager manager = new AirportManager();
            manager.showMenu();
        });
    }
}

/**
 * Represents an airport with its properties
 */
class Airport implements Serializable {
    // Serialization version control
    public static final long serialVersionUID = 1L;
    
    // Airport properties
    public String name;            // Full name of the airport
    public String icao;            // 4-letter ICAO code
    public double latitude;        // Geographic latitude coordinate
    public double longitude;       // Geographic longitude coordinate
    public int fuelType;           // Supported fuel types (1-3)
    public final int key;          // Unique identifier
    public String radioType;       // Type of radio communication
    public double radioFrequency;  // Primary radio frequency

    /**
     * Constructs new Airport instance
     */
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

    // Standard getters and setters
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

    /**
     * Returns formatted string representation
     */
    @Override
    public String toString() {
        return String.format(
            "Airport [Key: %d, Name: %s, ICAO: %s, Latitude: %.4f, Longitude: %.4f, " +
            "Fuel Type: %d, Radio Type: %s, Radio Frequency: %.4f]",
            key, name, icao, latitude, longitude, fuelType, radioType, radioFrequency
        );
    }
}

/**
 * In-memory database for airport records with persistent storage
 */
class AirportDatabase extends HashMap<String, Airport> {
    // Database storage
    public Map<Integer, Airport> airports = new HashMap<>();
    public int nextKey = 1;
    public static String DATA_FILE = "airports.dat";

    /**
     * Constructor loads existing data from file
     */
    public AirportDatabase() {
        loadAirports();
    }

    /**
     * Loads airports from persistent storage file
     */
    public void loadAirports() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                airports = (HashMap<Integer, Airport>) ois.readObject();
                // Set nextKey to highest existing key + 1
                nextKey = airports.keySet().stream().max(Integer::compare).orElse(0) + 1;
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Error loading airport data: " + e.getMessage(), 
                                           "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Saves current airport records to file
     */
    public void saveAirports() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(airports);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving airport data: " + e.getMessage(), 
                                       "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Adds new airport to database and saves
     * @param airport The airport to add
     */
    public void addAirport(Airport airport) {
        airports.put(airport.getKey(), airport);
        saveAirports();
    }

    /**
     * Retrieves airport by key
     * @param key The airport's unique key
     * @return The airport or null if not found
     */
    public Airport getAirport(int key) {
        return airports.get(key);
    }

    /**
     * Searches for airport by name or ICAO code
     * @param searchTerm The term to search for
     * @return Matching airport or null
     */
    public Airport searchAirport(String searchTerm) {
        return airports.values().stream()
            .filter(a -> a.getName().equalsIgnoreCase(searchTerm) || 
                        a.getIcao().equalsIgnoreCase(searchTerm))
            .findFirst()
            .orElse(null);
    }

    /**
     * Checks if airport name already exists
     * @param name The name to check
     * @return true if exists, false otherwise
     */
    public boolean airportNameExists(String name) {
        return airports.values().stream()
            .anyMatch(a -> a.getName().equalsIgnoreCase(name));
    }

    /**
     * Checks if ICAO code already exists
     * @param icao The ICAO code to check
     * @return true if exists, false otherwise
     */
    public boolean icaoExists(String icao) {
        return airports.values().stream()
            .anyMatch(a -> a.getIcao().equalsIgnoreCase(icao));
    }

    /**
     * Checks if coordinates already exist
     * @param latitude The latitude to check
     * @param longitude The longitude to check
     * @return true if exists, false otherwise
     */
    public boolean coordinatesExist(double latitude, double longitude) {
        return airports.values().stream()
            .anyMatch(a -> a.getLatitude() == latitude && a.getLongitude() == longitude);
    }

    /**
     * Updates existing airport record and saves
     * @param airport The airport with updated values
     */
    public void updateAirport(Airport airport) {
        airports.put(airport.getKey(), airport);
        saveAirports();
    }

    /**
     * Deletes airport by key and saves
     * @param key The airport's unique key
     * @return true if deleted, false if not found
     */
    public boolean deleteAirport(int key) {
        boolean removed = airports.remove(key) != null;
        if (removed) saveAirports();
        return removed;
    }

    /**
     * Gets all airports in database
     * @return Collection of all airports
     */
    public Collection<Airport> getAllAirports() {
        return airports.values();
    }

    /**
     * Gets next available key
     * @return The next auto-increment key
     */
    public int getNextKey() {
        return nextKey++;
    }
}