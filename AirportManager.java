import java.io.*;
import java.util.*;
import javax.swing.*;

public class AirportManager {
    public AirportDatabase portDbase;

    public AirportManager() {
        this.portDbase = new AirportDatabase();
    }

    @SuppressWarnings("UseSpecificCatch")
    public void addAirport() {
        try {
            String name = showInputDialogWithValidation(
                "Enter the name of the airport:",
                "Airport name cannot be empty and must be less than 40 characters.",
                input -> input != null && !input.trim().isEmpty() && input.length() <= 40
            );

            if (portDbase.airportNameExists(name)) {
                throw new IllegalArgumentException("An airport with this name already exists.");
            }

            String icao = showInputDialogWithValidation(
                "Enter the ICAO Identifier of the airport (4 characters):",
                "ICAO must be exactly 4 characters.",
                input -> input != null && input.length() == 4
            ).toUpperCase();

            if (portDbase.icaoExists(icao)) {
                throw new IllegalArgumentException("An airport with this ICAO code already exists.");
            }

            @SuppressWarnings("UnnecessaryUnboxing")
            double latitude = showNumericInputDialogWithValidation(
                "Enter the latitude of the airport (-90 to 90):",
                "Invalid latitude. Must be between -90 and 90.",
                -90.0, 90.0,
                false
            ).doubleValue();

            @SuppressWarnings("UnnecessaryUnboxing")
            double longitude = showNumericInputDialogWithValidation(
                "Enter the longitude of the airport (-180 to 180):",
                "Invalid longitude. Must be between -180 and 180.",
                -180.0, 180.0,
                false
            ).doubleValue();

            if (portDbase.coordinatesExist(latitude, longitude)) {
                throw new IllegalArgumentException("An airport already exists at these exact coordinates.");
            }

            int fuelType = showNumericInputDialogWithValidation(
                "Enter the fuel types the airport supports (1-3). 1=AVGAS, 2=JA-1 / JP-8, 3 = Both.:",
                "Invalid fuel type. Must be between 1 and 3.",
                1, 3
            );

            String radioType = showInputDialogWithValidation(
                "Enter the radio type (e.g., VHF, UHF, HF):",
                "Radio type cannot be empty and must be less than 20 characters.",
                input -> input != null && !input.trim().isEmpty() && input.length() <= 20
            );

            @SuppressWarnings("UnnecessaryUnboxing")
            double radioFrequency = showNumericInputDialogWithValidation(
                "Enter the radio frequency (e.g., 118.00 - 136.975 for VHF):",
                "Invalid radio frequency. Must be positive.",
                0.1, Double.MAX_VALUE,
                false
            ).doubleValue();

            int key = portDbase.getNextKey();
            Airport airport = new Airport(name, icao, latitude, longitude, fuelType, key, radioType, radioFrequency);

            if (!validateAirportData(airport)) {
                throw new IllegalArgumentException("Airport data validation failed");
            }

            portDbase.addAirport(airport);
            JOptionPane.showMessageDialog(null, "Airport added successfully with key: " + key);

        } catch (IllegalArgumentException e) {
            showErrorDialog("Input Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "An unexpected error occurred while adding airport: " + e.getMessage());
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public void searchAirport() {
        try {
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

            String searchTerm = showInputDialogWithValidation(
                choice == 0 ? "Enter airport name:" : "Enter ICAO identifier (4 characters):",
                "Search term cannot be empty",
                input -> input != null && !input.trim().isEmpty()
            );

            if (choice == 1 && searchTerm.length() != 4) {
                throw new IllegalArgumentException("ICAO must be exactly 4 characters");
            }

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

    @SuppressWarnings({"UnnecessaryUnboxing", "UseSpecificCatch"})
    public void modifyAirport() {
        try {
            Integer key = showNumericInputDialogWithValidation(
                "Enter key of airport to modify:",
                "Invalid airport key",
                1, Integer.MAX_VALUE
            );

            Airport airport = portDbase.getAirport(key);
            if (airport == null) {
                throw new IllegalArgumentException("Airport with key " + key + " not found");
            }

            JOptionPane.showMessageDialog(null, "Current airport details:\n" + airport);

            String name = showInputDialogWithValidation(
                "Enter new name (leave blank to keep current):",
                "Airport name cannot be empty and must be less than 40 characters.",
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

            String icao = showInputDialogWithValidation(
                "Enter new ICAO (leave blank to keep current):",
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

            Double latitude = showNumericInputDialogWithValidation(
                "Enter new latitude (or 0 to keep current):",
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

            Double longitude = showNumericInputDialogWithValidation(
                "Enter new longitude (or 0 to keep current):",
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

            Integer fuelType = showIntegerInputDialogWithValidation(
                "Enter new fuel type (or 0 to keep current, 1-3) 1=AVGAS, 2=JA-1 / JP-8, 3 = Both.:",
                "Invalid fuel type. Must be between 1 and 3.",
                1, 3,
                true
            );
            if (fuelType != null && fuelType != 0) {
                airport.setFuelType(fuelType);
            }

            String radioType = showInputDialogWithValidation(
                "Enter new radio type (leave blank to keep current):",
                "Radio type must be less than 20 characters.",
                input -> input.isEmpty() || input.length() <= 20
            );
            if (!radioType.isEmpty()) {
                airport.setRadioType(radioType);
            }

            Double radioFrequency = showNumericInputDialogWithValidation(
                "Enter new radio frequency (or 0 to keep current, 118.00-136.975):",
                "Invalid radio frequency. Must be positive.",
                0.1, Double.MAX_VALUE,
                true
            );
            if (radioFrequency != null && radioFrequency != 0) {
                airport.setRadioFrequency(radioFrequency);
            }

            if (!validateAirportData(airport)) {
                throw new IllegalArgumentException("Modified airport data validation failed");
            }

            portDbase.updateAirport(airport);
            JOptionPane.showMessageDialog(null, "Airport updated successfully.");

        } catch (IllegalArgumentException e) {
            showErrorDialog("Modification Error", e.getMessage());
        } catch (Exception e) {
            showErrorDialog("Error", "An unexpected error occurred while modifying airport: " + e.getMessage());
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public void deleteAirport() {
        try {
            Integer key = showNumericInputDialogWithValidation(
                "Enter key of airport to delete:",
                "Invalid airport key",
                1, Integer.MAX_VALUE
            );

            Airport airport = portDbase.getAirport(key);
            if (airport == null) {
                throw new IllegalArgumentException("Airport with key " + key + " not found");
            }

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

    @SuppressWarnings("UseSpecificCatch")
    public void printAirportList() {
        try {
            Collection<Airport> airports = portDbase.getAllAirports();
            if (airports.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No airports in the database.");
                return;
            }

            StringBuilder sb = new StringBuilder("Current Airport List:\n");
            for (Airport airport : airports) {
                sb.append(airport).append("\n\n");
            }

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
            JOptionPane.showMessageDialog(null, scrollPane, "Airport List", JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            showErrorDialog("Error", "An error occurred while retrieving airport list: " + e.getMessage());
        }
    }

    private boolean validateAirportData(Airport airport) {
        try {
            if (airport.getName() == null || airport.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Airport name cannot be empty");
            }
            if (airport.getName().length() > 40) {
                throw new IllegalArgumentException("Name cannot exceed 40 characters");
            }
            if (airport.getIcao() == null || airport.getIcao().length() != 4) {
                throw new IllegalArgumentException("ICAO code must be exactly 4 characters");
            }
            if (airport.getLatitude() < -90 || airport.getLatitude() > 90) {
                throw new IllegalArgumentException("Latitude must be between -90 and 90");
            }
            if (airport.getLongitude() < -180 || airport.getLongitude() > 180) {
                throw new IllegalArgumentException("Longitude must be between -180 and 180");
            }
            if (airport.getFuelType() < 1 || airport.getFuelType() > 5) {
                throw new IllegalArgumentException("Fuel type must be between 1 and 5");
            }
            return true;
        } catch (IllegalArgumentException e) {
            showErrorDialog("Validation Error", e.getMessage());
            return false;
        }
    }

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

    private Integer showIntegerInputDialogWithValidation(String message, String errorMessage, int min, int max, boolean allowEmpty) {
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

    public int showNumericInputDialogWithValidation(String message, String errorMessage, int min, int max) {
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
                showErrorDialog("Invalid Input", "Please enter a valid number");
            }
        }
    }

    public void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AirportManager manager = new AirportManager();
            manager.showMenu();
        });
    }
}

class Airport implements Serializable {
    public static final long serialVersionUID = 1L;
    public String name;
    public String icao;
    public double latitude;
    public double longitude;
    public int fuelType;
    public final int key;
    public String radioType;
    public double radioFrequency;

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
}

class AirportDatabase extends HashMap<String, Airport> {
    public Map<Integer, Airport> airports = new HashMap<>();
    public int nextKey = 1;
    public static String DATA_FILE = "airports.dat";

    public AirportDatabase() {
        loadAirports();
    }

    public void loadAirports() {
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

    public void saveAirports() {
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

    public boolean icaoExists(String icao) {
        return airports.values().stream()
            .anyMatch(a -> a.getIcao().equalsIgnoreCase(icao));
    }

    public boolean coordinatesExist(double latitude, double longitude) {
        return airports.values().stream()
            .anyMatch(a -> a.getLatitude() == latitude && a.getLongitude() == longitude);
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