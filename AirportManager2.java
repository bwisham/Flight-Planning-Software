import java.io.*;
import java.util.*;
import javax.swing.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AirportManager {
    private final AirportDatabase portDbase;

    public AirportManager() {
        this.portDbase = new AirportDatabase(); // Active database from AirportsDB.xlsx
    }

    @SuppressWarnings("UseSpecificCatch")
    public void addAirport() {
        try {
            String name = showInputDialogWithValidation(
                "Enter the name of the airport:",
                "Airport name cannot be empty and must be less than 40 characters. It also must be letters only.",
                input -> input != null && input.matches("[A-Za-z ]+") && input.length() <= 40
            );

            if (portDbase.airportNameExists(name)) {
                throw new IllegalArgumentException("An airport with this name already exists.");
            }

            String icao = showInputDialogWithValidation(
                "Enter the ICAO Identifier of the airport (4 letters):",
                "ICAO must be exactly 4 alphabetic characters.",
                input -> input != null && input.matches("[A-Za-z]{4}")
            ).toUpperCase();

            double latitude = showNumericInputDialogWithValidation(
                "Enter the latitude of the airport (-90 to 90):",
                "Invalid latitude. Must be between -90 and 90.",
                -90.0, 90.0,
                false
            ).doubleValue();

            double longitude = showNumericInputDialogWithValidation(
                "Enter the longitude of the airport (-180 to 180):",
                "Invalid longitude. Must be between -180 and 180.",
                -180.0, 180.0,
                false
            ).doubleValue();

            int fuelType = showNumericInputDialogWithValidation(
                "Enter the fuel type the airport supports (1=AVGAS, 2=JA-1 / JP-8, 3=Both):",
                "Invalid fuel type. Must be 1, 2, or 3.",
                1, 3
            );

            String radioType = showInputDialogWithValidation(
                "Enter the radio type (e.g., VHF, UHF, HF):",
                "Radio type cannot be empty and must be less than 20 characters.",
                input -> input != null && !input.trim().isEmpty() && input.length() <= 20
            );

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
                choice == 0 ? "Enter airport name:" : "Enter ICAO identifier (4 letters):",
                "Search term cannot be empty",
                input -> input != null && !input.trim().isEmpty()
            );

            if (choice == 1 && !searchTerm.matches("[A-Za-z]{4}")) {
                throw new IllegalArgumentException("ICAO must be exactly 4 alphabetic characters");
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

            Airport airport = portDbase.getAirportByKey(key);
            if (airport == null) {
                throw new IllegalArgumentException("Airport with key " + key + " not found");
            }

            JOptionPane.showMessageDialog(null, "Current airport details:\n" + airport);

            String name = showInputDialogWithValidation(
                "Enter new name (leave blank to keep current):",
                "Airport name cannot be empty and must be less than 40 characters. It also must be letters only.",
                input -> true // allow empty input to keep current value
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
                "ICAO must be exactly 4 alphabetic characters",
                input -> input.isEmpty() || input.matches("[A-Za-z]{4}")
            );
            if (!icao.isEmpty()) {
                airport.setIcao(icao.toUpperCase());
            }

            Double latitude = showNumericInputDialogWithValidation(
                "Enter new latitude (or 0 to keep current):",
                "Invalid latitude. Must be between -90 and 90.",
                -90.0, 90.0,
                true
            );
            if (latitude != null && latitude != 0) {
                airport.setLatitude(latitude);
            }

            Double longitude = showNumericInputDialogWithValidation(
                "Enter new longitude (or 0 to keep current):",
                "Invalid longitude. Must be between -180 and 180.",
                -180.0, 180.0,
                true
            );
            if (longitude != null && longitude != 0) {
                airport.setLongitude(longitude);
            }

            Integer fuelType = showIntegerInputDialogWithValidation(
                "Enter new fuel type (or 0 to keep current, 1-3):",
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
                "Enter new radio frequency (or 0 to keep current):",
                "Invalid radio frequency. Must be positive.",
                0.1, Double.MAX_VALUE,
                true
            ).doubleValue();
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

            Airport airport = portDbase.getAirportByKey(key);
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
            if (!airport.getName().matches("[A-Za-z ]+")) {
                throw new IllegalArgumentException("Name must contain only letters and spaces");
            }
            if (airport.getName().length() > 40) {
                throw new IllegalArgumentException("Name cannot exceed 40 characters");
            }
            if (airport.getIcao() == null || !airport.getIcao().matches("[A-Z]{4}")) {
                throw new IllegalArgumentException("ICAO code must be exactly 4 uppercase letters");
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
                showErrorDialog("Invalid Input", "Please enter a valid number");
            }
        }
    }

    private void showErrorDialog(String title, String message) {
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
}

class AirportDatabase {
    // This database uses only the active Excel file "AirportsDB.xlsx" as the source.
    private Map<Integer, Airport> airports = new HashMap<>();
    private int nextKey = 1;
    private static final String EXCEL_FILE = "AirportsDB.xlsx";

    public AirportDatabase() {
        File excelFile = new File(EXCEL_FILE);
        if (excelFile.exists()) {
            loadAirportsFromExcel(EXCEL_FILE);
        } else {
            // If the Excel file does not exist, we initialize an empty database
            // and immediately write the header structure to a new Excel file.
            saveAirportsToExcel(EXCEL_FILE);
        }
    }
    
    private void loadAirportsFromExcel(String excelPath) {
        try (FileInputStream fis = new FileInputStream(new File(excelPath));
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            // Assume first row is header
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                String name = row.getCell(0).getStringCellValue();
                String icao = row.getCell(1).getStringCellValue();
                double latitude = row.getCell(2).getNumericCellValue();
                double longitude = row.getCell(3).getNumericCellValue();
                int fuelType = (int) row.getCell(4).getNumericCellValue();
                String radioType = row.getCell(5).getStringCellValue();
                double radioFrequency = row.getCell(6).getNumericCellValue();
                int key = getNextKey();
                Airport airport = new Airport(name, icao.toUpperCase(), latitude, longitude, fuelType, key, radioType, radioFrequency);
                airports.put(key, airport);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading Excel file: " + e.getMessage(), 
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveAirportsToExcel(String excelPath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Airports");
            // Create header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("ICAO");
            header.createCell(2).setCellValue("Latitude");
            header.createCell(3).setCellValue("Longitude");
            header.createCell(4).setCellValue("FuelType");
            header.createCell(5).setCellValue("RadioType");
            header.createCell(6).setCellValue("RadioFrequency");
            int rowIndex = 1;
            for (Airport airport : airports.values()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(airport.getName());
                row.createCell(1).setCellValue(airport.getIcao());
                row.createCell(2).setCellValue(airport.getLatitude());
                row.createCell(3).setCellValue(airport.getLongitude());
                row.createCell(4).setCellValue(airport.getFuelType());
                row.createCell(5).setCellValue(airport.getRadioType());
                row.createCell(6).setCellValue(airport.getRadioFrequency());
            }
            try (FileOutputStream fos = new FileOutputStream(new File(excelPath))) {
                workbook.write(fos);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error saving to Excel file: " + e.getMessage(), 
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveAirports() {
        // Always update the Excel file as the active database.
        saveAirportsToExcel(EXCEL_FILE);
    }
    
    public void addAirport(Airport airport) {
        airports.put(airport.getKey(), airport);
        saveAirports();
    }
    
    public Airport getAirportByKey(int key) {
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
        if (removed) {
            saveAirports();
        }
        return removed;
    }
    
    public Collection<Airport> getAllAirports() {
        return airports.values();
    }
    
    // Additional methods to support PlanningSoftware
    public int size() {
        return airports.size();
    }
    
    public Airport get(int index) {
        List<Airport> list = new ArrayList<>(airports.values());
        if (index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return null;
    }
    
    public int getNextKey() {
        return nextKey++;
    }
}
