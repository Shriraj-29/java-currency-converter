import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.json.JSONObject;

public class CurrencyConverter {
    private static String API_KEY;
    private static String API_BASE_URL;

    static {
        try (InputStream input = CurrencyConverter.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
            }
            Properties properties = new Properties();
            properties.load(input);
            API_KEY = properties.getProperty("API_KEY");
            API_BASE_URL = properties.getProperty("API_BASE_URL");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (API_KEY == null || API_BASE_URL == null) {
            System.err.println("Error: API_KEY or API_BASE_URL environment variable is not set.");
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter the base currency (e.g. INR, USD, EUR): ");
            String baseCurrency = reader.readLine().trim().toUpperCase();
            if (baseCurrency.isEmpty()) {
                System.err.println("Base currency cannot be empty.");
                return;
            }

            System.out.print("Enter the target currency (e.g. INR, USD, EUR): ");
            String targetCurrency = reader.readLine().trim().toUpperCase();
            if (targetCurrency.isEmpty()) {
                System.err.println("Target currency cannot be empty.");
                return;
            }

            System.out.print("Enter the amount to convert: ");
            double amount;
            try {
                amount = Double.parseDouble(reader.readLine().trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid amount entered. Please enter a numeric value.");
                return;
            }

            double exchangeRate = getExchangeRate(baseCurrency, targetCurrency);
            if (exchangeRate > 0) {
                double convertedAmount = amount * exchangeRate;
                System.out.printf("Converted amount: %.2f %s%n", convertedAmount, targetCurrency);
            } else {
                System.err.println("Failed to retrieve a valid exchange rate.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double getExchangeRate(String baseCurrency, String targetCurrency) throws IOException {
        try {
            URI uri = new URI(API_BASE_URL + "?apikey=" + API_KEY + "&currencies=" + targetCurrency + "&base_currency=" + baseCurrency);
            URL url = uri.toURL();

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject rates = jsonResponse.getJSONObject("data");
                    return rates.getDouble(targetCurrency);
                }
            } else {
                System.out.println("Failed to fetch exchange rate. Response code: " + responseCode);
                return 0.0;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
