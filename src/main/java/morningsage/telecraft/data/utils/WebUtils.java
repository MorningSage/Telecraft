package morningsage.telecraft.data.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class WebUtils {
    public static String downloadURLAsString(String url) {
        // Will house the server's text response
        StringBuilder response = new StringBuilder();

        // The connection, stream, and temp buffer string
        HttpURLConnection connection;
        BufferedReader bufferedReader;
        String inputLine;

        try {
            // Attempt to open the connection
            connection = (HttpURLConnection) new URL(url).openConnection();

            // Choose the correct stream
            if (connection.getResponseCode() > 299) {
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }

            // Read a line at a time and save it until we are done
            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }

            // Clean up
            bufferedReader.close();
            connection.disconnect();
        } catch (Exception ex) {
            // Fail silently.  The exception should be logged elsewhere
        }

        // Return whatever we got
        return response.toString();
    }
}
