import java.util.*;
import java.io.*;

public class PlanningSoftware {

    private AirportDatabase portDbase;
    private AirplaneDatabase planeDbase; // Assume AirplaneDatabase is defined elsewhere

    /**
     * Constructor: uses the active Excel database for airports.
     */
    public PlanningSoftware() {
        portDbase = new AirportDatabase(); // Uses "AirportsDB.xlsx"
        planeDbase = new AirplaneDatabase();
    }

    /**
     * Performs the flight planning process.
     */
    public void flightPlanning() {
        Scanner scanner = new Scanner(System.in);
        
        // Use an ArrayList to store the route (sequence of airports)
        List<Airport> route = new ArrayList<>();
        String str1, str2, str3;
        Airport port;
        
        // Prompt for airplane details
        System.out.print("Enter the make of the airplane: ");
        String make = scanner.nextLine();
        System.out.print("Enter the model of the airplane: ");
        String model = scanner.nextLine();
        System.out.print("Enter the type of the airplane: ");
        String type = scanner.nextLine();
        
        Airplane plane = planeDbase.search(make, model, type);

        // Prompt for starting airport using ICAO identifier
        System.out.print("Enter ICAO identifier of starting airport: ");
        str1 = scanner.nextLine();
        port = portDbase.searchAirport(str1);
        if (port == null) {
            System.out.println("Starting airport not found.");
            scanner.close();
            return;
        }
        route.add(port);

        // Prompt for destination airport
        System.out.print("Enter ICAO identifier of destination airport: ");
        str2 = scanner.nextLine();
        port = portDbase.searchAirport(str2);
        if (port == null) {
            System.out.println("Destination airport not found.");
            scanner.close();
            return;
        }
        route.add(port);

        // Allow user to add intermediate stops until they confirm final destination
        System.out.print("Is this your final destination? (yes/no): ");
        String answer = scanner.nextLine().toLowerCase();
        while (!answer.equals("yes")) {
            System.out.print("Enter ICAO identifier of next destination airport: ");
            str3 = scanner.nextLine();
            port = portDbase.searchAirport(str3);
            if (port != null) {
                route.add(port);
            } else {
                System.out.println("Airport not found. Please try again.");
            }
            System.out.print("Is this your final destination? (yes/no): ");
            answer = scanner.nextLine().toLowerCase();
        }

        // Process the flight plan for potential refueling stops
        for (int m = 0; m < route.size() - 1; m++) {
            Airport s1 = route.get(m);
            Airport s2 = route.get(m + 1);

            // If s2 is the final destination
            if (m == route.size() - 2) {
                if (!fuelEnough(plane, s1.getLatitude(), s1.getLongitude(), 
                                  s2.getLatitude(), s2.getLongitude())) {
                    Airport refuelPort = addRefuelingStop(plane, s1, s2);
                    if (refuelPort != null) {
                        route.add(m + 1, refuelPort);
                    }
                }
            } else {
                Airport s3 = route.get(m + 2);
                if (!fuelEnough(plane, s1.getLatitude(), s1.getLongitude(), 
                                  s2.getLatitude(), s2.getLongitude(), 
                                  s3.getLatitude(), s3.getLongitude())) {
                    Airport refuelPort = addRefuelingStop(plane, s1, s2, s3);
                    if (refuelPort != null) {
                        route.set(m + 1, refuelPort);
                    }
                }
            }
        }

        output(plane, route);
        scanner.close();
    }

    /**
     * Calculates the distance between two points using the Haversine formula.
     */
    public double distanceOfLegs(double lat1, double long1, double lat2, double long2) {
        double R = 6371 / 1.85; // Earth's radius in nautical miles
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLong = Math.toRadians(long2 - long1);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLong / 2) * Math.sin(deltaLong / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Adds a refueling stop between two airports if needed.
     */
    private Airport addRefuelingStop(Airplane plane, Airport s1, Airport s2) {
        System.out.println("Insufficient fuel for the leg. Searching for a refueling stop...");
        for (int m = 0; m < portDbase.size(); m++) {
            Airport temp = portDbase.get(m);
            double angle = getAngle(s1.getLatitude(), s1.getLongitude(), 
                                    temp.getLatitude(), temp.getLongitude());
            if (angle >= 0 && angle <= 180) {
                int fuel_type = temp.getFuelType();
                if (fuel_type == plane.getFuelType()) {
                    if (fuelEnough(plane, s1.getLatitude(), s1.getLongitude(), 
                                   temp.getLatitude(), temp.getLongitude()) &&
                        fuelEnough(plane, temp.getLatitude(), temp.getLongitude(), 
                                   s2.getLatitude(), s2.getLongitude())) {
                        return temp;
                    }
                }
            }
        }
        System.out.println("Unable to locate a suitable refueling stop.");
        return null;
    }

    /**
     * Overloaded method for adding a refueling stop considering three airports.
     */
    private Airport addRefuelingStop(Airplane plane, Airport s1, Airport s2, Airport s3) {
        // Similar logic can be applied here if needed; currently returns null.
        return null;
    }

    /**
     * Checks if the airplane has sufficient fuel for a leg.
     */
    private boolean fuelEnough(Airplane plane, double lat1, double long1, double lat2, double long2) {
        double distance = distanceOfLegs(lat1, long1, lat2, long2);
        double maxRange = getMaxRangeOfAeroplane(plane, lat1, long1, lat2, long2);
        return distance <= maxRange;
    }

    /**
     * Overloaded method to check fuel sufficiency for two legs.
     */
    private boolean fuelEnough(Airplane plane, double lat1, double long1, 
                               double lat2, double long2, double lat3, double long3) {
        double distance1 = distanceOfLegs(lat1, long1, lat2, long2);
        double distance2 = distanceOfLegs(lat2, long2, lat3, long3);
        double maxRange = getMaxRangeOfAeroplane(plane, lat1, long1, lat2, long2);
        return (distance1 + distance2) <= maxRange;
    }

    /**
     * Calculates the maximum range an airplane can fly without refueling.
     */
    public double getMaxRangeOfAeroplane(Airplane ap, double lat1, double long1, double lat2, double long2) {
        double capacity = ap.getFuelCapacity();
        double burn_rate = ap.getFuelBurnRate();
        double time = capacity / burn_rate;
        double real_airspeed = getRealAirspeed(ap.getAirspeed(), lat1, long1, lat2, long2);
        return real_airspeed * time;
    }

    /**
     * Calculates the real airspeed; simplified implementation.
     */
    private double getRealAirspeed(double airspeed, double lat1, double long1, double lat2, double long2) {
        return airspeed; // Simplified: no wind or weather adjustments.
    }

    /**
     * Calculates the angle between two points.
     */
    private double getAngle(double lat1, double long1, double lat2, double long2) {
        double deltaLong = long2 - long1;
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double y = Math.sin(Math.toRadians(deltaLong)) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(Math.toRadians(deltaLong));
        double angle = Math.toDegrees(Math.atan2(y, x));
        return (angle + 360) % 360;
    }

    /**
     * Outputs the flight plan.
     */
    private void output(Airplane plane, List<Airport> route) {
        System.out.println("Flight Plan:");
        System.out.println("Aircraft: " + plane.getMake() + " " + plane.getModel());
        for (int i = 0; i < route.size(); i++) {
            System.out.println("Stop " + (i + 1) + ": " + route.get(i).getName());
        }
    }

    public static void main(String[] args) {
        PlanningSoftware planning = new PlanningSoftware();
        planning.flightPlanning();
    }
}
