package dev.soviaat.dashboard;

import dev.soviaat.utils.ResponseBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static dev.soviaat.FileManagement.writeStatsToFile;
import static net.minecraft.text.ClickEvent.Action.OPEN_URL;


public class DashboardPrompt extends Screen {
    public DashboardPrompt() {
        super(Text.literal("Statify Dashboard"));
    }

    @Override
    protected void init() {
        int buttonWidth = 100;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Yes"), button -> {
                if (this.client != null) {
                    try {
                        ProcessBuilder checkNode = new ProcessBuilder("node", "-v");
                        Process process = checkNode.start();
                        if (!process.waitFor(5, TimeUnit.SECONDS) || process.exitValue() != 0) {
                            // Node.js nem elérhető
                            MutableText error = ResponseBuilder.Builder(
                                    "Error: Node.js is not installed. Please install Node.js and make sure to add it to your system PATH.",
                                    true,
                                    "[CLICK HERE TO DOWNLOAD]",
                                    "Click to download Node.js",
                                    "https://nodejs.org/dist/v22.14.0/node-v22.14.0-x64.msi",
                                    OPEN_URL
                            );

                            if (this.client.player != null) {
                                this.client.player.sendMessage(error, false);
                            }
                            this.client.setScreen(null);
                            return;
                        }
                    } catch (IOException | InterruptedException e) {
                        if (this.client.player != null) {
                            this.client.player.sendMessage(Text.literal("Error checking Node.js installation."), false);
                        }
                        this.client.setScreen(null);
                        return;
                    }
                }

                DashboardServer.startDashboardServer();
                String id = IdGenerator.getLastGeneratedId();
                String port = DashboardServer.PORT;
                String worldName = "unknown";

                if (this.client != null) {
                    this.client.setScreen(null);
                    if (MinecraftClient.getInstance().getServer() != null) {
                        worldName = MinecraftClient.getInstance().getServer().getSaveProperties().getLevelName();
                        MutableText response = ResponseBuilder.Builder(
                                "Statify Dashboard server started locally ",
                                false,
                                "[CLICK HERE TO OPEN]",
                                "Click to open in browser",
                                "http://localhost:" + port + "/dashboard?id=" + id + "&world=" + worldName.replace(" ", "%20"),
                                OPEN_URL
                        );
                        if (this.client.player != null) this.client.player.sendMessage(response, false);
                        if (this.client.getServer() != null) writeStatsToFile(this.client.getServer().getPlayerManager().getPlayerList().getFirst(), worldName);
                    }
                }
            })
            .dimensions(centerX - buttonWidth - 10, centerY, buttonWidth, buttonHeight)
            .build()
        );

        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("No"), button -> {
                if (this.client != null) {
                    this.client.setScreen(null);
                }
            })
            .dimensions(centerX + 10, centerY, buttonWidth, buttonHeight)
            .build()
        );
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(this.textRenderer, "Would you like to start Statify Dashboard?", this.width / 2, this.height / 2 - 40, 0x1C86FF);
        super.render(ctx, mouseX, mouseY, delta);
    }
}
