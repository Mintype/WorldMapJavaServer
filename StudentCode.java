import java.util.*;

public class StudentCode extends Server{

    public static void main(String[] args) {
        Server server = new StudentCode(); // Initialize server on default port (8000).
        server.run(); // Start the server.
        server.openURL(); // Open url in browser.
    }

    @Override
    public void getInputCountries(String country1, String country2) {
//        sendMessageToUser("The shortest path has been calculated.");
//        addCountryColor(country1, "red");
//        addCountryColor(country2, "blue");
//        setMessage("hello");
    }

    @Override
    public void getColorPath() {

    }

    @Override
    public void handleClick(String country) {

    }
}
