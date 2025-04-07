import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

public abstract class Server {
    private HttpServer server;
    private int port; // Port on which the server will listen
    private MapInteraction interactor;

    public Server(int port) {
		interactor = new MapInteraction();
        this.port = port;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            // Define the routes
            server.createContext("/", new DefaultRoute());         // Serves index.html
            server.createContext("/static", new StaticFileHandler()); // Serves static files like JS
            server.createContext("/country-clicked", new CountryClickedHandler()); // POST route that is received when user clicks a country.
            server.createContext("/api", new APIHandler()); // POST route that is received when user clicks a country.
        } catch (IOException e) {
            throw new RuntimeException("Failed to start HTTP server on port " + port, e);
        }
    }

    public Server() {
        interactor = new MapInteraction();
        this.port = 8000;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            // Define the routes
            server.createContext("/", new DefaultRoute());         // Serves index.html
            server.createContext("/static", new StaticFileHandler()); // Serves static files like JS
            server.createContext("/country-clicked", new CountryClickedHandler()); // POST route that is received when user clicks a country.
            server.createContext("/api", new APIHandler()); // POST route that is received when user clicks a country.
        } catch (IOException e) {
            throw new RuntimeException("Failed to start HTTP server on port " + port, e);
        }
    }

    public abstract Map<String, String> getCountriesToColor(String country1, String country2);

    // Main route where the index.html is served
    static class DefaultRoute implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] res = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("index.html"));
            exchange.sendResponseHeaders(200, res.length);  // Send 200 OK status
            OutputStream os = exchange.getResponseBody();
            os.write(res);
            os.close();
        }
    }

    // Handler to serve static files like JS and CSS
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String filePath = "";//"resources" + path.substring("/static".length());
            byte[] fileContent = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath));

            if (path.endsWith(".js")) {
                exchange.getResponseHeaders().add("Content-Type", "application/javascript");
            } else if (path.endsWith(".css")) {
                exchange.getResponseHeaders().add("Content-Type", "text/css");
            }

            exchange.sendResponseHeaders(200, fileContent.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileContent);
            os.close();
        }
    }

    /* POST request "country-clicked". Runs when the user clicks a country. It gets a country from the user. Returns a fixed list of countries to the user.
    static class CountryClickedHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Read the request body for the country name
                InputStream is = exchange.getRequestBody();
                String country = new BufferedReader(new InputStreamReader(is))
                        .lines().collect(Collectors.joining("\n"));
                System.out.println("Country clicked: " + country);

                // Return a fixed list of country names
                // This can be changed later for something else.
                String response = "United States of America";

                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed for non-POST
            }
        }
    }
    */




		public class CountryClickedHandler implements HttpHandler {
		    @Override
		    public void handle(HttpExchange exchange) throws IOException {

		        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
		            // Read the request body (country name)
		            String country;
		            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
		                country = reader.lines().collect(Collectors.joining("\n"));
		            }

		            System.out.println("Country clicked: " + country);

		            Map<String, String> countryColors = new HashMap<>();
                    countryColors.put("United States of America", "green");
                    countryColors.put("Canada", "red");
                    countryColors.put("Germany", "yellow");

								//ObjectMapper objectMapper = new ObjectMapper();
            		//String jsonResponse = objectMapper.writeValueAsString(countryColors);
					ArrayList<String> countries = interactor.click(country);
					StringJoiner sj = new StringJoiner("\",\"", "[\"", "\"]");
					for (String c : countries) sj.add(c);
		            // Respond with the same country received (or modify as needed)
		            byte[] responseBytes = sj.toString().getBytes(StandardCharsets.UTF_8);
		            exchange.sendResponseHeaders(200, responseBytes.length);

		            try (OutputStream os = exchange.getResponseBody()) {
		                os.write(responseBytes);
		            } // Auto-closes OutputStream

		        } else {
		            exchange.sendResponseHeaders(405, 0); // Method Not Allowed
		            exchange.getResponseBody().close();
		        }
		    }
		}

    public class APIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
//                // Read the request body (country name)
//                String country1, country2;
//                String input;
//                try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
////                    country = reader.lines().collect(Collectors.joining("\n"));
//                    input = reader.lines().collect(Collectors.joining("\n"));
//                }
//                String[] countries = input.substring(0, input.length() - 1).split("\"");
//
//                country1 = countries[3];
//                country2 = countries[countries.length - 1];

                // Read the request body
                String input;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                    input = reader.lines().collect(Collectors.joining("\n"));
                }

                HashMap<String, String> jsonObject = parseJSON(input);
                String country1 = jsonObject.get("country1");
                String country2 = jsonObject.get("country2");
                System.out.println("country1: " + country1);
                System.out.println("country2: " + country2);

                // This is a KEY example on how you can give a hashmap of countries+color to the frontend to display!
                Map<String, String> countryColors = getCountriesToColor(country1,country2);
//                countryColors.put("United States of America", "green");
//                countryColors.put("Canada", "red");
//                countryColors.put("Germany", "yellow");

                // Convert HashMap to JSON string
                String jsonResponse = mapToJSON(countryColors);

                // Set content type and send response
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                } // Auto-closes OutputStream

            } else {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                exchange.getResponseBody().close();
            }
        }
    }

    // Starts the server and opens the default URL in a browser
    public void run() {
        server.setExecutor(null);
        server.start();
        System.out.println("Server is running on port " + this.port);
//        openURL("http://localhost:" + this.port + "/");
    }

    public void openURL() {
        try {
            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI("http://localhost:" + this.port + "/");
            desktop.browse(uri);
        } catch (Exception e) {
            System.err.println("Failed to open URL: " + "http://localhost:" + this.port + "/" + " - " + e.getMessage());
        }
    }

    // Convert HashMap to JSON string (made with claude 3.7)
    private String mapToJSON(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\"");
            first = false;
        }

        json.append("}");
        return json.toString();
    }

    // Simple JSON parser without libraries (made with claude 3.7)
    private HashMap<String, String> parseJSON(String jsonStr) {
        HashMap<String, String> result = new HashMap<>();
        // Remove curly braces and parse key-value pairs
        jsonStr = jsonStr.trim();
        if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
            jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
        }

        // Split by commas not inside quotes
        String[] pairs = jsonStr.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                // Remove quotes from key and value if present
                if (key.startsWith("\"") && key.endsWith("\"")) {
                    key = key.substring(1, key.length() - 1);
                }
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                result.put(key, value);
            }
        }
        return result;
    }
}
class MapInteraction {
    //private ArrayList<String> total;

    public MapInteraction() {
        //total = new ArrayList<String>();
    }

    public ArrayList<String> click(String country) {
        //total.add(country);
        ArrayList<String> countries = new ArrayList<>();
        countries.add(country);
        return countries;
    }
}
