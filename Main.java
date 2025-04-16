import javax.swing.*;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Load databases from files
            Map<Integer, Airport> airports = loadAirportsFromFile("airports.dat");
            Map<Integer, Airplane> airplanes = loadAirplanesFromFile("airplanes.dat");
           
            if (airports == null || airplanes == null || airports.isEmpty() || airplanes.isEmpty()) {
                String message = "Error: Failed to load databases or databases are empty!\n" +
                               "Airports loaded: " + (airports != null ? airports.size() : 0) + "\n" +
                               "Airplanes loaded: " + (airplanes != null ? airplanes.size() : 0);
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
           
            new FlightPlannerGUI().showMainMenu(airports, airplanes);
        });
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Airport> loadAirportsFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "Airport database file not found: " + filename, 
                                       "Warning", JOptionPane.WARNING_MESSAGE);
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                return (Map<Integer, Airport>) obj;
            } else {
                JOptionPane.showMessageDialog(null, "Unexpected object type in airport file: " + obj.getClass(),
                                           "Error", JOptionPane.ERROR_MESSAGE);
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error loading airport database from " + filename + ": " + e.getMessage(),
                                       "Error", JOptionPane.ERROR_MESSAGE);
            return new HashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, Airplane> loadAirplanesFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "Airplane database file not found: " + filename,
                                       "Warning", JOptionPane.WARNING_MESSAGE);
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Object obj = ois.readObject();
            
            if (obj instanceof ArrayList) {
                ArrayList<Airplane> airplaneList = (ArrayList<Airplane>) obj;
                Map<Integer, Airplane> airplaneMap = new HashMap<>();
                for (Airplane airplane : airplaneList) {
                    airplaneMap.put(airplane.getKey(), airplane);
                }
                return airplaneMap;
            } else if (obj instanceof Map) {
                return (Map<Integer, Airplane>) obj;
            } else {
                JOptionPane.showMessageDialog(null, "Unexpected object type in airplane file: " + obj.getClass(),
                                           "Error", JOptionPane.ERROR_MESSAGE);
                return new HashMap<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Error loading airplane database from " + filename + ": " + e.getMessage(),
                                       "Error", JOptionPane.ERROR_MESSAGE);
            return new HashMap<>();
        }
    }
}

class FlightPlannerGUI {
    private static final String DISCLAIMER = 
        "DISCLAIMER:\n\n" +
        "This flight planning tool is for RECREATIONAL USE ONLY.\n" +
        "It should NOT be used for actual flight planning.\n\n" +
        "Always consult official aviation resources and professionals\n" +
        "for real-world flight planning.\n\n" +
        "By using this tool, you acknowledge that it provides\n" +
        "approximations only and should not be relied upon\n" +
        "for actual flight operations.";

