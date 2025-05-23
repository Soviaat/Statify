package dev.soviaat.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import dev.soviaat.FileManagement;
import dev.soviaat.Statify;
import dev.soviaat.commands.SheetId;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.google.auth.oauth2.GoogleCredentials.fromStream;
import static dev.soviaat.Common.LOGGER;

public class GoogleSheetsUtil {
    private static final String APP_NAME = "Statify";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static Sheets sheetService;

    private static GoogleCredentials authorize() throws IOException, GeneralSecurityException {
        InputStream credStream = Statify.class.getClassLoader().getResourceAsStream("config/statify/credentials.json");

        if (credStream == null) {
            throw new FileNotFoundException("Google credentials file not found.");
        }

        return fromStream(credStream)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/spreadsheets"));
    }

    public static Sheets getSheetService() throws IOException, GeneralSecurityException {
        if (sheetService == null) {
            sheetService = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, new HttpCredentialsAdapter(authorize()))
                    .setApplicationName(APP_NAME).build();
        }
        return sheetService;
    }

    public static void UpdateStatsFromCSV(String csvFilePath, String range, String worldName) throws GeneralSecurityException, IOException {
        CompletableFuture.runAsync(() -> {
            String sheetId = FileManagement.loadSheetIdFromJson(worldName);

            if(sheetId == null) {
                LOGGER.error("Cannot upload statistics. No Sheet ID found for world: {}", worldName);
                return;
            }

            ParseCSV(csvFilePath).thenAccept(data -> {
                try {
                        if(data.isEmpty()) {
                            LOGGER.warn("No data found in the CSV file: {}", csvFilePath);
                        }

                    Sheets sheetService = getSheetService();
                    ValueRange body = new ValueRange().setValues(data);

                    sheetService.spreadsheets().values()
                            .update(sheetId, range, body)
                            .setValueInputOption("RAW")
                            .execute();


                    LOGGER.info("Uploaded data from CSV to Google Sheets: {}", csvFilePath);
                } catch (IOException | GeneralSecurityException e) {
                    LOGGER.error("Failed to upload CSV data to Google Sheets", e);
                }
            });
        });
    }

    @SuppressWarnings("all")
    private static CompletableFuture<List<List<Object>>> ParseCSV(String csvFilePath) {
        return CompletableFuture.supplyAsync(() -> {
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
        });

    }
}
