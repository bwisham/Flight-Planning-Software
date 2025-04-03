import javax.swing.*;
import java.awt.*;

Public class AirportManager {

    AirportDatabase portDbase;

    public AirportManager() {
        Access AirportDatabase for portDbase;
    }
    /* This function on startup is used to access the preexisting airport database */

    public void addAirport() {
        String name;
        String icao;
        double longitude;
        double latitude;
        int fuelType;
        int key;

        //prompt user input for name of airport;
        String userinput = JOptionPane.showInputDialog("Enter the name of the airport:");
        name = userinput;
        //let name be the user response;
        //prompt user input for ICAO identifier of airport;
        String userinput2 = JOptionPane.showInputDialog("Enter the ICAO Identifier of the airport:");
        icao = userinput2;
       // let icao be the user response;
        //prompt user for latitude of airport;
        String userinput3 = JOptionPane.showInputDialog("Enter the longitude of the airport:");
        longitude = Double.parseDouble(userinput3);
        //let latitude be the user response;
        //prompt user for longitude of airport;
        String userinput4 = JOptionPane.showInputDialog("Enter the latitude of the airport:");
        latitude = Double.parseDouble(userinput4);
        //let longitude be the user response;
        //prompt user for airport fuel type;
        String userinput5 = JOptionPane.showInputDialog("Enter the fuel types the airport supports:");
        fuelType = Integer.parseInt(userinput5);
        //let fuelType be the user response;
        assign key addition in chronological order upon confirmation of submission;

        btnSubmit() {
            create GUI element for confirmation of submission of input variables;
            send submission to AirportDatabase and display results;
        }
    }

    public void searchAirport() {
        Insert GUI dropdown menu displaying airport elements;
        Insert GUI textbox for manual search input;
        send query to database;
        summarize closest matching element selected between dropdown and text input;
        Send summary to variable x;
        Display GUI popup with results = x;
    }

    public void modifyAirport() {
        Display GUI elements of current list of active airports;
        Access current values assigned to a selected submission;
        Display values of a selected airport in a text box, allow user edits;

        btnSubmit() {
            create GUI element for confirmation of submission of modified values;
            save updated values and display results;
        }
    }

    public void deleteAirport() {
        Display GUI elements of current list of active airports;
        Access current values assigned to a submission;
        Display values of a selected airport;

        btnDelete() {
            Create GUI popup: "Do you wish to delete the selected airport?";
            If yes → erase selected `portDbase` element;
            Else cancel → terminate popup;
        }
    }

    public void printAirportList() {
        Display GUI elements of current airports entered;
        View current values assigned to the user's specific submission;

        btnSubmit() {
            create GUI element for confirmation of submission of input variables;
            send submission to UserDatabase and display results;
        }
    }

    Private void validateAirportData() {
        Display GUI elements showing current values entered by the user;
        Confirm validity of assigned values for the user's submission;

        btnSubmit() {
            create GUI element for confirmation of validation;
            send submission to UserDatabase and display validation results;
        }
    }

    public void showMenu() {
        Display GUI elements showing current user-entered values;
        Display current values assigned to the user's specific submission;

        btnSubmit() {
            create GUI element for confirmation of submission of input variables;
            send submission to UserDatabase and display results;
        }
    }
}
