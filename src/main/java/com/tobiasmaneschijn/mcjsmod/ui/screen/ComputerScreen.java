package com.tobiasmaneschijn.mcjsmod.ui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.TextEditor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ComputerScreen extends Screen {
    private TextEditor codeInput;
    private MultiLineEditBox terminalOutput;
    private Button runButton;

    private Button incrementButton;

    private StringWidget incrementCounter;

    private final int imageWidth = 256;
    private final int imageHeight = 256;

    private ComputerBlockEntity computerBlockEntity;

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
        super.init();
         x = (width - imageWidth) / 2;
         y = (height - imageHeight) / 2;


        // Code input area
        codeInput = new TextEditor(x, y+12, 256, 156);
        addRenderableWidget(codeInput);

        // Run button
        runButton = Button.builder(Component.literal("Run"), button -> runCode())
                .bounds(x + 10, y + 125, 50, 20)
                .build();
        //addRenderableWidget(runButton);

        // Terminal output area
        terminalOutput = new MultiLineEditBox(this.font, x, y + 156 + 12, 256, 80, Component.literal("This here be the output log"), Component.literal("Output"));
        terminalOutput.setFocused(false);
        addRenderableWidget(terminalOutput);
    }

    private void runCode() {
        // ModMessages.sendToServer(new RunCodeC2SPacket(code, menu.getBlockEntity().getBlockPos()));
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

    public void setTerminalOutput(String output) {
        terminalOutput.setValue(output);
    }
}