    public void showMainMenu(Map<Integer, Airport> airports, Map<Integer, Airplane> airplanes) {
        while (true) {
            // Show disclaimer every time
            JOptionPane.showMessageDialog(null, DISCLAIMER, "Important Notice", JOptionPane.WARNING_MESSAGE);
            
            String[] options = {"Create Flight Plan", "Exit"};
            int choice = JOptionPane.showOptionDialog(null, 
                "Flight Planning System (Recreational Use Only)",
                "Main Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
            
            if (choice == 1 || choice == JOptionPane.CLOSED_OPTION) {
                JOptionPane.showMessageDialog(null, "Thank you for using the Flight Planner", "Goodbye", JOptionPane.INFORMATION_MESSAGE);
                return; // Exit program
            }
            
            // User chose to create flight plan
            createFlightPlan(airports, airplanes);
        }
    }

    public void createFlightPlan(Map<Integer, Airport> airports, Map<Integer, Airplane> airplanes) {
        if (airports.isEmpty() || airplanes.isEmpty()) {
            String message = "Error: Airports or Airplanes database is empty!\n" +
                           "Airports available: " + airports.size() + "\n" +
                           "Airplanes available: " + airplanes.size();
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Display available airports
        StringBuilder airportsList = new StringBuilder("Available Airports:\n");
        for (Airport airport : airports.values()) {
            airportsList.append("Key: ").append(airport.getKey())
                       .append(" | Name: ").append(airport.getName())
                       .append(" | ICAO: ").append(airport.getIcao())
                       .append("\n");
        }

        // Get departure airport
        Integer departureKey = getSelectionFromUser(airportsList.toString(), "Enter departure airport key:");
        if (departureKey == null) return;
        Airport departureAirport = airports.get(departureKey);
        if (departureAirport == null) {
            JOptionPane.showMessageDialog(null, "Invalid departure airport key!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create list of stops (starting with departure)
        List<Airport> route = new ArrayList<>();
        route.add(departureAirport);

        // Allow user to add intermediate stops
        while (true) {
            int response = JOptionPane.showConfirmDialog(null, 
                "Would you like to add an intermediate stop?", 
                "Add Stop", 
                JOptionPane.YES_NO_OPTION);
            
            if (response != JOptionPane.YES_OPTION) {
                break;
            }
            
            while (true) {
                Integer stopKey = getSelectionFromUser(airportsList.toString(), "Enter intermediate airport key:");
                if (stopKey == null) break;
                
                Airport stopAirport = airports.get(stopKey);
                if (stopAirport == null) {
                    JOptionPane.showMessageDialog(null, "Invalid airport key!", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                
                // Check if this is the same as the last airport in the route
                if (!route.isEmpty() && stopAirport.getKey() == route.get(route.size()-1).getKey()) {
                    JOptionPane.showMessageDialog(null, 
                        "Error: You cannot add the same airport consecutively!\n" +
                        "Current last stop: " + route.get(route.size()-1).getName() + 
                        " (" + route.get(route.size()-1).getIcao() + ")",
                        "Invalid Stop", 
                        JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                
                route.add(stopAirport);
                break;
            }
        }

        // Get destination airport
        while (true) {
            Integer destinationKey = getSelectionFromUser(airportsList.toString(), "Enter destination airport key:");
            if (destinationKey == null) return;
            
            Airport destinationAirport = airports.get(destinationKey);
            if (destinationAirport == null) {
                JOptionPane.showMessageDialog(null, "Invalid destination airport key!", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            // Check if destination is same as last stop
            if (!route.isEmpty() && destinationAirport.getKey() == route.get(route.size()-1).getKey()) {
                JOptionPane.showMessageDialog(null, 
                    "Error: Destination cannot be the same as your last stop!\n" +
                    "Current last stop: " + route.get(route.size()-1).getName() + 
                    " (" + route.get(route.size()-1).getIcao() + ")",
                    "Invalid Destination", 
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            route.add(destinationAirport);
            break;
        }

        // Display available airplanes
        StringBuilder airplanesList = new StringBuilder("Available Airplanes:\n");
        for (Airplane airplane : airplanes.values()) {
            airplanesList.append("Key: ").append(airplane.getKey())
                        .append(" | Make: ").append(airplane.getMake())
                        .append(" | Model: ").append(airplane.getModel())
                        .append(" | Type: ").append(airplane.getAircraftType())
                        .append("\n");
        }

        // Get selected airplane
        Integer airplaneKey = getSelectionFromUser(airplanesList.toString(), "Enter airplane key to use:");
        if (airplaneKey == null) return;
        Airplane airplane = airplanes.get(airplaneKey);
        if (airplane == null) {
            JOptionPane.showMessageDialog(null, "Invalid airplane key!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calculate flight details for each leg and totals
        double totalDistance = 0;
        double totalFlightTime = 0;
        StringBuilder legsInfo = new StringBuilder();
        
        for (int i = 0; i < route.size() - 1; i++) {
            Airport from = route.get(i);
            Airport to = route.get(i + 1);
            double distance = calculateDistance(from, to);
            double heading = calculateHeading(from, to);
            double flightTime = distance / airplane.getAirspeed();
            
            totalDistance += distance;
            totalFlightTime += flightTime;
            
            legsInfo.append(String.format("Leg %d: %s (%s) to %s (%s)\n", i+1, 
                from.getName(), from.getIcao(), to.getName(), to.getIcao()));
            legsInfo.append(String.format("  Distance: %.2f nm | Time: %.2f hours | Heading: %.1f°\n\n", 
                distance, flightTime, heading));
        }

        double fuelNeeded = totalFlightTime * airplane.getFuelBurn();

        // Build flight plan summary
        StringBuilder summary = new StringBuilder();
        summary.append("===== Flight Plan Summary =====\n");
        summary.append("Route:\n").append(legsInfo);
        summary.append("\nTOTALS:\n");
        summary.append(String.format("Total Distance: %.2f nautical miles%n", totalDistance));
        summary.append(String.format("Total Flight Time: %.2f hours%n", totalFlightTime));
        summary.append(String.format("Total Fuel Needed: %.2f liters%n", fuelNeeded));
        summary.append(String.format("Airplane Fuel Capacity: %.2f liters%n", airplane.getFuelSize()));

        if (fuelNeeded > airplane.getFuelSize()) {
            summary.append("WARNING: This airplane will require refueling during the flight.\n");
            summary.append(String.format("Additional fuel needed: %.2f liters%n", fuelNeeded - airplane.getFuelSize()));
            
            // Check fuel type compatibility for all stops
            for (Airport stop : route) {
                if ((airplane.getFuelType() == 1 && stop.getFuelType() != 1 && stop.getFuelType() != 3) ||
                    (airplane.getFuelType() == 2 && stop.getFuelType() != 2 && stop.getFuelType() != 3)) {
                    summary.append("WARNING: ").append(stop.getName())
                          .append(" doesn't have the required fuel type!\n");
                }
            }
        } else {
            summary.append("Flight can be completed without refueling.\n");
            summary.append(String.format("Remaining fuel after flight: %.2f liters%n", airplane.getFuelSize() - fuelNeeded));
        }
        summary.append("===============================");

        // Display summary in scrollable pane
        JTextArea textArea = new JTextArea(summary.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
        JOptionPane.showMessageDialog(null, scrollPane, "Flight Plan Summary", JOptionPane.INFORMATION_MESSAGE);

        // Ask user what to do next
        String[] options = {"Create Another Flight Plan", "Return to Main Menu"};
        int choice = JOptionPane.showOptionDialog(null, 
            "Flight plan completed. What would you like to do next?",
            "Flight Plan Complete",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        if (choice == 1) {
            return; // Return to main menu
        }
        // Otherwise, create another flight plan
        createFlightPlan(airports, airplanes);
    }

    private Integer getSelectionFromUser(String message, String prompt) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));
        
        Object[] components = {scrollPane, prompt};
        String input = JOptionPane.showInputDialog(null, components, "Flight Planner", JOptionPane.QUESTION_MESSAGE);
        
        if (input == null) return null; // User cancelled
        
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private double calculateDistance(Airport a1, Airport a2) {
        double lat1 = Math.toRadians(a1.getLatitude());
        double lon1 = Math.toRadians(a1.getLongitude());
        double lat2 = Math.toRadians(a2.getLatitude());
        double lon2 = Math.toRadians(a2.getLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.pow(Math.sin(dLat / 2), 2) + 
                   Math.cos(lat1) * Math.cos(lat2) * 
                   Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 3440.1 * c; // Earth radius in nautical miles
    }

    private double calculateHeading(Airport from, Airport to) {
        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon2 = Math.toRadians(to.getLongitude());

        double dLon = lon2 - lon1;

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - 
                   Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double heading = Math.toDegrees(Math.atan2(y, x));
        heading = (heading + 360) % 360; // Normalize to 0-360 degrees

        return heading;
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
    public String getIcao() { return icao; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getFuelType() { return fuelType; }
    public int getKey() { return key; }
    public String getRadioType() { return radioType; }
    public double getRadioFrequency() { return radioFrequency; }
}

class Airplane implements Serializable {
    private static final long serialVersionUID = -7817292208027958121L;
    private String make;
    private String model;
    private String aircraftType;
    private double fuelSize;
    private int fuelType;
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

    public String getMake() { return make; }
    public String getModel() { return model; }
    public String getAircraftType() { return aircraftType; }
    public double getFuelSize() { return fuelSize; }
    public int getFuelType() { return fuelType; }
    public double getFuelBurn() { return fuelBurn; }
    public int getAirspeed() { return airspeed; }
    public int getKey() { return key; }
}