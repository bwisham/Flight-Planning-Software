import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.Desktop;
import java.net.URI;

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
        "THIS SOFTWARE IS NOT TO BE USED FOR FLIGHT PLANNING OR NAVIGATIONAL PURPOSES\n";
        

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
        double averageHeading = 0;
        int legCount = 0;
        StringBuilder legsInfo = new StringBuilder();
        List<Airport> fullRoute = new ArrayList<>();
        boolean flightPossible = true;
        String impossibilityReason = "";
        
        // Calculate initial route legs
        for (int i = 0; i < route.size() - 1; i++) {
            Airport from = route.get(i);
            Airport to = route.get(i + 1);
            double distance = calculateDistance(from, to);
            double heading = calculateHeading(from, to);
            double flightTime = distance / airplane.getAirspeed();
            
            totalDistance += distance;
            totalFlightTime += flightTime;
            averageHeading += heading;
            legCount++;
            
            legsInfo.append(String.format("Leg %d: %s (%s) to %s (%s)\n", legCount, 
                from.getName(), from.getIcao(), to.getName(), to.getIcao()));
            legsInfo.append(String.format("  Distance: %.2f nm | Time: %.2f hours | Heading: %.1f°\n\n", 
                distance, flightTime, heading));
            
            fullRoute.add(from);
        }
        fullRoute.add(route.get(route.size()-1)); // Add final destination
        
        // Check if refueling stops are needed and add them
        double maxLegDistance = airplane.getFuelSize() / airplane.getFuelBurn() * airplane.getAirspeed();
        List<Airport> routeWithRefuel = new ArrayList<>();
        routeWithRefuel.add(fullRoute.get(0));
        
        for (int i = 1; i < fullRoute.size(); i++) {
            Airport prev = routeWithRefuel.get(routeWithRefuel.size()-1);
            Airport current = fullRoute.get(i);
            double legDistance = calculateDistance(prev, current);
            
            if (legDistance > maxLegDistance) {
                // Need to find refueling stops
                List<Airport> refuelStops = findRefuelStops(prev, current, airports, airplane, maxLegDistance);
                if (refuelStops.isEmpty()) {
                    flightPossible = false;
                    impossibilityReason = String.format(
                        "No suitable refueling airports between %s and %s (distance: %.1f nm, max range: %.1f nm)",
                        prev.getName(), current.getName(), legDistance, maxLegDistance);
                    break;
                }
                routeWithRefuel.addAll(refuelStops);
            }
            routeWithRefuel.add(current);
        }
        
        // Recalculate totals if we added refueling stops
        if (flightPossible && routeWithRefuel.size() > fullRoute.size()) {
            totalDistance = 0;
            totalFlightTime = 0;
            averageHeading = 0;
            legCount = 0;
            legsInfo = new StringBuilder();
            
            for (int i = 0; i < routeWithRefuel.size() - 1; i++) {
                Airport from = routeWithRefuel.get(i);
                Airport to = routeWithRefuel.get(i + 1);
                double distance = calculateDistance(from, to);
                double heading = calculateHeading(from, to);
                double flightTime = distance / airplane.getAirspeed();
                
                totalDistance += distance;
                totalFlightTime += flightTime;
                averageHeading += heading;
                legCount++;
                
                String legType = (i < route.size() && route.contains(from) && route.contains(to)) ? 
                    "Main Route" : "Refuel Stop";
                
                legsInfo.append(String.format("%s Leg %d: %s (%s) to %s (%s)\n", legType, legCount,
                    from.getName(), from.getIcao(), to.getName(), to.getIcao()));
                legsInfo.append(String.format("  Distance: %.2f nm | Time: %.2f hours | Heading: %.1f°\n\n", 
                    distance, flightTime, heading));
            }
            averageHeading /= legCount;
        } else {
            averageHeading /= legCount;
        }

        double fuelNeeded = totalFlightTime * airplane.getFuelBurn();

        // Build flight plan summary
        StringBuilder summary = new StringBuilder();
        summary.append("===== Flight Plan Summary =====\n");
        summary.append("Route:\n").append(legsInfo);
        summary.append("\nTOTALS:\n");
        
        if (!flightPossible) {
            summary.append("FLIGHT IMPOSSIBLE: ").append(impossibilityReason).append("\n\n");
        } else {
            summary.append(String.format("Total Distance: %.2f nautical miles%n", totalDistance));
            summary.append(String.format("Total Flight Time: %.2f hours%n", totalFlightTime));
            summary.append(String.format("Average Heading: %.1f°%n", averageHeading));
            summary.append(String.format("Total Fuel Needed: %.2f liters%n", fuelNeeded));
            summary.append(String.format("Airplane Fuel Capacity: %.2f liters%n", airplane.getFuelSize()));

            if (routeWithRefuel.size() > fullRoute.size()) {
                summary.append("\nNOTE: Refueling stops were automatically added to the route\n");
            }

            if (fuelNeeded > airplane.getFuelSize()) {
                summary.append("WARNING: This flight requires refueling stops (already added to route)\n");
            } else {
                summary.append("Flight can be completed without refueling.\n");
                summary.append(String.format("Remaining fuel after flight: %.2f liters%n", airplane.getFuelSize() - fuelNeeded));
            }
        }
        summary.append("===============================");

        // Display summary in scrollable pane
        JTextArea textArea = new JTextArea(summary.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
        JOptionPane.showMessageDialog(null, scrollPane, "Flight Plan Summary", JOptionPane.INFORMATION_MESSAGE);

        // Ask user what to do next
         // Create buttons
         String[] options;
         if (flightPossible) {
             options = new String[]{"View on Map", "Create Another Flight Plan", "Return to Main Menu"};
         } else {
             options = new String[]{"Create Another Flight Plan", "Return to Main Menu"};
         }
        //String[] options = {"Create Another Flight Plan", "Return to Main Menu"};
        int choice = JOptionPane.showOptionDialog(null, 
            "Flight plan completed. What would you like to do next?",
            "Flight Plan Complete",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
            if (choice == 0 && flightPossible) {
                // User clicked "View on Map"
                showFlightPlanOnOSM(routeWithRefuel);
                // Show the options again after viewing the map
                showPostPlanOptions(airports, airplanes);
            } else if ((choice == 1 && flightPossible) || (choice == 0 && !flightPossible)) {
                // Create another flight plan
                createFlightPlan(airports, airplanes);
            } else {
                // Return to main menu
                return;
            }// Return to main menu
        }
        // Otherwise, create another flight plan
        //createFlightPlan(airports, airplanes);
        private void showPostPlanOptions(Map<Integer, Airport> airports, Map<Integer, Airplane> airplanes) {
            String[] options = {"Create Another Flight Plan", "Return to Main Menu"};
            int choice = JOptionPane.showOptionDialog(null,
                    "What would you like to do next?",
                    "Flight Plan Complete",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
    
            if (choice == 0) {
                createFlightPlan(airports, airplanes);
            }
        }
    
        private void showFlightPlanOnOSM(List<Airport> route) {
            if (route == null || route.isEmpty()) {
                return;
            }
    
            try {
                // Build the OpenStreetMap URL with the flight path
                StringBuilder urlBuilder = new StringBuilder("https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=");
                
                // Add all airports in the route
                for (Airport airport : route) {
                    urlBuilder.append(airport.getLatitude())
                             .append(",")
                             .append(airport.getLongitude())
                             .append(";");
                }
                
                // Remove the trailing semicolon
                String url = urlBuilder.substring(0, urlBuilder.length() - 1);
                
                // Open in default browser
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, 
                    "Could not open map in browser: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }

    private List<Airport> findRefuelStops(Airport from, Airport to, Map<Integer, Airport> airports, 
                                        Airplane airplane, double maxLegDistance) {
        List<Airport> refuelStops = new ArrayList<>();
        Airport current = from;
        double remainingDistance = calculateDistance(from, to);
        
        while (remainingDistance > maxLegDistance) {
            Airport bestStop = null;
            double bestDistance = 0;
            
            for (Airport potentialStop : airports.values()) {
                // Skip if same as current or destination
                if (potentialStop.getKey() == current.getKey() || potentialStop.getKey() == to.getKey()) {
                    continue;
                }
                
                // Check fuel compatibility
                if (!isFuelCompatible(airplane, potentialStop)) {
                    continue;
                }
                
                double distToStop = calculateDistance(current, potentialStop);
                double distFromStopToDest = calculateDistance(potentialStop, to);
                
                if (distToStop <= maxLegDistance && distFromStopToDest < remainingDistance) {
                    if (bestStop == null || distToStop > bestDistance) {
                        bestStop = potentialStop;
                        bestDistance = distToStop;
                    }
                }
            }
            
            if (bestStop == null) {
                return Collections.emptyList(); // No suitable stop found
            }
            
            refuelStops.add(bestStop);
            remainingDistance = calculateDistance(bestStop, to);
            current = bestStop;
        }
        
        return refuelStops;
    }

    private boolean isFuelCompatible(Airplane airplane, Airport airport) {
        int planeFuelType = airplane.getFuelType();
        int airportFuelType = airport.getFuelType();
        
        // Fuel type 3 means both types available
        return (planeFuelType == airportFuelType) || 
               (airportFuelType == 3) ||
               (planeFuelType == 1 && airportFuelType == 3) ||
               (planeFuelType == 2 && airportFuelType == 3);
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