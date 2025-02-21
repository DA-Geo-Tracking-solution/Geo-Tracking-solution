package htl.hl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class GPSSender {
    private final static String QUEUE_NAME = "gps_data_queue";

    public static void main(String[] argv) {
        String csvFile = "src/main/resources/data_processed.csv";
        String line;
        String csvSeparator = ",";

        String username = "test2";
        String password = "password";

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        try {
            // Step 1: Fetch Access Token using KeycloakTokenFetcher logic
            String accessToken = getAccessToken(username, password);
            System.out.println("Access Token: " + accessToken);

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);

                // Step 2: Read and process the CSV file
                try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                    br.readLine(); // Skip the header

                    int i = 0;

                    while ((line = br.readLine()) != null) {
                        String[] values = line.split(csvSeparator);
                        String sender = values[0];
                        String timestamp = values[1];
                        String longitude = values[2];
                        String latitude = values[3];

                        // Replace space with 'T' to match ISO-8601 format
                        timestamp = timestamp.replace(" ", "T");

                        try {
                            // Parse the timestamp into a ZonedDateTime
                            ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestamp);
                            Instant instant = zonedDateTime.toInstant(); // Convert to Instant
                            instant = Instant.now();

                            // Construct the message with the Instant timestamp (in UTC)
                            String message = accessToken + ":" + sender + "," + instant.toString() + "," + longitude + "," + i;

                            // Publish the message
                            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
                            System.out.println(" [x] Sent: '" + message + "'");
                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {
                            System.out.println("Failed to parse timestamp: " + timestamp);
                            continue; // Skip this record if parsing fails
                        }
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static String getAccessToken(String username, String password) throws Exception {
        URL url = new URL("http://localhost:8081/realms/geo-tracking-solution/protocol/openid-connect/token");

        // Open connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Request body as per the Postman request
        String requestBody = "username=" + username + "&password=" + password + "&grant_type=password&client_id=angular-client";

        // Write request body
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
        }

        // Read the response
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                scanner.useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";
                return extractTokenFromResponse(response, "access_token");
            }
        } else {
            throw new Exception("Failed to fetch token. Response Code: " + responseCode);
        }
    }

    private static String extractTokenFromResponse(String responseBody, String key) {
        String tokenKey = "\"" + key + "\":\"";
        int startIndex = responseBody.indexOf(tokenKey) + tokenKey.length();
        int endIndex = responseBody.indexOf("\"", startIndex);
        return responseBody.substring(startIndex, endIndex);
    }
}
