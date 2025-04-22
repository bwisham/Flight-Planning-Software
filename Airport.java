/**
 * Represents an airport with its key attributes including identification,
 * location, communication details, and available fuel types.
 */
public class Airport {
    // Public fields representing airport attributes
    public String nameOfAirport;      // Full name of the airport
    public String ICAOIdentifier;     // 4-letter ICAO code (e.g., "KJFK" for JFK)
    public float longitude;           // Geographic longitude coordinate
    public float latitude;            // Geographic latitude coordinate
    public float radioFrequency;      // Communication frequency (in MHz)
    public String radioType;          // Type of radio communication available
    public String fuelTypes;          // Available fuel types (e.g., "JetA, AvGas")

    /**
     * Sets the name of the airport.
     * @param nAirport The name to set for the airport
     */
    public void setNameAirport(String nAirport) {
        this.nameOfAirport = nAirport;
    }

    /**
     * Sets the radio communication frequency.
     * @param Rfreq The frequency value in MHz (e.g., 121.5 for emergency frequency)
     */
    public void setRadioFrequency(float Rfreq) {
        this.radioFrequency = Rfreq;
    }

    /**
     * Sets the type of radio communication available.
     * @param RType The radio type (e.g., "Tower", "Ground", "ATIS")
     */
    public void setRadioType(String RType) {
        this.radioType = RType;
    }

    /**
     * Sets the ICAO identifier code.
     * @param iIdentifier The 4-letter ICAO code (e.g., "EGLL" for Heathrow)
     */
    public void setICAOIdentifier(String iIdentifier) {
        this.ICAOIdentifier = iIdentifier;
    }

    /**
     * Sets the longitude coordinate of the airport.
     * @param Longitude The longitude value in decimal degrees
     */
    public void setLongitude(float Longitude) {
        this.longitude = Longitude;
    }

    /**
     * Sets the latitude coordinate of the airport.
     * @param Latitude The latitude value in decimal degrees
     */
    public void setLatitude(float Latitude) {
        this.latitude = Latitude;
    }

    /**
     * Sets the available fuel types at the airport.
     * @param FTypes Comma-separated list of fuel types (e.g., "JetA1, 100LL")
     */
    public void setFuelTypes(String FTypes) {
        this.fuelTypes = FTypes;
    }

    /**
     * Gets the available fuel types.
     * @return String containing the fuel types available
     */
    public String getFuelType() {
        return fuelTypes;
    }

    /**
     * Gets the radio communication frequency.
     * @return The frequency value in MHz
     */
    public float getRadioFrequency() {
        return radioFrequency;
    }

    /**
     * Gets the type of radio communication.
     * @return The radio type as a string
     */
    public String getRadioType() {
        return radioType;
    }

    /**
     * Gets the name of the airport.
     * @return The airport name
     */
    public String getAirportName() {
        return nameOfAirport;
    }

    /**
     * Gets the ICAO identifier code.
     * @return The 4-letter ICAO code
     */
    public String getICAOIdentifier() {
        return ICAOIdentifier;
    }

    /**
     * Gets the longitude coordinate.
     * @return The longitude value in decimal degrees
     */
    public float getLongitude() {
        return longitude;
    }

    /**
     * Gets the latitude coordinate.
     * @return The latitude value in decimal degrees
     */
    public float getLatitude() {
        return latitude;
    }

    /**
     * Prints all airport details to standard output in a human-readable format.
     * Output includes all airport attributes with descriptive labels.
     */
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