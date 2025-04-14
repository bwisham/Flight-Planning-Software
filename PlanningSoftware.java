/*public class FlightPlanner {
    private int travelCapacity;
    private String refuelRequirements;
    private float longitude;
    private float latitude;
    private float distance;
    private String software;

    public void setTravelCapacity(int tCapacity) {
        this.travelCapacity = tCapacity;
    }

    public void setRefuelRequirments(String rRequirments) {
        this.refuelRequirements = rRequirments;
    }

    public void setLongitude(float Longitude) {
        this.longitude = Longitude;
    }

    public void setLatitude(float Latitude) {
        this.latitude = Latitude;
    }

    public void setDistance(float Distance) {
        this.distance = Distance;
    }

    public void setSoftware(String Software) {
        this.software = Software;
    }

    public int getTravelCapacity() {
        return travelCapacity;
    }

    public String getRefuelRequirements() {
        return refuelRequirements;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getDistance() {
        return distance;
    }

    public String getSoftware() {
        return software;
    }

    public void print() {
        System.out.println("Travel Capacity: " + travelCapacity);
        System.out.println("Refuel Requirements: " + refuelRequirements);
        System.out.println("Longitude: " + longitude);
        System.out.println("Latitude: " + latitude);
        System.out.println("Distance: " + distance);
        System.out.println("Software: " + software);
    }
}
*/
import java.util.Scanner;

public class PlanningSoftware {

    private AirportDatabase portDbase;
    private AirplaneDatabase planeDbase;

    /**
     * Constructor
     */
    public PlanningSoftware() {
        portDbase = new AirportDatabase();
        planeDbase = new AirplaneDatabase();
    }

    /**
     * This function sets the parameters for what the user will input
     * and the data that will be collected
     */
    public void flightPlanning() {
        Scanner scanner = new Scanner(System.in);
        
        Distance D;
        Longitude l1;
        Latitude l2;
        FuelCapacity FC;
        RefuelRequirements RR;
        Airplane plane;
        Airport port;
        Airspeed AR;
        String make, model, type;
        String str1, str2, str3;

        // Prompt user for airplane details
        System.out.print("Enter the make of the airplane: ");
        make = scanner.nextLine();
        
        System.out.print("Enter the model of the airplane: ");
        model = scanner.nextLine();
        
        System.out.print("Enter the type of the airplane: ");
        type = scanner.nextLine();
        
        plane = planeDbase.search(make, model, type);

        // Prompt for starting airport
        System.out.print("Enter ICAO identifier/Longitude and Latitude of starting airport: ");
        str1 = scanner.nextLine();
        port = portDbase.search(str1);
        Airport a1 = new Airport();
        a1.add(port);

        // Prompt for destination airport
        System.out.print("Enter ICAO identifier/Longitude and Latitude of destination airport: ");
        str2 = scanner.nextLine();
        port = portDbase.search(str2);
        a1.add(port);

        // Check if this is the final destination
        System.out.print("Is this your final destination? (yes/no): ");
        String answer = scanner.nextLine().toLowerCase();
        
        while (!answer.equals("yes")) {
            System.out.print("Enter ICAO identifier/Longitude and Latitude of next destination airport: ");
            str3 = scanner.nextLine();
            port = portDbase.search(str3);
            a1.add(port);
            
            System.out.print("Is this your final destination? (yes/no): ");
            answer = scanner.nextLine().toLowerCase();
        }

        // Process the flight plan
        for (int m = 0; m < a1.size() - 1; m++) {
            Airport s1 = a1.get(m);
            Airport s2 = a1.get(m + 1);

            if (m == a1.size() - 2) { // If s2 is the final destination
                if (!fuelEnough(plane, s1.getLatitude(), s1.getLongitude(), 
                              s2.getLatitude(), s2.getLongitude())) {
                    Airport refuelPort = addRefuelingStop(plane, s1, s2);
                    if (refuelPort != null) {
                        a1.add(m + 1, refuelPort);
                    }
                }
            } else {
                Airport s3 = a1.get(m + 2);
                if (!fuelEnough(plane, s1.getLongitude(), s1.getLatitude(), 
                              s2.getLongitude(), s2.getLatitude(), 
                              s3.getLongitude(), s3.getLatitude())) {
                    Airport refuelPort = addRefuelingStop(plane, s1, s2, s3);
                    if (refuelPort != null) {
                        a1.set(m + 1, refuelPort);
                    }
                }
            }
        }

        output(plane, a1);
        scanner.close();
    }

