import java.util.*;

public class StudentCode extends Server{

    @Override
    public Map<String, String> getCountriesToColor(String country1, String country2) {
        Map<String, String> countriesToColor = new HashMap<>();
        countriesToColor.put(country1, "red");
        countriesToColor.put(country2, "blue");
        return countriesToColor;
    }

    public static void main(String[] args) {
        Server server = new StudentCode(); // Initialize server on default port (8000).
        server.run(); // Start the server.
        server.openURL(); // Open url in browser.
    }
}
