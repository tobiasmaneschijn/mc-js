package com.tobiasmaneschijn.mcjsmod.ui.widget.terminal;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
import com.tobiasmaneschijn.mcjsmod.blockentity.ComputerBlockEntity;
import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.Cursor;
import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.record.TextSegment;
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
import java.util.function.Consumer;

public class Terminal extends AbstractWidget {

    private static final boolean LOGGING_ENABLED = true; // Toggle this flag to enable/disable logging

    private String currentInputPrompt = "";

    private final List<String> lines;
    private final Cursor cursor;
    private final TerminalRenderer renderer;
    private int scrollOffset;
    private static final int LINE_HEIGHT = 12;
    private static final int PADDING = 5;
    private List<String> commandHistory;
    private int historyIndex;
    private String currentInput;
    private Consumer<String> commandCallback;
    private boolean isScrolledToBottom = true;

    private final TerminalSyntaxHighlighter syntaxHighlighter;
    private final ComputerBlockEntity computerBlockEntity;

    private String lastAppendedLine = "";

    private Consumer<String> inputCallback;

    public Terminal(int x, int y, int width, int height, ComputerBlockEntity computerBlockEntity) {
        super(x, y, width, height, Component.empty());
        this.lines = new ArrayList<>();
        this.cursor = new Cursor();
        this.renderer = new TerminalRenderer();
        this.scrollOffset = 0;
        this.commandHistory = new ArrayList<>();
        this.historyIndex = -1;
        this.currentInput = "";
        this.syntaxHighlighter = new TerminalSyntaxHighlighter();
        this.computerBlockEntity = computerBlockEntity;

        newPrompt();
    }

