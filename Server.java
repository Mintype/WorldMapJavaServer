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

public class Server {
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
            server.createContext("/country-clicked", new CountryClickedHandler()); // POST route that runs when user clicks a country.
        } catch (IOException e) {
            throw new RuntimeException("Failed to start HTTP server on port " + port, e);
        }
    }

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

		            Map<String, String> countryColors;
								countryColors = new HashMap<>();
								countryColors.put("USA", "blue");
								countryColors.put("Canada", "red");
								countryColors.put("Germany", "black");

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


    // Starts the server and opens the default URL in a browser
    public void run() {
        server.setExecutor(null);
        server.start();
        System.out.println("Server is running on port " + this.port);
        openURL("http://localhost:" + this.port + "/");
    }

    private void openURL(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            URI uri = new URI(url);
            desktop.browse(uri);
        } catch (Exception e) {
            System.err.println("Failed to open URL: " + url + " - " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Server server = new Server(8000); // Initialize server on port 8000.
        server.run(); // Start the server.
    }
}
