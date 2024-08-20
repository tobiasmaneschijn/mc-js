package com.tobiasmaneschijn.mcjsmod.ui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.network.payload.ComputerBlockPayload;
import com.tobiasmaneschijn.mcjsmod.network.payload.RunComputerCodePayload;
import com.tobiasmaneschijn.mcjsmod.network.shell.JavaScriptExecutionPayload;
import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.EditorLogger;
import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.TextEditor;
import com.tobiasmaneschijn.mcjsmod.ui.widget.terminal.Terminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class ComputerScreen extends Screen {
    private Terminal terminal;


    private final int imageWidth = 256;
    private final int imageHeight = 256;

    private ComputerBlockEntity computerBlockEntity;

    private List<String> terminalLines = new ArrayList<>();


    // texture asset locations

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MCJSMod.MODID, "textures/gui/background.png");

    // corners
    private static final ResourceLocation CORNERS_TEXTURE = ResourceLocation.fromNamespaceAndPath(MCJSMod.MODID, "textures/gui/corners2.png");

    // Update these constants to match your new texture layout
    private static final int SIDE_HEIGHT = 12;
    private static final int SIDE_WIDTH = 256;
    private static final int TOP_SIDE_UV_START = 0;
    private static final int BOTTOM_SIDE_UV_START = 12;

    private static final int LEFT_SIDE_UV_X_START = 232;
    private static final int LEFT_SIDE_UV_Y_START = 24;
    private static final int RIGHT_SIDE_UV_X_START = 244;
    private static final int RIGHT_SIDE_UV_Y_START = 24;

    private int x;
    private int y;

    public ComputerScreen(BlockPos pos) {
        super(Component.translatable("ui.mcjsmod.computer_screen_title"));
        if (Minecraft.getInstance().level != null) {
            this.computerBlockEntity = (ComputerBlockEntity) Minecraft.getInstance().level.getBlockEntity(pos);
        }
    }

    @Override
    protected void init() {
        try {
            super.init();

            x = (width - imageWidth) / 2;
            y = (height - imageHeight) / 2;

            // Terminal output area
            terminal = new Terminal(x, y + 12, 256, 256 - 24);
            terminal.setCommandCallback(this::handleTerminalCommand);
            addRenderableWidget(terminal);

            // Restore terminal content if it exists
            if (!terminalLines.isEmpty()) {
                for (String line : terminalLines) {
                    terminal.appendLine(line);
                }
            }
        } catch (Exception e) {
            MCJSMod.LOGGER.error("Error initializing ComputerScreen", e);
        }
    }


    @Override
    public void tick() {
        super.tick();
        // If there's any pending result in the ComputerBlockEntity, display it
        if (computerBlockEntity != null) {
            String latestResult = computerBlockEntity.getLatestResult();
            String latestConsoleOutput = computerBlockEntity.getLatestConsoleOutput();
            if (!latestResult.isEmpty() || !latestConsoleOutput.isEmpty()) {
                handleJavaScriptResult(latestConsoleOutput, latestResult);
                // Clear the stored results to avoid displaying them again
                computerBlockEntity.setLatestResult("");
                computerBlockEntity.setLatestConsoleOutput("");
            }
        }
    }


    @Override
    public void onClose() {
        super.onClose();
        terminalLines = new ArrayList<>(terminal.getLines());
        if (computerBlockEntity != null) {
            computerBlockEntity.setScreen(null);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // draw the background

        // set the shader to TexPosShader
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        // bind the texture
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        guiGraphics.blit(BACKGROUND_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    private void renderCornersAndSides(GuiGraphics guiGraphics) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CORNERS_TEXTURE);

        // draw the sides

        // top side
        guiGraphics.blit(CORNERS_TEXTURE, x, y, 0, TOP_SIDE_UV_START, SIDE_WIDTH, SIDE_HEIGHT);

        // bottom side
        guiGraphics.blit(CORNERS_TEXTURE, x, y + imageHeight - SIDE_HEIGHT, 0, BOTTOM_SIDE_UV_START, SIDE_WIDTH, SIDE_HEIGHT);

        // left side
        guiGraphics.blit(CORNERS_TEXTURE, x - 12, y + 12, LEFT_SIDE_UV_X_START, LEFT_SIDE_UV_Y_START, SIDE_HEIGHT, SIDE_WIDTH - 24);

        // right side
        guiGraphics.blit(CORNERS_TEXTURE, x + imageWidth, y + 12, RIGHT_SIDE_UV_X_START, RIGHT_SIDE_UV_Y_START, SIDE_HEIGHT, SIDE_WIDTH - 24);

        // top left corner 12X12 pixels starts art 0,24
        guiGraphics.blit(CORNERS_TEXTURE, x - 12, y, 0, 24, 12, 12);

        // top right corner 12X12 pixels starts at 12,24
        guiGraphics.blit(CORNERS_TEXTURE, x + imageWidth, y, 12, 24, 12, 12);

        // bottom left corner 12X12 pixels starts at 0,36
        guiGraphics.blit(CORNERS_TEXTURE, x - 12, y + imageHeight - 12, 0, 36, 12, 12);

        // bottom right corner 12X12 pixels starts at 12,36
        guiGraphics.blit(CORNERS_TEXTURE, x + imageWidth, y + imageHeight - 12, 12, 36, 12, 12);


    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderCornersAndSides(guiGraphics);

    }


    private void handleTerminalCommand(String command) {
        try {
            if (computerBlockEntity != null) {
                // Send all commands to the server for execution
                JavaScriptExecutionPayload payload = new JavaScriptExecutionPayload(computerBlockEntity.getBlockPos(), command);
                PacketDistributor.sendToServer(payload);
            } else {
                terminal.appendError("Computer block entity not found.");
            }

            if (command.equalsIgnoreCase("clear")) {
                terminal.clear();
                terminalLines.clear();
            }

        } catch (Exception e) {
            MCJSMod.LOGGER.error("Error handling terminal command: " + command, e);
            terminal.appendError("Error: " + e.getMessage());
        }
    }


    public void handleJavaScriptResult(String output, String result) {
        try {
            StringBuilder displayText = new StringBuilder();

            if (!output.isEmpty()) {
                displayText.append(output);
            }

            if (!result.isEmpty() && output.isEmpty() && !result.toLowerCase().contains("null")) {
                if (displayText.length() > 0) {
                    displayText.append("\n");
                }
                displayText.append(result);
            }

            for (String line : displayText.toString().split("\n")) {
                terminal.appendLine(line);
                terminalLines.add(line);
            }

            // new line
            terminal.newPrompt();

        } catch (Exception e) {
            MCJSMod.LOGGER.error("Error handling JavaScript result", e);
            terminal.appendError("Error handling result: " + e.getMessage());
            terminal.newPrompt();
        }
    }


    public void setTerminalOutput(String output, boolean clear) {
        if (terminal != null) {
            if (clear) {
                terminal.clear();
                terminalLines.clear();
            }

            for (String line : output.split("\n")) {
                terminal.appendLine(line);
                terminalLines.add(line);
            }
        }
    }


}