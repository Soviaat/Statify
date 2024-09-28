package dev.soviaat.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import dev.soviaat.Statify;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.auth.oauth2.GoogleCredentials.fromStream;
import static com.mojang.text2speech.Narrator.LOGGER;

public class GoogleSheetsUtil {
    private static final String APP_NAME = "Statify";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static Sheets sheetService;
    private static final String SPREADSHEET_ID = "1voa-kqrSQ-abclwb6eb7fASu_XGXbVcK4AOjN2BujQU";

    private static GoogleCredentials authorize() throws IOException, GeneralSecurityException {
        InputStream credStream = Statify.class.getClassLoader().getResourceAsStream("config/statify/credentials.json");

        if (credStream == null) {
            throw new FileNotFoundException("Google credentials file not found.");
        }

        return fromStream(credStream)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));
    }

    private static Sheets getSheetService() throws IOException, GeneralSecurityException {
        if (sheetService == null) {
            sheetService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpCredentialsAdapter(authorize()))
                    .setApplicationName(APP_NAME).build();
        }
        return sheetService;
    }

    public static void UpdateStatsFromCSV(String csvFilePath, String range) throws GeneralSecurityException, IOException {
        try {
            List<List<Object>> csvData = ParseCSV(csvFilePath);

            if (csvData.isEmpty()) {
                LOGGER.warn("No data found in the CSV file: {}", csvFilePath);
                return;
            }

            Sheets sheetService = getSheetService();
            ValueRange body = new ValueRange().setValues(csvData);

            sheetService.spreadsheets().values()
                    .update(SPREADSHEET_ID, range, body)
                    .setValueInputOption("RAW")
                    .execute();

            LOGGER.info("Uploaded data from CSV to Google Sheets: {}", csvFilePath);
        } catch (IOException | GeneralSecurityException e) {
            LOGGER.error("Failed to upload CSV data to Google Sheets", e);
        }
    }

    @SuppressWarnings("all")
    private static List<List<Object>> ParseCSV(String csvFilePath) {
        List<List<Object>> data = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(";");
                List<Object> row = new ArrayList<>();
                for (String value : values) {
                    row.add(value);
                }
                data.add(row);
            }
        } catch (IOException e) {
            LOGGER.error("Error reading CSV file: {}", csvFilePath, e);
        }

        return data;
    }
}
