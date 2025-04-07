public class FlightPlanner {
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
