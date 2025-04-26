// Import necessary libraries for GUI, file I/O, collections, and desktop operations
import java.awt.Desktop;
import java.io.*;
import java.util.*;
import javax.swing.*;

// Main class that serves as the entry point for the Flight Planner application
public class Main {
    // Main method - entry point of the application
    public static void main(String[] args) {
        // Use SwingUtilities to ensure GUI operations are performed on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Load airport and airplane databases from files
            Map<Integer, Airport> airports = loadAirportsFromFile("airports.dat");
            Map<Integer, Airplane> airplanes = loadAirplanesFromFile("airplanes.dat");
           
            // Check if databases loaded successfully and are not empty
            if (airports == null || airplanes == null || airports.isEmpty() || airplanes.isEmpty()) {
                // Create error message with loading statistics
                String message = "Error: Failed to load databases or databases are empty!\n" +
                               "Airports loaded: " + (airports != null ? airports.size() : 0) + "\n" +
                               "Airplanes loaded: " + (airplanes != null ? airplanes.size() : 0);
                // Show error dialog to user
                JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
                return; // Exit if databases couldn't be loaded
            }
           
            // Create and show the main GUI interface with loaded databases
            new FlightPlannerGUI().showMainMenu(airports, airplanes);
        });
    }

    // Method to load airports from a serialized file
    @SuppressWarnings("unchecked")
    private static Map<Integer, Airport> loadAirportsFromFile(String filename) {
        // Check if file exists
        File file = new File(filename);
        if (!file.exists()) {
            // Show warning if file not found
            JOptionPane.showMessageDialog(null, "Airport database file not found: " + filename, 
                                       "Warning", JOptionPane.WARNING_MESSAGE);
            return new HashMap<>(); // Return empty map
        }

        // Try-with-resources to ensure stream is closed properly
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            // Read the object from file
            Object obj = ois.readObject();
            // Check if object is of expected type
            if (obj instanceof Map) {
                return (Map<Integer, Airport>) obj; // Return the loaded map
            } else {
                // Show error if file contains unexpected type
                JOptionPane.showMessageDialog(null, "Unexpected object type in airport file: " + obj.getClass(),
                                           "Error", JOptionPane.ERROR_MESSAGE);
                return new HashMap<>(); // Return empty map
            }
        } catch (IOException | ClassNotFoundException e) {
            // Show error if loading fails
            JOptionPane.showMessageDialog(null, "Error loading airport database from " + filename + ": " + e.getMessage(),
                                       "Error", JOptionPane.ERROR_MESSAGE);
            return new HashMap<>(); // Return empty map
        }
    }

    // Method to load airplanes from a serialized file
    @SuppressWarnings("unchecked")
    private static Map<Integer, Airplane> loadAirplanesFromFile(String filename) {
        // Check if file exists
        File file = new File(filename);
        if (!file.exists()) {
            // Show warning if file not found
            JOptionPane.showMessageDialog(null, "Airplane database file not found: " + filename,
                                       "Warning", JOptionPane.WARNING_MESSAGE);
            return new HashMap<>(); // Return empty map
        }

        // Try-with-resources to ensure stream is closed properly
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            // Read the object from file
            Object obj = ois.readObject();
            
            // Handle both possible formats (ArrayList or Map)
            if (obj instanceof ArrayList) {
                // Convert ArrayList to Map if needed
                ArrayList<Airplane> airplaneList = (ArrayList<Airplane>) obj;
                Map<Integer, Airplane> airplaneMap = new HashMap<>();
                for (Airplane airplane : airplaneList) {
                    airplaneMap.put(airplane.getKey(), airplane);
                }
                return airplaneMap;
            } else if (obj instanceof Map) {
                // Return directly if already a Map
                return (Map<Integer, Airplane>) obj;
            } else {
                // Show error if file contains unexpected type
                JOptionPane.showMessageDialog(null, "Unexpected object type in airplane file: " + obj.getClass(),
                                           "Error", JOptionPane.ERROR_MESSAGE);
                return new HashMap<>(); // Return empty map
            }
        } catch (IOException | ClassNotFoundException e) {
            // Show error if loading fails
            JOptionPane.showMessageDialog(null, "Error loading airplane database from " + filename + ": " + e.getMessage(),
                                       "Error", JOptionPane.ERROR_MESSAGE);
            return new HashMap<>(); // Return empty map
        }
    }
}

