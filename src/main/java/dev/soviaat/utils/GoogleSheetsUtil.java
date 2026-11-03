package dev.soviaat.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import dev.soviaat.Common;
import dev.soviaat.FileManagement;
import dev.soviaat.Statify;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GoogleSheetsUtil {
    private static final String APP_NAME = "Statify";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private static HttpTransport httpTransport;
    private static Sheets sheetService;

    private static GoogleCredentials authorize() throws IOException, GeneralSecurityException {
        InputStream credStream = Statify.class.getClassLoader().getResourceAsStream("config/statify/credentials.json");
        if (credStream == null) {
            throw new FileNotFoundException("Google credentials file not found.");
        } else {
            return GoogleCredentials.fromStream(credStream)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));
        }
    }

    public static synchronized Sheets getSheetService() throws IOException, GeneralSecurityException {
        if (sheetService == null) {
            // Keep reference to httpTransport so we can shut down its thread pool
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            sheetService = new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(authorize()))
                    .setApplicationName(APP_NAME)
                    .build();
        }
        return sheetService;
    }

    public static synchronized void shutdown() {
        if (httpTransport != null) {
            try {
                httpTransport.shutdown();
                httpTransport = null;
                sheetService = null;
                Common.LOGGER.info("Google Sheets HTTP transport successfully shut down.");
            } catch (IOException e) {
                Common.LOGGER.error("Failed to shut down Google Sheets HTTP transport", e);
            }
        }
    }

    public static void updateStatsFromCSV(String csvFilePath, String range, String worldName) {
        CompletableFuture.runAsync(() -> updateStatsFromCSVSync(csvFilePath, range, worldName));
    }

    public static void updateStatsFromCSVSync(String csvFilePath, String range, String worldName) {
        String sheetId = FileManagement.loadSheetIdFromJson(worldName);
        if (sheetId == null) {
            Common.LOGGER.error("Cannot upload statistics. No Sheet ID found for world: {}", worldName);
            return;
        }

        List<List<Object>> data = parseCSVSync(csvFilePath);
        if (data.isEmpty()) {
            Common.LOGGER.warn("No data found in the CSV file: {}", csvFilePath);
            return;
        }

        try {
            Sheets service = getSheetService();
            ValueRange body = new ValueRange().setValues(data);
            service.spreadsheets().values().update(sheetId, range, body).setValueInputOption("RAW").execute();
            Common.LOGGER.info("Uploaded data from CSV to Google Sheets: {}", csvFilePath);
        } catch (GeneralSecurityException | IOException e) {
            Common.LOGGER.error("Failed to upload CSV data to Google Sheets", e);
        }
    }

    private static List<List<Object>> parseCSVSync(String csvFilePath) {
        List<List<Object>> data = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFilePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                List<Object> row = new ArrayList<>();
                Collections.addAll(row, values);
                data.add(row);
            }
        } catch (IOException e) {
            Common.LOGGER.error("Error reading CSV file: {}", csvFilePath, e);
        }
        return data;
    }
}