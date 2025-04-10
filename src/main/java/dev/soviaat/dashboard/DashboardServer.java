package dev.soviaat.dashboard;

import dev.soviaat.Statify;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static dev.soviaat.Common.LOGGER;

public class DashboardServer {
    private static Process dashboardProcess;
    public static final String PORT = "3000";

    public static void startDashboardServer() {

        try {
            // Get the absolute path of the resources folder
            File resourcesDir = new File(System.getProperty("user.dir"), "../src/main/resources");
            File dashboardDir = new File(resourcesDir, "dashboard");
            File indexFile = new File(dashboardDir, "index.js");

            if (!dashboardDir.exists() || !indexFile.exists()) {
                LOGGER.error("Error: Dashboard directory or index.js not found at " + indexFile.getAbsolutePath());
                return;
            }

            if (isDashboardRunning()) {
                LOGGER.info("Dashboard server is already running.");
                return;
            }

            ProcessBuilder processBuilder = new ProcessBuilder("node", "index.js");
            processBuilder.directory(dashboardDir);
            processBuilder.redirectErrorStream(true);

            dashboardProcess = processBuilder.start();
            LOGGER.info("Dashboard server started at " + dashboardDir.getAbsolutePath());

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dashboardProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        LOGGER.info("[Dashboard] " + line);
                    }
                } catch (IOException e) {
                    LOGGER.error("Error reading dashboard server output", e);
                }
            }).start();
        } catch (IOException e) {
            LOGGER.error("Error starting dashboard server", e);
        }
    }

    public static void stopDashboardServer() {
        try {
            if (!isDashboardRunning()) {
                LOGGER.info("Dashboard server is not running.");
                return;
            }

            String command = "fuser -k 3000/tcp";

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                command = "taskkill /F /IM node.exe";
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.start();

            LOGGER.info("Dashboard server stopped.");
        } catch (IOException e) {
            LOGGER.error("Error stopping dashboard server", e);
        }
    }

    private static boolean isDashboardRunning() {
        try (Socket socket = new Socket("localhost", 3000)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