    /**
     * Calculates the actual distance between a starting and destination airport
     * @param lat1 - latitude of the 1st station
     * @param long1 - longitude of the 1st station
     * @param lat2 - latitude of the 2nd station
     * @param long2 - longitude of the 2nd station
     * @return the distance between two stations in nautical miles
     */
    public double distanceOfLegs(double lat1, double long1, double lat2, double long2) {
        double distance;
        double R = 6371 / 1.85; // the radius of the earth in nautical miles

        // Haversine formula to calculate distance
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLong = Math.toRadians(long2 - long1);

        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLong/2) * Math.sin(deltaLong/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        
        distance = R * c;
        return distance;
    }

    /**
     * Adds a refueling stop between two airports if needed
     */
    private Airport addRefuelingStop(Airplane plane, Airport s1, Airport s2) {
        System.out.println("The fuel is not enough for two stations, please enter the refueling stop station");
        
        int fuel_type;
        for (int m = 0; m < portDbase.size(); m++) {
            Airport temp = portDbase.get(m);
            double angle = getAngle(s1.getLatitude(), s1.getLongitude(), 
                                  temp.getLatitude(), temp.getLongitude());
            
            if (angle >= 0 && angle <= 180) {
                fuel_type = temp.getFuel().getType();
                
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
        
        System.out.println("Cannot find a suitable refueling stop.");
        return null;
    }

    /**
     * Adds a refueling stop between three airports if needed (overloaded method)
     */
    private Airport addRefuelingStop(Airplane plane, Airport s1, Airport s2, Airport s3) {
        // Similar implementation as above but for three airports
        // Implementation would need to be completed based on specific requirements
        return null;
    }

    /**
     * Checks if the airplane has enough fuel for the leg
     */
    private boolean fuelEnough(Airplane plane, double lat1, double long1, double lat2, double long2) {
        double distance = distanceOfLegs(lat1, long1, lat2, long2);
        double maxRange = getMaxRangeOfAeroplane(plane, lat1, long1, lat2, long2);
        return distance <= maxRange;
    }

    /**
     * Checks if the airplane has enough fuel for two legs (overloaded method)
     */
    private boolean fuelEnough(Airplane plane, double lat1, double long1, 
                             double lat2, double long2, double lat3, double long3) {
        double distance1 = distanceOfLegs(lat1, long1, lat2, long2);
        double distance2 = distanceOfLegs(lat2, long2, lat3, long3);
        double maxRange = getMaxRangeOfAeroplane(plane, lat1, long1, lat2, long2);
        return (distance1 + distance2) <= maxRange;
    }

    /**
     * Calculates the maximum distance that an airplane can fly without refueling
     */
    public double getMaxRangeOfAeroplane(Airplane ap, double lat1, double long1, double lat2, double long2) {
        double max_distance, capacity, burn_rate;
        double time;
        double real_airspeed;

        real_airspeed = getRealAirspeed(ap.getAirspeed(), lat1, long1, lat2, long2);
        capacity = ap.getFuelCapacity();
        burn_rate = ap.getFuelBurnRate();
        
        time = capacity / burn_rate;
        max_distance = real_airspeed * time;
        
        return max_distance;
    }

    /**
     * Calculates the actual direction a plane must travel between airports
     */
    public double calculateHeading(double airspeed, double lat1, double long1, double lat2, double long2) {
        double deltaLong = long2 - long1;
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        
        double y = Math.sin(Math.toRadians(deltaLong)) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - 
                  Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(Math.toRadians(deltaLong));
        
        double angle = Math.toDegrees(Math.atan2(y, x));
        return (angle + 360) % 360; // Normalize to 0-360 degrees
    }

    /**
     * Calculates the real airspeed considering wind and other factors
     */
    private double getRealAirspeed(double airspeed, double lat1, double long1, double lat2, double long2) {
        // Implementation would need wind data and more complex calculations
        return airspeed; // Simplified for now
    }

    /**
     * Calculates the angle between two points
     */
    private double getAngle(double lat1, double long1, double lat2, double long2) {
        // Implementation would calculate the angle between the two points
        return 0; // Simplified for now
    }

    /**
     * Outputs the flight plan
     */
    private void output(Airplane plane, Airport a1) {
        // Implementation would format and print the flight plan details
        System.out.println("Flight Plan:");
        System.out.println("Aircraft: " + plane.getMake() + " " + plane.getModel());
        
        for (int i = 0; i < a1.size(); i++) {
            System.out.println("Stop " + (i+1) + ": " + a1.get(i).getName());
        }
    }
}