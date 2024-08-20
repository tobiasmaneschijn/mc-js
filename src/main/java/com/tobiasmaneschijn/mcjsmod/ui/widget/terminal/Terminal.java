package com.tobiasmaneschijn.mcjsmod.ui.widget.terminal;

import com.tobiasmaneschijn.mcjsmod.MCJSMod;
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
    private final List<String> lines;
    private final Cursor cursor;
    private final TerminalRenderer renderer;
    private final TerminalInputHandler inputHandler;
    private int scrollOffset;
    private static final int LINE_HEIGHT = 12;
    private static final int PADDING = 5;
    private List<String> commandHistory;
    private int historyIndex;
    private String currentInput;
    private Consumer<String> commandCallback;
    private boolean isScrolledToBottom = true;

    private final TerminalSyntaxHighlighter syntaxHighlighter;

    public Terminal(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.lines = new ArrayList<>();
        this.cursor = new Cursor();
        this.renderer = new TerminalRenderer();
        this.inputHandler = new TerminalInputHandler(this);
        this.scrollOffset = 0;
        this.commandHistory = new ArrayList<>();
        this.historyIndex = -1;
        this.currentInput = "";
        this.syntaxHighlighter = new TerminalSyntaxHighlighter();

        newPrompt();
    }

    public void setCommandCallback(Consumer<String> callback) {
        this.commandCallback = callback;
    }


    public void newPrompt() {
        lines.add("> ");
        cursor.setLine(lines.size() - 1);
        cursor.setColumn(2);
        ensureCursorVisible();
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderer.render(guiGraphics, this);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return inputHandler.handleKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return inputHandler.handleCharTyped(codePoint, modifiers);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isMouseOver(mouseX, mouseY)) {
            // scrollY is positive when scrolling up, negative when scrolling down
            // We want to scroll up (decrease offset) when scrollY is positive
            int scrollAmount = (int) -scrollY;
            scrollOffset = Math.max(0, Math.min(scrollOffset + scrollAmount, Math.max(0, lines.size() - getVisibleLines())));
            isScrolledToBottom = (scrollOffset >= lines.size() - getVisibleLines());
            return true;
        }
        return false;
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

    public void appendLine(String line) {
        if (lines.isEmpty()) {
            // Start with a new prompt if the lines are empty
            newPrompt();
        }

        // Ensure the cursor is within valid bounds
        cursor.setLine(Math.max(0, cursor.getLine()));

        // Wrap the line based on the terminal width
        int maxWidth = this.width - 2 * PADDING;
        List<String> wrappedLines = wrapText(line, maxWidth);

        if (!wrappedLines.isEmpty()) {
            // Add the wrapped lines
            lines.addAll(wrappedLines);

            if (isScrolledToBottom) {
                scrollToBottom();
            }

            // Adjust cursor to the last line and set the correct column position
            cursor.setLine(lines.size() - 1);
            cursor.setColumn(wrappedLines.get(wrappedLines.size() - 1).length());
        } else {
            // If wrapping resulted in no lines (edge case), reset cursor position to a safe state
            cursor.setColumn(0);
        }

        ensureCursorVisible();
    }

    public void appendError(String errorMessage) {

        int maxWidth = this.width - 2 * PADDING;
        List<String> wrappedLines = wrapText("\"ERROR: \"" + errorMessage, maxWidth);

        lines.addAll(wrappedLines);
        if (isScrolledToBottom) {
            scrollToBottom();
        }
        cursor.setLine(lines.size() - 1);
        cursor.setColumn(lines.get(cursor.getLine()).length());
        ensureCursorVisible();
    }


    public void appendResult(String result) {
        int maxWidth = this.width - 2 * PADDING;

        List<String> wrappedLines = wrapText(result, maxWidth);

        if (!result.isEmpty()) {
            lines.addAll(wrappedLines);
        }
        newPrompt();
        ensureCursorVisible();
    }

    public int getVisibleLines() {
        return (this.height - PADDING * 2) / LINE_HEIGHT;
    }

    private List<String> wrapText(String text, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        List<String> wrappedLines = new ArrayList<>();

        while (!text.isEmpty()) {
            int wrapWidth = font.width(text);
            if (wrapWidth <= maxWidth) {
                wrappedLines.add(text);
                break;
            }

            int wrapPos = font.plainSubstrByWidth(text, maxWidth).length();
            int spacePos = text.lastIndexOf(' ', wrapPos);

            if (spacePos != -1 && spacePos != 0) {
                wrapPos = spacePos;
            }

            wrappedLines.add(text.substring(0, wrapPos));
            text = text.substring(wrapPos).trim();
        }

        return wrappedLines;
    }

    private void ensureCursorVisible() {
        try {
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
        } catch (Exception e) {
            MCJSMod.LOGGER.error("Error in ensureCursorVisible", e);
        }
    }

    private void scrollToBottom() {
        scrollOffset = Math.max(0, lines.size() - getVisibleLines());
        isScrolledToBottom = true;
    }

    public void clear() {
        lines.clear();
        scrollOffset = 0;
        isScrolledToBottom = true;
        cursor.setLine(0);
        cursor.setColumn(0);
        newPrompt();
    }


    private void executeCommand(String command) {
        commandHistory.add(command);
        historyIndex = commandHistory.size();

        if (commandCallback != null) {
            commandCallback.accept(command);
        } else {
            appendLine("Command execution not implemented.");
        }
    }


    // Inner classes for renderer and input handler
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

                y += LINE_HEIGHT;
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

    private class TerminalInputHandler {
        private final Terminal terminal;

        public TerminalInputHandler(Terminal terminal) {
            this.terminal = terminal;
        }

        public boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {

            try {
                Cursor cursor = terminal.getCursor();
                List<String> lines = terminal.getLines();

                if (lines.isEmpty()) {
                    newPrompt();
                    return true;
                }

                if (cursor.getLine() < 0 || cursor.getLine() >= lines.size()) {
                    cursor.setLine(lines.size() - 1);
                    cursor.setColumn(lines.get(cursor.getLine()).length());
                    return true;
                }

                String currentLine = lines.get(cursor.getLine());

                switch (keyCode) {
                    case 256 -> terminal.setFocused(false); // Escape
                    case 259 -> { // Backspace
                        if (cursor.getColumn() > 2 && cursor.getColumn() <= currentLine.length()) {
                            lines.set(cursor.getLine(), currentLine.substring(0, cursor.getColumn() - 1) + currentLine.substring(cursor.getColumn()));
                            cursor.setColumn(cursor.getColumn() - 1);
                        }
                    }
                    case 257 -> { // Enter
                        if (currentLine.length() > 2) {
                            String command = currentLine.substring(2);
                            executeCommand(command);
                        } else {
                            newPrompt();
                        }
                        return true;
                    }
                    case 265 -> { // Up arrow
                        if (historyIndex > 0 && historyIndex <= commandHistory.size()) {
                            historyIndex--;
                            String historyCommand = commandHistory.get(historyIndex);
                            lines.set(cursor.getLine(), "> " + historyCommand);
                            cursor.setColumn(Math.min(historyCommand.length() + 2, currentLine.length()));
                        }
                    }
                    case 264 -> { // Down arrow
                        if (historyIndex < commandHistory.size() - 1) {
                            historyIndex++;
                            String historyCommand = commandHistory.get(historyIndex);
                            lines.set(cursor.getLine(), "> " + historyCommand);
                            cursor.setColumn(Math.min(historyCommand.length() + 2, currentLine.length()));
                        } else if (historyIndex == commandHistory.size() - 1) {
                            historyIndex++;
                            lines.set(cursor.getLine(), "> ");
                            cursor.setColumn(2);
                        }
                    }
                    case 263 -> { // Left arrow
                        if (cursor.getColumn() > 2) {
                            cursor.setColumn(cursor.getColumn() - 1);
                        }
                    }
                    case 262 -> { // Right arrow
                        if (cursor.getColumn() < currentLine.length()) {
                            cursor.setColumn(cursor.getColumn() + 1);
                        }
                    }
                    case 268 -> terminal.scrollToTop(); // Home
                    case 269 -> terminal.scrollToBottom(); // End
                    default -> {
                        return false;
                    }
                }
                ensureCursorVisible();
                return true;
            } catch (Exception e) {
                MCJSMod.LOGGER.error("Error handling key press: " + e.getMessage());
                return false;
            }
        }

        public boolean handleCharTyped(char codePoint, int modifiers) {
            Cursor cursor = terminal.getCursor();
            List<String> lines = terminal.getLines();

            // Ensure cursor is at a valid line, if not, adjust it
            cursor.setLine(Math.max(0, cursor.getLine()));

            if (lines.isEmpty()) {
                // Start with a new prompt if the lines are empty
                newPrompt();
                return true;
            }

            String currentLine = lines.get(cursor.getLine());
            if (cursor.getColumn() >= 2) {
                // Insert the character and wrap the text
                String newLine = currentLine.substring(0, cursor.getColumn()) + codePoint + currentLine.substring(cursor.getColumn());

                // Remove the current line and wrap the new one
                lines.remove(cursor.getLine());
                int maxWidth = this.terminal.getWidth() - 2 * PADDING;
                List<String> wrappedLines = wrapText(newLine, maxWidth);

                // Add the wrapped lines and adjust cursor
                lines.addAll(cursor.getLine(), wrappedLines);
                cursor.setLine(cursor.getLine() + wrappedLines.size() - 1);
                cursor.setColumn(wrappedLines.get(wrappedLines.size() - 1).length());
            }
            ensureCursorVisible();
            return true;
        }
    }
}
