public class Airport {
    private int key;
    private String name;
    private String icao;
    private double latitude;
    private double longitude;

    public Airport(int key, String name, String icao, double latitude, double longitude) {
        this.key = key;
        this.name = name;
        this.icao = icao;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public int getKey() { return key; }
    public String getName() { return name; }
    public String getIcao() { return icao; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
} 