// Class that handles all GUI interactions for flight planning
class FlightPlannerGUI {
    // Disclaimer constant shown to users
    private static final String DISCLAIMER = 
        "DISCLAIMER:\n\n" +
        "THIS SOFTWARE IS NOT TO BE USED FOR FLIGHT PLANNING OR NAVIGATIONAL PURPOSES\n";
        
    // Method to display the main menu and handle user choices
    public void showMainMenu(Map<Integer, Airport> airports, Map<Integer, Airplane> airplanes) {
        // Main menu loop
        while (true) {
            // Show disclaimer every time the menu is displayed
            JOptionPane.showMessageDialog(null, DISCLAIMER, "Important Notice", JOptionPane.WARNING_MESSAGE);
            
            // Menu options
            String[] options = {"Create Flight Plan", "Exit"};
            // Show option dialog to user
            int choice = JOptionPane.showOptionDialog(null, 
                "Flight Planning System (Recreational Use Only)",
                "Main Menu",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
            
            // Handle user choice
            if (choice == 1 || choice == JOptionPane.CLOSED_OPTION) {
                // Exit option chosen
                JOptionPane.showMessageDialog(null, "Thank you for using the Flight Planner", "Goodbye", JOptionPane.INFORMATION_MESSAGE);
                return; // Exit program
            }
            
            // User chose to create flight plan
            createFlightPlan(airports, airplanes);
        }
    }

    // Main method for creating a flight plan
    public void createFlightPlan(Map<Integer, Airport> airports, Map<Integer, Airplane> airplanes) {
        // Check if databases are empty
        if (airports.isEmpty() || airplanes.isEmpty()) {
            // Create error message with database statistics
            String message = "Error: Airports or Airplanes database is empty!\n" +
                           "Airports available: " + airports.size() + "\n" +
                           "Airplanes available: " + airplanes.size();
            JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Display available airports to user
        StringBuilder airportsList = new StringBuilder("Available Airports:\n");
        for (Airport airport : airports.values()) {
            airportsList.append("Key: ").append(airport.getKey())
                       .append(" | Name: ").append(airport.getName())
                       .append(" | ICAO: ").append(airport.getIcao())
                       .append("\n");
        }

        // Get departure airport selection from user
        Integer departureKey = getSelectionFromUser(airportsList.toString(), "Enter departure airport key:");
        if (departureKey == null) return; // User cancelled
        Airport departureAirport = airports.get(departureKey);
        if (departureAirport == null) {
            JOptionPane.showMessageDialog(null, "Invalid departure airport key!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create list to store the flight route (starting with departure)
        List<Airport> route = new ArrayList<>();
        route.add(departureAirport);

        // Allow user to add intermediate stops
        while (true) {
            // Ask user if they want to add a stop
            int response = JOptionPane.showConfirmDialog(null, 
                "Would you like to add an intermediate stop?", 
                "Add Stop", 
                JOptionPane.YES_NO_OPTION);
            
            // Break if user doesn't want to add more stops
            if (response != JOptionPane.YES_OPTION) {
                break;
            }
            
            // Loop for adding a single stop
            while (true) {
                // Get stop selection from user
                Integer stopKey = getSelectionFromUser(airportsList.toString(), "Enter intermediate airport key:");
                if (stopKey == null) break; // User cancelled
                
                // Validate selection
                Airport stopAirport = airports.get(stopKey);
                if (stopAirport == null) {
                    JOptionPane.showMessageDialog(null, "Invalid airport key!", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                
                // Check if same as last airport (not allowed)
                if (!route.isEmpty() && stopAirport.getKey() == route.get(route.size()-1).getKey()) {
                    JOptionPane.showMessageDialog(null, 
                        "Error: You cannot add the same airport consecutively!\n" +
                        "Current last stop: " + route.get(route.size()-1).getName() + 
                        " (" + route.get(route.size()-1).getIcao() + ")",
                        "Invalid Stop", 
                        JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                
                // Add valid stop to route
                route.add(stopAirport);
                break;
            }
        }

        // Get destination airport selection from user
        while (true) {
            // Get destination selection
            Integer destinationKey = getSelectionFromUser(airportsList.toString(), "Enter destination airport key:");
            if (destinationKey == null) return; // User cancelled
            
            // Validate selection
            Airport destinationAirport = airports.get(destinationKey);
            if (destinationAirport == null) {
                JOptionPane.showMessageDialog(null, "Invalid destination airport key!", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            // Check if destination is same as last stop (not allowed)
            if (!route.isEmpty() && destinationAirport.getKey() == route.get(route.size()-1).getKey()) {
                JOptionPane.showMessageDialog(null, 
                    "Error: Destination cannot be the same as your last stop!\n" +
                    "Current last stop: " + route.get(route.size()-1).getName() + 
                    " (" + route.get(route.size()-1).getIcao() + ")",
                    "Invalid Destination", 
                    JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            // Add valid destination to route
            route.add(destinationAirport);
            break;
        }

        // Display available airplanes to user
        StringBuilder airplanesList = new StringBuilder("Available Airplanes:\n");
        for (Airplane airplane : airplanes.values()) {
            airplanesList.append("Key: ").append(airplane.getKey())
                        .append(" | Make: ").append(airplane.getMake())
                        .append(" | Model: ").append(airplane.getModel())
                        .append(" | Type: ").append(airplane.getAircraftType())
                        .append("\n");
        }

        // Get airplane selection from user
        Integer airplaneKey = getSelectionFromUser(airplanesList.toString(), "Enter airplane key to use:");
        if (airplaneKey == null) return; // User cancelled
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
        
        // Calculate initial route legs (between all selected airports)
        for (int i = 0; i < route.size() - 1; i++) {
            Airport from = route.get(i);
            Airport to = route.get(i + 1);
            // Calculate distance, heading and flight time for this leg
            double distance = calculateDistance(from, to);
            double heading = calculateHeading(from, to);
            double flightTime = distance / airplane.getAirspeed();
            
            // Update totals
            totalDistance += distance;
            totalFlightTime += flightTime;
            averageHeading += heading;
            legCount++;
            
            // Add leg information to display string
            legsInfo.append(String.format("Leg %d: %s (%s) to %s (%s)\n", legCount, 
                from.getName(), from.getIcao(), to.getName(), to.getIcao()));
            legsInfo.append(String.format("  Distance: %.2f nm | Time: %.2f hours | Heading: %.1f°\n\n", 
                distance, flightTime, heading));
            
            fullRoute.add(from);
        }
        fullRoute.add(route.get(route.size()-1)); // Add final destination
        
        // Check if refueling stops are needed based on airplane range
        double maxLegDistance = airplane.getFuelSize() / airplane.getFuelBurn() * airplane.getAirspeed();
        List<Airport> routeWithRefuel = new ArrayList<>();
        routeWithRefuel.add(fullRoute.get(0));
        
        // Find and add necessary refueling stops
        for (int i = 1; i < fullRoute.size(); i++) {
            Airport prev = routeWithRefuel.get(routeWithRefuel.size()-1);
            Airport current = fullRoute.get(i);
            double legDistance = calculateDistance(prev, current);
            
            // Check if this leg exceeds maximum range
            if (legDistance > maxLegDistance) {
                // Find suitable refueling stops
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
        
        // Recalculate totals if refueling stops were added
        if (flightPossible && routeWithRefuel.size() > fullRoute.size()) {
            totalDistance = 0;
            totalFlightTime = 0;
            averageHeading = 0;
            legCount = 0;
            legsInfo = new StringBuilder();
            
            // Recalculate all legs including refueling stops
            for (int i = 0; i < routeWithRefuel.size() - 1; i++) {
                Airport from = routeWithRefuel.get(i);
                Airport to = routeWithRefuel.get(i + 1);
                double distance = calculateDistance(from, to);
                double heading = calculateHeading(from, to);
                double flightTime = distance / airplane.getAirspeed();
                
                // Update totals
                totalDistance += distance;
                totalFlightTime += flightTime;
                averageHeading += heading;
                legCount++;
                
                // Determine if this is a main route leg or refueling leg
                String legType = (i < route.size() && route.contains(from) && route.contains(to)) ? 
                    "Main Route" : "Refuel Stop";
                
                // Add leg information to display string
                legsInfo.append(String.format("%s Leg %d: %s (%s) to %s (%s)\n", legType, legCount,
                    from.getName(), from.getIcao(), to.getName(), to.getIcao()));
                legsInfo.append(String.format("  Distance: %.2f nm | Time: %.2f hours | Heading: %.1f°\n\n", 
                    distance, flightTime, heading));
            }
            averageHeading /= legCount; // Calculate average heading
        } else {
            averageHeading /= legCount; // Calculate average heading
        }

        // Calculate total fuel needed
        double fuelNeeded = totalFlightTime * airplane.getFuelBurn();

        // Build flight plan summary message
        StringBuilder summary = new StringBuilder();
        summary.append("===== Flight Plan Summary =====\n");
        summary.append("Route:\n").append(legsInfo);
        summary.append("\nTOTALS:\n");
        
        // Add flight impossibility message if applicable
        if (!flightPossible) {
            summary.append("FLIGHT IMPOSSIBLE: ").append(impossibilityReason).append("\n\n");
        } else {
            // Add flight statistics
            summary.append(String.format("Total Distance: %.2f nautical miles%n", totalDistance));
            summary.append(String.format("Total Flight Time: %.2f hours%n", totalFlightTime));
            summary.append(String.format("Average Heading: %.1f°%n", averageHeading));
            summary.append(String.format("Total Fuel Needed: %.2f liters%n", fuelNeeded));
            summary.append(String.format("Airplane Fuel Capacity: %.2f liters%n", airplane.getFuelSize()));

            // Add notes about refueling if applicable
            if (routeWithRefuel.size() > fullRoute.size()) {
                summary.append("\nNOTE: Refueling stops were automatically added to the route\n");
            }

            // Add fuel status warning/notification
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

        // Determine available options based on flight possibility
        String[] options;
        if (flightPossible) {
            options = new String[]{"View on Map", "Create Another Flight Plan", "Return to Main Menu"};
        } else {
            options = new String[]{"Create Another Flight Plan", "Return to Main Menu"};
        }
        
        // Ask user what to do next
        int choice = JOptionPane.showOptionDialog(null, 
            "Flight plan completed. What would you like to do next?",
            "Flight Plan Complete",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);
        
        // Handle user choice
        if (choice == 0 && flightPossible) {
            // User chose to view on map
            showFlightPlanOnOSM(routeWithRefuel);
            // Show post-plan options again
            showPostPlanOptions(airports, airplanes);
        } else if ((choice == 1 && flightPossible) || (choice == 0 && !flightPossible)) {
            // User chose to create another flight plan
            createFlightPlan(airports, airplanes);
        }
        // Otherwise, return to main menu
    }

    // Method to show options after flight plan is completed
    private void showPostPlanOptions(Map<Integer, Airport> airports, Map<Integer, Airplane> airplanes) {
        // Options for after viewing map
        String[] options = {"Create Another Flight Plan", "Return to Main Menu"};
        int choice = JOptionPane.showOptionDialog(null,
                "What would you like to do next?",
                "Flight Plan Complete",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
    
        // Handle user choice
        if (choice == 0) {
            createFlightPlan(airports, airplanes); // Create another plan
        }
        // Otherwise, return to main menu
    }

    // Method to show flight plan on OpenStreetMap in default browser
// Method to show flight plan on OpenStreetMap in default browser with connected path
// Method to show flight plan on OpenStreetMap in default browser
// Method to show flight plan in a custom HTML map
private void showFlightPlanOnOSM(List<Airport> route) {
    if (route == null || route.isEmpty()) {
        return;
    }

    try {
        // Create a temporary HTML file
        File htmlFile = File.createTempFile("flightplan", ".html");
        
        // Write the HTML content
        try (PrintWriter writer = new PrintWriter(htmlFile)) {
            writer.println(generateFlightPlanHTML(route));
        }
        
        // Open in default browser
        Desktop.getDesktop().browse(htmlFile.toURI());
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, 
            "Could not display flight plan: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}

// Method to generate HTML with flight path visualization
private String generateFlightPlanHTML(List<Airport> route) {
    StringBuilder sb = new StringBuilder();
    
    sb.append("<!DOCTYPE html>\n");
    sb.append("<html>\n");
    sb.append("<head>\n");
    sb.append("    <title>Flight Plan Visualization</title>\n");
    sb.append("    <meta charset=\"utf-8\">\n");
    sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
    sb.append("    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.css\"/>\n");
    sb.append("    <style>\n");
    sb.append("        #map { height: 100vh; width: 100%; }\n");
    sb.append("        body { margin: 0; padding: 0; }\n");
    sb.append("        .airport-info { font-weight: bold; }\n");
    sb.append("    </style>\n");
    sb.append("</head>\n");
    sb.append("<body>\n");
    sb.append("    <div id=\"map\"></div>\n");
    sb.append("    <script src=\"https://unpkg.com/leaflet@1.7.1/dist/leaflet.js\"></script>\n");
    sb.append("    <script>\n");
    sb.append("        // Airport data\n");
    sb.append("        const airports = [\n");
    
    // Add airport data
    for (Airport airport : route) {
        sb.append("            {\n");
        sb.append("                name: \"").append(escapeJavaScript(airport.getName())).append("\",\n");
        sb.append("                icao: \"").append(escapeJavaScript(airport.getIcao())).append("\",\n");
        sb.append("                lat: ").append(airport.getLatitude()).append(",\n");
        sb.append("                lng: ").append(airport.getLongitude()).append("\n");
        sb.append("            },\n");
    }
    
    sb.append("        ];\n");
    sb.append("\n");
    sb.append("        // Initialize map\n");
    sb.append("        const map = L.map('map').setView([airports[0].lat, airports[0].lng], 7);\n");
    sb.append("        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n");
    sb.append("            attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n");
    sb.append("        }).addTo(map);\n");
    sb.append("\n");
    sb.append("        // Add flight path\n");
    sb.append("        const flightPath = L.polyline(\n");
    sb.append("            airports.map(ap => [ap.lat, ap.lng]),\n");
    sb.append("            {color: 'red', weight: 3, dashArray: '10,10'}\n");
    sb.append("        ).addTo(map);\n");
    sb.append("\n");
    sb.append("        // Add markers for each airport\n");
    sb.append("        airports.forEach(ap => {\n");
    sb.append("            L.marker([ap.lat, ap.lng])\n");
    sb.append("                .addTo(map)\n");
    sb.append("                .bindPopup(`<div class=\"airport-info\">${ap.name}<br>${ap.icao}</div>`);\n");
    sb.append("        });\n");
    sb.append("\n");
    sb.append("        // Fit map to flight path\n");
    sb.append("        map.fitBounds(flightPath.getBounds());\n");
    sb.append("    </script>\n");
    sb.append("</body>\n");
    sb.append("</html>\n");
    
    return sb.toString();
}

// Helper method to escape strings for JavaScript
private String escapeJavaScript(String input) {
    if (input == null) return "";
    return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
}
    // Method to find suitable refueling stops between two airports
    private List<Airport> findRefuelStops(Airport from, Airport to, Map<Integer, Airport> airports, 
                                        Airplane airplane, double maxLegDistance) {
        List<Airport> refuelStops = new ArrayList<>();
        Airport current = from;
        double remainingDistance = calculateDistance(from, to);
        
        // Keep adding stops until remaining distance is within range
        while (remainingDistance > maxLegDistance) {
            Airport bestStop = null;
            double bestDistance = 0;
            
            // Search all airports for potential refuel stops
            for (Airport potentialStop : airports.values()) {
                // Skip current and destination airports
                if (potentialStop.getKey() == current.getKey() || potentialStop.getKey() == to.getKey()) {
                    continue;
                }
                
                // Check fuel compatibility
                if (!isFuelCompatible(airplane, potentialStop)) {
                    continue;
                }
                
                // Calculate distances
                double distToStop = calculateDistance(current, potentialStop);
                double distFromStopToDest = calculateDistance(potentialStop, to);
                
                // Check if this stop is within range and makes progress
                if (distToStop <= maxLegDistance && distFromStopToDest < remainingDistance) {
                    // Select the stop that's farthest along the route
                    if (bestStop == null || distToStop > bestDistance) {
                        bestStop = potentialStop;
                        bestDistance = distToStop;
                    }
                }
            }
            
            // If no suitable stop found, return empty list
            if (bestStop == null) {
                return Collections.emptyList();
            }
            
            // Add the best stop found
            refuelStops.add(bestStop);
            remainingDistance = calculateDistance(bestStop, to);
            current = bestStop;
        }
        
        return refuelStops;
    }

    // Method to check if airplane fuel type is compatible with airport fuel type
    private boolean isFuelCompatible(Airplane airplane, Airport airport) {
        int planeFuelType = airplane.getFuelType();
        int airportFuelType = airport.getFuelType();
        
        // Fuel type 3 means both types available
        return (planeFuelType == airportFuelType) || 
               (airportFuelType == 3) ||
               (planeFuelType == 1 && airportFuelType == 3) ||
               (planeFuelType == 2 && airportFuelType == 3);
    }

    // Helper method to get selection input from user with scrollable display
    private Integer getSelectionFromUser(String message, String prompt) {
        // Create scrollable text area for displaying options
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(500, 300));
        
        // Create input dialog with scroll pane and prompt
        Object[] components = {scrollPane, prompt};
        String input = JOptionPane.showInputDialog(null, components, "Flight Planner", JOptionPane.QUESTION_MESSAGE);
        
        if (input == null) return null; // User cancelled
        
        try {
            // Parse and return user input as integer
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            // Show error for invalid input
            JOptionPane.showMessageDialog(null, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Method to calculate distance between two airports using Haversine formula
    private double calculateDistance(Airport a1, Airport a2) {
        // Convert coordinates to radians
        double lat1 = Math.toRadians(a1.getLatitude());
        double lon1 = Math.toRadians(a1.getLongitude());
        double lat2 = Math.toRadians(a2.getLatitude());
        double lon2 = Math.toRadians(a2.getLongitude());

        // Calculate differences
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // Haversine formula
        double a = Math.pow(Math.sin(dLat / 2), 2) + 
                   Math.cos(lat1) * Math.cos(lat2) * 
                   Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 3440.1 * c; // Earth radius in nautical miles
    }

    // Method to calculate initial heading between two airports
    private double calculateHeading(Airport from, Airport to) {
        // Convert coordinates to radians
        double lat1 = Math.toRadians(from.getLatitude());
        double lon1 = Math.toRadians(from.getLongitude());
        double lat2 = Math.toRadians(to.getLatitude());
        double lon2 = Math.toRadians(to.getLongitude());

        // Calculate longitude difference
        double dLon = lon2 - lon1;

        // Calculate heading components
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - 
                   Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        // Calculate and normalize heading
        double heading = Math.toDegrees(Math.atan2(y, x));
        heading = (heading + 360) % 360; // Normalize to 0-360 degrees

        return heading;
    }
}

// Class representing an Airport with serialization support
class Airport implements Serializable {
    private static final long serialVersionUID = 1L; // Version control for serialization
    private String name;         // Airport name
    private String icao;         // ICAO code
    private double latitude;     // Latitude coordinate
    private double longitude;    // Longitude coordinate
    private int fuelType;        // Type of fuel available (1, 2, or 3 for both)
    private final int key;       // Unique identifier
    private String radioType;    // Type of radio available
    private double radioFrequency; // Radio frequency

    // Constructor
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

    // Getters for all fields
    public String getName() { return name; }
    public String getIcao() { return icao; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public int getFuelType() { return fuelType; }
    public int getKey() { return key; }
    public String getRadioType() { return radioType; }
    public double getRadioFrequency() { return radioFrequency; }
}

// Class representing an Airplane with serialization support
class Airplane implements Serializable {
    private static final long serialVersionUID = -7817292208027958121L; // Version control for serialization
    private String make;          // Manufacturer
    private String model;         // Model name
    private String aircraftType;  // Type of aircraft
    private double fuelSize;      // Fuel tank capacity (liters)
    private int fuelType;         // Type of fuel required (1 or 2)
    private double fuelBurn;      // Fuel consumption rate (liters/hour)
    private int airspeed;         // Cruising speed (knots)
    private final int key;        // Unique identifier

    // Constructor
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

    // Getters for all fields
    public String getMake() { return make; }
    public String getModel() { return model; }
    public String getAircraftType() { return aircraftType; }
    public double getFuelSize() { return fuelSize; }
    public int getFuelType() { return fuelType; }
    public double getFuelBurn() { return fuelBurn; }
    public int getAirspeed() { return airspeed; }
    public int getKey() { return key; }
}