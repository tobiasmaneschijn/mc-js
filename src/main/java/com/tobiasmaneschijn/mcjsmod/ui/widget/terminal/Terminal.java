package com.tobiasmaneschijn.mcjsmod.ui.widget.terminal;

import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.Cursor;
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

    public Terminal(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.lines = new ArrayList<>();
        this.lines.add("Welcome to the Terminal!");
        this.cursor = new Cursor();
        this.renderer = new TerminalRenderer();
        this.inputHandler = new TerminalInputHandler(this);
        this.scrollOffset = 0;
        this.commandHistory = new ArrayList<>();
        this.historyIndex = -1;
        this.currentInput = "";
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
    public List<String> getLines() { return lines; }
    public Cursor getCursor() { return cursor; }
    public int getScrollOffset() { return scrollOffset; }
    public void setScrollOffset(int offset) { this.scrollOffset = offset; }

    public void appendLine(String line) {
        lines.add(line);
        if (isScrolledToBottom) {
            scrollToBottom();
        }
        cursor.setLine(lines.size() - 1);
        cursor.setColumn(line.length());
        ensureCursorVisible();
    }

    public int getVisibleLines() {
        return (this.height - PADDING * 2) / LINE_HEIGHT;
    }

    private void ensureCursorVisible() {
        int totalLines = lines.size();
        int visibleLines = getVisibleLines();

        if (cursor.getLine() < scrollOffset) {
            scrollOffset = cursor.getLine();
        } else if (cursor.getLine() >= scrollOffset + visibleLines) {
            scrollOffset = Math.max(0, cursor.getLine() - visibleLines + 1);
        }
        isScrolledToBottom = (scrollOffset >= totalLines - visibleLines);
    }

    private void scrollToBottom() {
        scrollOffset = Math.max(0, lines.size() - getVisibleLines());
        isScrolledToBottom = true;
    }

    public void clear() {
        lines.clear();
        scrollOffset = 0;
        isScrolledToBottom = true;
    }

    private void executeCommand(String command) {
        commandHistory.add(command);
        historyIndex = commandHistory.size();

        if (commandCallback != null) {
            commandCallback.accept(command);
        } else {
            appendLine("Command execution not implemented.");
        }

        newPrompt();
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
                guiGraphics.drawString(font, lines.get(i), terminal.getX() + PADDING, y, 0xFFFFFF);
                y += LINE_HEIGHT;
            }
        }

        private void renderCursor(GuiGraphics guiGraphics, Terminal terminal) {
            List<String> lines = terminal.getLines();
            Cursor cursor = terminal.getCursor();
            if (cursor.getLine() >= terminal.getScrollOffset() && cursor.getLine() < terminal.getScrollOffset() + terminal.getVisibleLines()) {
                Font font = Minecraft.getInstance().font;
                String currentLine = lines.get(cursor.getLine());
                int cursorX = terminal.getX() + PADDING + font.width(currentLine.substring(0, Math.min(cursor.getColumn(), currentLine.length())));
                int cursorY = terminal.getY() + PADDING + (cursor.getLine() - terminal.getScrollOffset()) * LINE_HEIGHT;

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
            Cursor cursor = terminal.getCursor();
            List<String> lines = terminal.getLines();
            String currentLine = lines.get(cursor.getLine());

            switch (keyCode) {
                case 256 -> terminal.setFocused(false); // Escape
                case 259 -> { // Backspace
                    if (cursor.getColumn() > 2) {
                        lines.set(cursor.getLine(), currentLine.substring(0, cursor.getColumn() - 1) + currentLine.substring(cursor.getColumn()));
                        cursor.setColumn(cursor.getColumn() - 1);
                    }
                }
                case 257 -> { // Enter
                    String command = currentLine.substring(2);
                    executeCommand(command);
                }
                case 265 -> { // Up arrow
                    if (historyIndex > 0) {
                        historyIndex--;
                        String historyCommand = commandHistory.get(historyIndex);
                        lines.set(cursor.getLine(), "> " + historyCommand);
                        cursor.setColumn(historyCommand.length() + 2);
                    }
                }
                case 264 -> { // Down arrow
                    if (historyIndex < commandHistory.size() - 1) {
                        historyIndex++;
                        String historyCommand = commandHistory.get(historyIndex);
                        lines.set(cursor.getLine(), "> " + historyCommand);
                        cursor.setColumn(historyCommand.length() + 2);
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

                // Scroll to top
                case 268 -> { // Home
                    terminal.scrollToTop();
                }

                // Scroll to bottom
                case 269 -> { // End
                    terminal.scrollToBottom();
                }


                default -> {
                    return false;
                }
            }
            ensureCursorVisible();
            return true;
        }

        public boolean handleCharTyped(char codePoint, int modifiers) {
            Cursor cursor = terminal.getCursor();
            List<String> lines = terminal.getLines();

            String currentLine = lines.get(cursor.getLine());
            if (cursor.getColumn() >= 2) {
                lines.set(cursor.getLine(), currentLine.substring(0, cursor.getColumn()) + codePoint + currentLine.substring(cursor.getColumn()));
                cursor.setColumn(cursor.getColumn() + 1);
            }
            ensureCursorVisible();
            return true;
        }
    }
}