    public void newPrompt() {
        if (LOGGING_ENABLED) MCJSMod.LOGGER.info("newPrompt called.");
        lines.add("> ");
        cursor.setLine(lines.size() - 1);
        cursor.setColumn(2);
        currentInput = "";
        currentInputPrompt = "> ";
        ensureCursorVisible();
        if (LOGGING_ENABLED) MCJSMod.LOGGER.info("New prompt created. Current line: {}", lines.get(cursor.getLine()));
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderer.render(guiGraphics, this);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257) { // Enter key
            if (LOGGING_ENABLED) MCJSMod.LOGGER.info("Enter key pressed");
            String input = currentInput;
            if (inputCallback != null) {
                inputCallback.accept(input);
            }
            currentInput = "";
            return true;
        } else if (keyCode == 259) { // Backspace
            if (currentInput.length() > 0) {
                currentInput = currentInput.substring(0, currentInput.length() - 1);
                updateCurrentLine();
            }
            return true;
        } else if (keyCode == 263) { // Left arrow
            if (cursor.getColumn() > currentInputPrompt.length()) {
                cursor.setColumn(cursor.getColumn() - 1);
            }
            return true;
        } else if (keyCode == 262) { // Right arrow
            if (cursor.getColumn() < currentInputPrompt.length() + currentInput.length()) {
                cursor.setColumn(cursor.getColumn() + 1);
            }
            return true;
        } else if (keyCode == 265) { // Up arrow
            if (historyIndex > 0) {
                historyIndex--;
                currentInput = commandHistory.get(historyIndex);
                updateCurrentLine();
            }
            return true;
        } else if (keyCode == 264) { // Down arrow
            if (historyIndex < commandHistory.size() - 1) {
                historyIndex++;
                currentInput = commandHistory.get(historyIndex);
                updateCurrentLine();
            } else if (historyIndex == commandHistory.size() - 1) {
                historyIndex++;
                currentInput = "";
                updateCurrentLine();
            }
            return true;
        }
        return false;
    }

    public void setInputCallback(Consumer<String> callback) {
        this.inputCallback = callback;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (LOGGING_ENABLED) MCJSMod.LOGGER.info("charTyped called with codePoint: {} and modifiers: {}", codePoint, modifiers);
        currentInput += codePoint;
        updateCurrentLine();
        if (LOGGING_ENABLED) MCJSMod.LOGGER.info("Current input after charTyped: {}", currentInput);
        return true;
    }

    public void clear() {
        lines.clear();
        scrollOffset = 0;
        isScrolledToBottom = true;
        cursor.setLine(0);
        cursor.setColumn(0);
    }

    public void clearInput() {
        currentInput = "";
        updateCurrentLine();
    }

    private void updateCurrentLine() {
        if (LOGGING_ENABLED) MCJSMod.LOGGER.info("updateCurrentLine called. Current cursor position: line {}, column {}", cursor.getLine(), cursor.getColumn());

        if (lines.isEmpty() || cursor.getLine() >= lines.size()) {
            if (LOGGING_ENABLED) MCJSMod.LOGGER.info("Lines are empty or cursor is out of bounds. Calling newPrompt.");
            newPrompt();
        }

        String currentLine = lines.get(cursor.getLine());
        String prompt = currentLine.substring(0, currentLine.indexOf('>') + 2);
        lines.set(cursor.getLine(), prompt + currentInput);
        cursor.setColumn(prompt.length() + currentInput.length());

        if (LOGGING_ENABLED) {
            MCJSMod.LOGGER.info("Updated line: {}", lines.get(cursor.getLine()));
            MCJSMod.LOGGER.info("Updated cursor position: line {}, column {}", cursor.getLine(), cursor.getColumn());
        }
    }

    private void ensureCursorVisible() {
        try {
            if (LOGGING_ENABLED) MCJSMod.LOGGER.info("ensureCursorVisible called. Scroll offset: {}, Total lines: {}", scrollOffset, lines.size());
            int totalLines = lines.size();
            int visibleLines = getVisibleLines();

            if (cursor.getLine() < 0) {
                cursor.setLine(0);
            }
            if (cursor.getLine() >= totalLines) {
                cursor.setLine(totalLines - 1);
            }

            if (cursor.getLine() < scrollOffset) {
                scrollOffset = cursor.getLine();
            } else if (cursor.getLine() >= scrollOffset + visibleLines) {
                scrollOffset = Math.max(0, cursor.getLine() - visibleLines + 1);
            }
            isScrolledToBottom = (scrollOffset >= totalLines - visibleLines);

            if (LOGGING_ENABLED) MCJSMod.LOGGER.info("Cursor visibility ensured. New scroll offset: {}, Is scrolled to bottom: {}", scrollOffset, isScrolledToBottom);
        } catch (Exception e) {
            MCJSMod.LOGGER.error("Error in ensureCursorVisible", e);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.literal("Terminal"));
    }

    // Getters and setters
    public List<String> getLines() {
        return lines;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int offset) {
        this.scrollOffset = offset;
    }

    public String getCurrentInput() {
        return currentInput;
    }

    public void appendLine(String line) {
        int maxWidth = this.width - 2 * PADDING;
        List<String> wrappedLines = wrapText(line, maxWidth);

        if (!wrappedLines.isEmpty()) {
            lines.addAll(wrappedLines);

            if (isScrolledToBottom) {
                scrollToBottom();
            }

            cursor.setLine(lines.size() - 1);
            cursor.setColumn(wrappedLines.get(wrappedLines.size() - 1).length());
        }

        ensureCursorVisible();
    }

    public void appendResult(String result) {
        int maxWidth = this.width - 2 * PADDING;
        List<String> wrappedLines = wrapText(result, maxWidth);

        if (!result.isEmpty()) {
            lines.addAll(wrappedLines);
        }
        currentInput = "";  // Ensure currentInput is cleared when a new result is appended
        ensureCursorVisible();
    }

    public int getVisibleLines() {
        return (this.height - PADDING * 2) / LINE_HEIGHT;
    }

    private List<String> wrapText(String text, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        List<String> wrappedLines = new ArrayList<>();

        for (String line : text.split("\n")) {  // Split input text into lines based on newline characters
            while (!line.isEmpty()) {
                int wrapWidth = font.width(line);
                if (wrapWidth <= maxWidth) {
                    wrappedLines.add(line);
                    break;
                }

                int wrapPos = font.plainSubstrByWidth(line, maxWidth).length();
                int spacePos = line.lastIndexOf(' ', wrapPos);

                if (spacePos != -1 && spacePos != 0) {
                    wrapPos = spacePos;
                }

                wrappedLines.add(line.substring(0, wrapPos));
                line = line.substring(wrapPos).trim();
            }
        }

        return wrappedLines;
    }

    private void scrollToBottom() {
        scrollOffset = Math.max(0, lines.size() - getVisibleLines());
        isScrolledToBottom = true;
    }

    public void scrollToTop() {
        scrollOffset = 0;
        isScrolledToBottom = false;
    }

    public void scrollBy(int lines) {
        scrollOffset = Math.max(0, Math.min(scrollOffset + lines, Math.max(0, this.lines.size() - getVisibleLines())));
        isScrolledToBottom = (scrollOffset >= this.lines.size() - getVisibleLines());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isMouseOver(mouseX, mouseY)) {
            int scrollAmount = (int) -scrollY;
            scrollOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, Math.max(0, lines.size() - getVisibleLines())));
            isScrolledToBottom = (scrollOffset >= lines.size() - getVisibleLines());
            return true;
        }
        return false;
    }

    public void updateFromBlockEntity() {
        String latestOutput = computerBlockEntity.getLatestConsoleOutput();
        if (!latestOutput.isEmpty() && !latestOutput.equals(lastAppendedLine)) {
            appendLine(latestOutput);
            lastAppendedLine = latestOutput;
            if (LOGGING_ENABLED) MCJSMod.LOGGER.info("Terminal updated with: " + latestOutput);
        }

        String latestResult = computerBlockEntity.getLatestResult();
        if (!latestResult.isEmpty()) {
            appendResult(latestResult);
            if (LOGGING_ENABLED) MCJSMod.LOGGER.info("Terminal updated with result: " + latestResult);
        }
    }

    private static class TerminalRenderer {
        public void render(GuiGraphics guiGraphics, Terminal terminal) {
            renderBackground(guiGraphics, terminal);
            renderText(guiGraphics, terminal);
            renderCursor(guiGraphics, terminal);
            renderScrollbar(guiGraphics, terminal);
        }

        private void renderBackground(GuiGraphics guiGraphics, Terminal terminal) {
            guiGraphics.fill(terminal.getX(), terminal.getY(), terminal.getX() + terminal.getWidth(), terminal.getY() + terminal.getHeight(), 0xFF000000);
        }

        private void renderText(GuiGraphics guiGraphics, Terminal terminal) {
            Font font = Minecraft.getInstance().font;
            int y = terminal.getY() + PADDING;
            List<String> lines = terminal.getLines();

            for (int i = terminal.getScrollOffset(); i < Math.min(lines.size(), terminal.getScrollOffset() + terminal.getVisibleLines()); i++) {
                String line = lines.get(i);
                List<TextSegment> segments = terminal.syntaxHighlighter.highlightLine(line);
                int x = terminal.getX() + PADDING;

                for (TextSegment segment : segments) {
                    guiGraphics.drawString(font, segment.text(), x, y, segment.color());
                    x += font.width(segment.text());
                }

                y += LINE_HEIGHT;  // Move to the next line for each line of text
            }
        }

        private void renderCursor(GuiGraphics guiGraphics, Terminal terminal) {
            List<String> lines = terminal.getLines();
            Cursor cursor = terminal.getCursor();

            if (lines.isEmpty()) {
                return; // Don't render cursor if there are no lines
            }

            int cursorLine = Math.min(cursor.getLine(), lines.size() - 1);
            if (cursorLine >= terminal.getScrollOffset() && cursorLine < terminal.getScrollOffset() + terminal.getVisibleLines()) {
                Font font = Minecraft.getInstance().font;
                String currentLine = lines.get(cursorLine);
                int cursorColumn = Math.min(cursor.getColumn(), currentLine.length());
                int cursorX = terminal.getX() + PADDING + font.width(currentLine.substring(0, cursorColumn));
                int cursorY = terminal.getY() + PADDING + (cursorLine - terminal.getScrollOffset()) * LINE_HEIGHT;

                guiGraphics.fill(cursorX, cursorY, cursorX + 1, cursorY + LINE_HEIGHT, 0xFFFFFFFF);
            }
        }

        private void renderScrollbar(GuiGraphics guiGraphics, Terminal terminal) {
            int totalLines = terminal.getLines().size();
            int visibleLines = terminal.getVisibleLines();

            if (totalLines > visibleLines) {
                int scrollbarHeight = terminal.getHeight() * visibleLines / totalLines;
                int scrollbarY = terminal.getY() + (terminal.getHeight() - scrollbarHeight) * terminal.getScrollOffset() / (totalLines - visibleLines);

                guiGraphics.fill(terminal.getX() + terminal.getWidth() - 2, terminal.getY(),
                        terminal.getX() + terminal.getWidth(), terminal.getY() + terminal.getHeight(),
                        0xFF333333);
                guiGraphics.fill(terminal.getX() + terminal.getWidth() - 2, scrollbarY,
                        terminal.getX() + terminal.getWidth(), scrollbarY + scrollbarHeight,
                        0xFFAAAAAA);
            }
        }
    }
}
