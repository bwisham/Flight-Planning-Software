public class Airport {
    private String nameOfAirport;
    private String ICAOIdentifier;
    private float longitude;
    private float latitude;
    private float radioFrequency;
    private String radioType;
    private String fuelTypes;

    public void setNameAirport(String nAirport) {
        this.nameOfAirport = nAirport;
    }

    public void setRadioFrequency(float Rfreq) {
        this.radioFrequency = Rfreq;
    }

    public void setRadioType(String RType) {
        this.radioType = RType;
    }

    public void setICAOIdentifier(String iIdentifier) {
        this.ICAOIdentifier = iIdentifier;
    }

    public void setLongitude(float Longitude) {
        this.longitude = Longitude;
    }

    public void setLatitude(float Latitude) {
        this.latitude = Latitude;
    }

    public void setFuelTypes(String FTypes) {
        this.fuelTypes = FTypes;
    }

    public String getFuelType() {
        return fuelTypes;
    }

    public float getRadioFrequency() {
        return radioFrequency;
    }

    public String getRadioType() {
        return radioType;
    }

    public String getAirportName() {
        return nameOfAirport;
    }

    public String getICAOIdentifier() {
        return ICAOIdentifier;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void print() {
        System.out.println("Airport Name: " + nameOfAirport);
        System.out.println("ICAO Identifier: " + ICAOIdentifier);
        System.out.println("Longitude: " + longitude);
        System.out.println("Latitude: " + latitude);
        System.out.println("Radio Frequency: " + radioFrequency);
        System.out.println("Radio Type: " + radioType);
        System.out.println("Fuel Types: " + fuelTypes);
    }
}
