package com.tobiasmaneschijn.mcjsmod.ui.widget;


import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IDETextBox extends AbstractWidget {
    private List<String> lines;
    private int cursorLine;
    private int cursorColumn;
    private int scrollOffset;
    private static final int LINE_HEIGHT = 12;
    private static final int PADDING = 5;

    public IDETextBox(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.lines = new ArrayList<>();
        this.lines.add("");
        this.cursorLine = 0;
        this.cursorColumn = 0;
        this.scrollOffset = 0;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        renderLineNumbers(guiGraphics);
        renderText(guiGraphics);
        renderCursor(guiGraphics);
    }

    private void renderBackground(GuiGraphics guiGraphics) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);
    }

    private void renderLineNumbers(GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;
        int y = this.getY() + PADDING - scrollOffset * LINE_HEIGHT;
        for (int i = scrollOffset; i < Math.min(lines.size(), scrollOffset + getVisibleLines()); i++) {
            guiGraphics.drawString(font, String.valueOf(i + 1), this.getX() + PADDING, y, 0xAAAAAA);
            y += LINE_HEIGHT;
        }
    }

    private void renderText(GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;
        int y = this.getY() + PADDING - scrollOffset * LINE_HEIGHT;
        int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;

        for (int i = scrollOffset; i < Math.min(lines.size(), scrollOffset + getVisibleLines()); i++) {
            String line = lines.get(i);
            List<TextSegment> segments = parseLineForHighlighting(line);
            int x = this.getX() + lineNumberWidth;

            for (TextSegment segment : segments) {
                guiGraphics.drawString(font, segment.text, x, y, segment.color);
                x += font.width(segment.text);
            }

            y += LINE_HEIGHT;
        }
    }

    private void renderCursor(GuiGraphics guiGraphics) {
        if (this.isFocused()) {
            Font font = Minecraft.getInstance().font;
            int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;
            int cursorX = this.getX() + lineNumberWidth + font.width(lines.get(cursorLine).substring(0, cursorColumn));
            int cursorY = this.getY() + PADDING + (cursorLine - scrollOffset) * LINE_HEIGHT;

            if (cursorY >= this.getY() && cursorY + LINE_HEIGHT <= this.getY() + this.height) {
                guiGraphics.fill(cursorX, cursorY, cursorX + 1, cursorY + LINE_HEIGHT, 0xFFFFFFFF);
            }
        }
    }

    private List<TextSegment> parseLineForHighlighting(String line) {
        List<TextSegment> segments = new ArrayList<>();

        Pattern keywordPattern = Pattern.compile("\\b(if|else|for|while|function)\\b");
        Pattern stringPattern = Pattern.compile("\"[^\"]*\"");

        Matcher keywordMatcher = keywordPattern.matcher(line);
        Matcher stringMatcher = stringPattern.matcher(line);

        int lastEnd = 0;

        while (lastEnd < line.length()) {
            boolean keywordFound = keywordMatcher.find(lastEnd);
            boolean stringFound = stringMatcher.find(lastEnd);

            if (!keywordFound && !stringFound) {
                // No more matches, add the rest of the line as plain text
                segments.add(new TextSegment(line.substring(lastEnd), 0xFFFFFF));
                break;
            }

            if (keywordFound && (!stringFound || keywordMatcher.start() < stringMatcher.start())) {
                // Keyword comes first (or is the only match)
                if (keywordMatcher.start() > lastEnd) {
                    segments.add(new TextSegment(line.substring(lastEnd, keywordMatcher.start()), 0xFFFFFF));
                }
                segments.add(new TextSegment(keywordMatcher.group(), 0xFF7777));
                lastEnd = keywordMatcher.end();
            } else {
                // String comes first (or is the only match)
                if (stringMatcher.start() > lastEnd) {
                    segments.add(new TextSegment(line.substring(lastEnd, stringMatcher.start()), 0xFFFFFF));
                }
                segments.add(new TextSegment(stringMatcher.group(), 0x77FF77));
                lastEnd = stringMatcher.end();
            }
        }

        return segments;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case 256 -> { // Escape
                setFocused(false);
                return true;
            }
            case 259 -> { // Backspace
                if (cursorColumn > 0) {
                    String currentLine = lines.get(cursorLine);
                    lines.set(cursorLine, currentLine.substring(0, cursorColumn - 1) + currentLine.substring(cursorColumn));
                    cursorColumn--;
                } else if (cursorLine > 0) {
                    String currentLine = lines.remove(cursorLine);
                    cursorLine--;
                    cursorColumn = lines.get(cursorLine).length();
                    lines.set(cursorLine, lines.get(cursorLine) + currentLine);
                }
                return true;
            }
            case 257 -> { // Enter
                String currentLine = lines.get(cursorLine);
                String newLine = currentLine.substring(cursorColumn);
                lines.set(cursorLine, currentLine.substring(0, cursorColumn));
                lines.add(cursorLine + 1, newLine);
                cursorLine++;
                cursorColumn = 0;
                return true;
            }
            case 263 -> { // Left arrow
                if (cursorColumn > 0) {
                    cursorColumn--;
                } else if (cursorLine > 0) {
                    cursorLine--;
                    cursorColumn = lines.get(cursorLine).length();
                }
                return true;
            }
            case 262 -> { // Right arrow
                if (cursorColumn < lines.get(cursorLine).length()) {
                    cursorColumn++;
                } else if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    cursorColumn = 0;
                }
                return true;
            }
            case 265 -> { // Up arrow
                if (cursorLine > 0) {
                    cursorLine--;
                    cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
                }
                return true;
            }
            case 264 -> { // Down arrow
                if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
                }
                return true;
            }
            case 258 -> { // Tab
                String currentLine = lines.get(cursorLine);
                lines.set(cursorLine, currentLine.substring(0, cursorColumn) + "    " + currentLine.substring(cursorColumn));
                cursorColumn += 4;
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        String currentLine = lines.get(cursorLine);
        lines.set(cursorLine, currentLine.substring(0, cursorColumn) + codePoint + currentLine.substring(cursorColumn));
        cursorColumn++;
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            setFocused(true);
            updateCursorPosition(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateCursorPosition(double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;

        cursorLine = scrollOffset + (int)((mouseY - this.getY() - PADDING) / LINE_HEIGHT);
        cursorLine = Math.max(0, Math.min(cursorLine, lines.size() - 1));

        String line = lines.get(cursorLine);
        int x = this.getX() + lineNumberWidth;
        for (cursorColumn = 0; cursorColumn < line.length(); cursorColumn++) {
            if (x + font.width(line.substring(0, cursorColumn + 1)) > mouseX) {
                break;
            }
        }
    }

    private int getVisibleLines() {
        return (this.height - PADDING * 2) / LINE_HEIGHT;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.literal("IDE Text Box"));
    }

    private record TextSegment(String text, int color) {}
}