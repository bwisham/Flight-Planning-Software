    private double calculateDistance(Airport a1, Airport a2) {
        double latDifference = a2.getLatitude() - a1.getLatitude();
        double longDifference = a2.getLongitude() - a1.getLongitude();
        return Math.sqrt(Math.pow(latDifference, 2) + Math.pow(longDifference, 2)) * 60;
    }
    
    class Airport {
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