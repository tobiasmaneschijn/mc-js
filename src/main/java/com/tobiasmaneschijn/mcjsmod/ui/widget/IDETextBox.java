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
import org.lwjgl.glfw.GLFW;

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

    private int selectionStartLine;
    private int selectionStartColumn;
    private int selectionEndLine;
    private int selectionEndColumn;

    public IDETextBox(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.lines = new ArrayList<>();
        this.lines.add("");
        this.cursorLine = 0;
        this.cursorColumn = 0;
        this.scrollOffset = 0;

        this.selectionStartLine = -1;
        this.selectionStartColumn = -1;
        this.selectionEndLine = -1;
        this.selectionEndColumn = -1;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        renderLineNumbers(guiGraphics);
        renderText(guiGraphics);
        renderCursor(guiGraphics);
    }

    public String getText() {
        return String.join("\n", lines);
    }

    public void setText(String text) {
        this.lines = new ArrayList<>(List.of(text.split("\n", -1)));
        this.cursorLine = Math.min(cursorLine, lines.size() - 1);
        this.cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
        clearSelection();
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

    private SelectionInfo getSelectionInfo(int line) {
        if (!hasSelection()) return null;

        int startLine = Math.min(selectionStartLine, selectionEndLine);
        int endLine = Math.max(selectionStartLine, selectionEndLine);

        if (line < startLine || line > endLine) return null;

        String lineText = lines.get(line);

        int startCol, endCol;
        if (selectionStartLine < selectionEndLine || (selectionStartLine == selectionEndLine && selectionStartColumn < selectionEndColumn)) {
            // Left-to-right selection
            startCol = (line == selectionStartLine) ? selectionStartColumn : 0;
            endCol = (line == selectionEndLine) ? selectionEndColumn : lineText.length();
        } else {
            // Right-to-left selection
            startCol = (line == selectionEndLine) ? selectionEndColumn : 0;
            endCol = (line == selectionStartLine) ? selectionStartColumn : lineText.length();
        }

        return new SelectionInfo(startCol, endCol);
    }

    private void renderText(GuiGraphics guiGraphics) {
        Font font = Minecraft.getInstance().font;
        int y = this.getY() + PADDING - scrollOffset * LINE_HEIGHT;
        int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;

        for (int i = scrollOffset; i < Math.min(lines.size(), scrollOffset + getVisibleLines()); i++) {
            String line = lines.get(i);
            List<TextSegment> segments = parseLineForHighlighting(line);
            int x = this.getX() + lineNumberWidth;

            SelectionInfo selectionInfo = getSelectionInfo(i);

            if (selectionInfo != null) {
                int highlightStartX = this.getX() + lineNumberWidth + font.width(line.substring(0, selectionInfo.startX()));
                int highlightEndX = this.getX() + lineNumberWidth + font.width(line.substring(0, selectionInfo.endX()));
                guiGraphics.fill(highlightStartX, y, highlightEndX, y + LINE_HEIGHT, 0x80808080);
            }

            for (TextSegment segment : segments) {
                guiGraphics.drawString(font, segment.text, x, y, segment.color);
                x += font.width(segment.text);
            }

            y += LINE_HEIGHT;
        }
    }

    private record SelectionInfo(int startX, int endX) {}
    private void normalizeSelection() {
        if (selectionStartLine > selectionEndLine ||
                (selectionStartLine == selectionEndLine && selectionStartColumn > selectionEndColumn)) {
            // Swap start and end if selection is right-to-left
            int tempLine = selectionStartLine;
            int tempColumn = selectionStartColumn;
            selectionStartLine = selectionEndLine;
            selectionStartColumn = selectionEndColumn;
            selectionEndLine = tempLine;
            selectionEndColumn = tempColumn;
        }

        // Ensure selection bounds are within valid ranges
        selectionStartLine = Math.max(0, Math.min(selectionStartLine, lines.size() - 1));
        selectionEndLine = Math.max(0, Math.min(selectionEndLine, lines.size() - 1));
        selectionStartColumn = Math.max(0, Math.min(selectionStartColumn, lines.get(selectionStartLine).length()));
        selectionEndColumn = Math.max(0, Math.min(selectionEndColumn, lines.get(selectionEndLine).length()));
    }

    private void renderCursor(GuiGraphics guiGraphics) {
        if (this.isFocused() && cursorLine >= 0 && cursorLine < lines.size()) {
            Font font = Minecraft.getInstance().font;
            int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;
            String currentLine = lines.get(cursorLine);
            int safeColumn = Math.min(cursorColumn, currentLine.length());
            int cursorX = this.getX() + lineNumberWidth + font.width(currentLine.substring(0, safeColumn));
            int cursorY = this.getY() + PADDING + (cursorLine - scrollOffset) * LINE_HEIGHT;

            if (cursorY >= this.getY() && cursorY + LINE_HEIGHT <= this.getY() + this.height) {
                guiGraphics.fill(cursorX, cursorY, cursorX + 1, cursorY + LINE_HEIGHT, 0xFFFFFFFF);
            }
        }
    }
    private List<TextSegment> parseLineForHighlighting(String line) {
        List<TextSegment> segments = new ArrayList<>();

        // Keywords
        String keywords = "\\b(var|let|const|if|else|for|while|do|switch|case|break|continue|" +
                "function|return|try|catch|finally|throw|class|extends|new|this|super|" +
                "import|export|async|await|yield)\\b";

        // Built-in objects and functions
        String builtIns = "\\b(Array|Object|String|Number|Boolean|Function|Symbol|RegExp|Date|" +
                "Math|JSON|console|window|document|null|undefined|NaN|Infinity)\\b";

        // Operators
        String operators = "[=+\\-*/%&|^~<>!?:]";

        // Numbers
        String numbers = "\\b\\d+(\\.\\d*)?([eE][+-]?\\d+)?\\b";

        // Strings
        String strings = "\"(?:\\\\\"|[^\"])*\"|'(?:\\\\'|[^'])*'|`(?:\\\\`|[^`])*`";

        // Comments (single-line only for simplicity)
        String comments = "//.*$";

        Pattern pattern = Pattern.compile(
                String.join("|", keywords, builtIns, operators, numbers, strings, comments),
                Pattern.MULTILINE
        );

        Matcher matcher = pattern.matcher(line);
        int lastEnd = 0;

        while (matcher.find()) {
            // Add any text before the match as plain text
            if (matcher.start() > lastEnd) {
                segments.add(new TextSegment(line.substring(lastEnd, matcher.start()), 0xFFFFFF));
            }

            String match = matcher.group();
            int color;

            if (match.matches(keywords)) {
                color = 0xFF7777; // Red for keywords
            } else if (match.matches(builtIns)) {
                color = 0x7777FF; // Blue for built-ins
            } else if (match.matches(operators)) {
                color = 0xFFFF77; // Yellow for operators
            } else if (match.matches(numbers)) {
                color = 0x77FFFF; // Cyan for numbers
            } else if (match.matches(strings)) {
                color = 0x77FF77; // Green for strings
            } else if (match.matches(comments)) {
                color = 0x777777; // Gray for comments
            } else {
                color = 0xFFFFFF; // White for unrecognized (shouldn't happen)
            }

            segments.add(new TextSegment(match, color));
            lastEnd = matcher.end();
        }

        // Add any remaining text as plain text
        if (lastEnd < line.length()) {
            segments.add(new TextSegment(line.substring(lastEnd), 0xFFFFFF));
        }

        return segments;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        try {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Key pressed: " + keyCode), false);
            }

            // Ensure cursorLine is within valid range
            cursorLine = Math.max(0, Math.min(cursorLine, lines.size() - 1));
            // Ensure cursorColumn is within valid range for the current line
            cursorColumn = Math.max(0, Math.min(cursorColumn, lines.get(cursorLine).length()));


            boolean isCtrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
            boolean isShiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

            if(isCtrlPressed)
            {
                switch (keyCode) {
                    case 65 -> { // Ctrl + A
                        selectAll();
                        return true;
                    }
                    case 67 -> { // Ctrl + C
                        copy();
                        return true;
                    }
                    case 86 -> { // Ctrl + V
                        paste();
                        return true;
                    }
                    case 88 -> { // Ctrl + X
                        cut();
                        return true;
                    }
                    // ctrl + D
                    case 68 -> {
                        duplicateLine();
                        return true;
                    }
                    // ctrl + Z
                    case 90 -> {
                        undo();
                        return true;
                    }
                    // ctrl + Y
                    case 89 -> {
                        redo();
                        return true;
                    }
                }
            }
            else if (isShiftPressed) {
                switch (keyCode) {
                    case 263 -> { // Shift + Left arrow
                        if (!hasSelection()) {
                            selectionStartLine = cursorLine;
                            selectionStartColumn = cursorColumn;
                        }
                        cursorColumn = Math.max(0, cursorColumn - 1);
                        selectionEndLine = cursorLine;
                        selectionEndColumn = cursorColumn;
                        logDebugInfo("Shift + Left arrow");
                        return true;
                    }
                    case 262 -> { // Shift + Right arrow
                        if (!hasSelection()) {
                            selectionStartLine = cursorLine;
                            selectionStartColumn = cursorColumn;
                        }
                        cursorColumn = Math.min(lines.get(cursorLine).length(), cursorColumn + 1);
                        selectionEndLine = cursorLine;
                        selectionEndColumn = cursorColumn;
                        return true;
                    }
                    case 265 -> { // Shift + Up arrow
                        if (!hasSelection()) {
                            selectionStartLine = cursorLine;
                            selectionStartColumn = cursorColumn;
                        }
                        cursorLine = Math.max(0, cursorLine - 1);
                        cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
                        selectionEndLine = cursorLine;
                        selectionEndColumn = cursorColumn;
                        return true;
                    }
                    case 264 -> { // Shift + Down arrow
                        if (!hasSelection()) {
                            selectionStartLine = cursorLine;
                            selectionStartColumn = cursorColumn;
                        }
                        cursorLine = Math.min(lines.size() - 1, cursorLine + 1);
                        cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
                        selectionEndLine = cursorLine;
                        selectionEndColumn = cursorColumn;
                        return true;
                    }
                }
            }


            switch (keyCode) {
                case 256 -> { // Escape
                    setFocused(false);
                    return true;
                }
                case 259 -> { // Backspace
                    if (hasSelection()) {
                        deleteSelection();
                    } else if (cursorColumn > 0) {
                        String currentLine = lines.get(cursorLine);
                        lines.set(cursorLine, currentLine.substring(0, cursorColumn - 1) + currentLine.substring(cursorColumn));
                        cursorColumn--;
                        addUndoAction();
                    } else if (cursorLine > 0) {
                        String currentLine = lines.remove(cursorLine);
                        cursorLine--;
                        cursorColumn = lines.get(cursorLine).length();
                        lines.set(cursorLine, lines.get(cursorLine) + currentLine);
                        addUndoAction();
                    }
                    clearSelection();
                    return true;
                }
                case 261 -> { // Delete
                    if (hasSelection()) {
                        deleteSelection();
                    } else if (cursorColumn < lines.get(cursorLine).length()) {
                        String currentLine = lines.get(cursorLine);
                        lines.set(cursorLine, currentLine.substring(0, cursorColumn) + currentLine.substring(cursorColumn + 1));
                        addUndoAction();
                    } else if (cursorLine < lines.size() - 1) {
                        String currentLine = lines.remove(cursorLine + 1);
                        lines.set(cursorLine, lines.get(cursorLine) + currentLine);
                        addUndoAction();
                    }
                    clearSelection();
                    return true;
                }
                case 260 -> { // Home
                    cursorColumn = 0;
                    return true;
                }
                case 269 -> { // End
                    cursorColumn = lines.get(cursorLine).length();
                    return true;
                }
                case 266 -> { // Page up
                    scrollOffset = Math.max(0, scrollOffset - getVisibleLines());
                    return true;
                }
                case 267 -> { // Page down
                    scrollOffset = Math.min(lines.size() - getVisibleLines(), scrollOffset + getVisibleLines());
                    return true;
                }
                case 257 -> { // Enter
                    if (hasSelection()) {
                        deleteSelection();
                    }
                    String currentLine = lines.get(cursorLine);
                    String newLine = currentLine.substring(cursorColumn);
                    lines.set(cursorLine, currentLine.substring(0, cursorColumn));
                    lines.add(cursorLine + 1, newLine);
                    cursorLine++;
                    cursorColumn = 0;
                    return true;
                }
                case 263 -> { // Left arrow
                    if (hasSelection()) {
                        clearSelection();
                    }
                    if (cursorColumn > 0) {
                        cursorColumn--;
                    } else if (cursorLine > 0) {
                        cursorLine--;
                        cursorColumn = lines.get(cursorLine).length();
                    }
                    return true;
                }
                case 262 -> { // Right arrow
                    if (hasSelection()) {
                        clearSelection();
                    }
                    if (cursorColumn < lines.get(cursorLine).length()) {
                        cursorColumn++;
                    } else if (cursorLine < lines.size() - 1) {
                        cursorLine++;
                        cursorColumn = 0;
                    }
                    return true;
                }
                case 265 -> { // Up arrow
                    if (hasSelection()) {
                        clearSelection();
                    }
                    if (cursorLine > 0) {
                        cursorLine--;
                        cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
                    }
                    return true;
                }
                case 264 -> { // Down arrow
                    if (hasSelection()) {
                        clearSelection();
                    }
                    if (cursorLine < lines.size() - 1) {
                        cursorLine++;
                        cursorColumn = Math.min(cursorColumn, lines.get(cursorLine).length());
                    }
                    return true;
                }
                case 258 -> { // Tab
                    if (hasSelection()) {
                        clearSelection();
                    }
                    String currentLine = lines.get(cursorLine);
                    lines.set(cursorLine, currentLine.substring(0, cursorColumn) + "    " + currentLine.substring(cursorColumn));
                    cursorColumn += 4;
                    return true;
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        } catch (Exception e) {
            // Log the exception
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Error in keyPressed: " + e.getMessage()), false);
            e.printStackTrace();
            return false;
        }
    }



    private boolean hasSelection() {
        return selectionStartLine != -1 && selectionEndLine != -1 &&
                (selectionStartLine != selectionEndLine || selectionStartColumn != selectionEndColumn);
    }

    private void deleteSelection() {
        if (!hasSelection()) return;

        int startLine = Math.min(selectionStartLine, selectionEndLine);
        int endLine = Math.max(selectionStartLine, selectionEndLine);
        int startColumn, endColumn;

        if (selectionStartLine < selectionEndLine || (selectionStartLine == selectionEndLine && selectionStartColumn < selectionEndColumn)) {
            // Left-to-right selection
            startColumn = selectionStartColumn;
            endColumn = selectionEndColumn;
        } else {
            // Right-to-left selection
            startColumn = selectionEndColumn;
            endColumn = selectionStartColumn;
        }

        if (startLine == endLine) {
            String line = lines.get(startLine);
            lines.set(startLine, line.substring(0, startColumn) + line.substring(endColumn));
        } else {
            String startLineContent = lines.get(startLine).substring(0, startColumn);
            String endLineContent = lines.get(endLine).substring(endColumn);
            lines.subList(startLine + 1, endLine + 1).clear();
            lines.set(startLine, startLineContent + endLineContent);
        }

        cursorLine = startLine;
        cursorColumn = startColumn;
        addUndoAction();
        clearSelection();
    }

    private void clearSelection() {
        selectionStartLine = selectionEndLine = cursorLine;
        selectionStartColumn = selectionEndColumn = cursorColumn;
        logDebugInfo("clearSelection");
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        try {
            if (hasSelection()) {
                deleteSelection();
            }
            String currentLine = lines.get(cursorLine);
            lines.set(cursorLine, currentLine.substring(0, cursorColumn) + codePoint + currentLine.substring(cursorColumn));
            cursorColumn++;
            clearSelection();
            addUndoAction();
            logDebugInfo("charTyped");
            return true;
        } catch (Exception e) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Error in charTyped: " + e.getMessage()), false);
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            setFocused(true);
            updateCursorPosition(mouseX, mouseY);
            if (button == 0) { // Left click
                selectionStartLine = selectionEndLine = cursorLine;
                selectionStartColumn = selectionEndColumn = cursorColumn;
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            updateCursorPosition(mouseX, mouseY);
            selectionEndLine = cursorLine;
            selectionEndColumn = cursorColumn;
            logDebugInfo("mouseDragged");
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    private void updateCursorPosition(double mouseX, double mouseY) {
        if (mouseX < this.getX() || mouseX > this.getX() + this.width ||
                mouseY < this.getY() || mouseY > this.getY() + this.height) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;

        cursorLine = scrollOffset + (int)((mouseY - this.getY() - PADDING) / LINE_HEIGHT);
        cursorLine = Math.max(0, Math.min(cursorLine, lines.size() - 1));

        String line = lines.get(cursorLine);
        int x = this.getX() + lineNumberWidth;
        int textX = (int)mouseX - x;

        cursorColumn = 0;
        for (int i = 0; i <= line.length(); i++) {
            int width = font.width(line.substring(0, i));
            if (width > textX) {
                cursorColumn = i;
                if (i > 0 && textX < width - font.width(line.substring(i - 1, i)) / 2) {
                    cursorColumn--;
                }
                break;
            }
        }
        cursorColumn = Math.min(cursorColumn, line.length());
    }

    private void logDebugInfo(String methodName) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal(
                    String.format("%s - Cursor: (%d, %d), Selection: (%d, %d) to (%d, %d), HasSelection: %b",
                            methodName, cursorLine, cursorColumn,
                            selectionStartLine, selectionStartColumn,
                            selectionEndLine, selectionEndColumn,
                            hasSelection())
            ), false);
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

    private void selectAll() {
        selectionStartLine = 0;
        selectionStartColumn = 0;
        selectionEndLine = lines.size() - 1;
        selectionEndColumn = lines.get(selectionEndLine).length();
        cursorLine = selectionEndLine;
        cursorColumn = selectionEndColumn;
    }

    private void copy() {
        if (hasSelection()) {
            String selectedText = getSelectedText();
            Minecraft.getInstance().keyboardHandler.setClipboard(selectedText);
        }
    }

    private void cut() {
        if (hasSelection()) {
            copy();
            deleteSelection();
            addUndoAction();
        }
    }

    private void paste() {
        String clipboardText = Minecraft.getInstance().keyboardHandler.getClipboard();
        if (!clipboardText.isEmpty()) {
            if (hasSelection()) {
                deleteSelection();
            }
            insertText(clipboardText);
            addUndoAction();
        }
    }

    private void duplicateLine() {
        String currentLine = lines.get(cursorLine);
        lines.add(cursorLine + 1, currentLine);
        cursorLine++;
        addUndoAction();
    }

    private String getSelectedText() {
        if (!hasSelection()) return "";

        StringBuilder sb = new StringBuilder();
        int startLine = Math.min(selectionStartLine, selectionEndLine);
        int endLine = Math.max(selectionStartLine, selectionEndLine);
        int startCol = (startLine == selectionStartLine) ? selectionStartColumn : selectionEndColumn;
        int endCol = (endLine == selectionEndLine) ? selectionEndColumn : selectionStartColumn;

        for (int i = startLine; i <= endLine; i++) {
            String line = lines.get(i);
            if (i == startLine && i == endLine) {
                sb.append(line, Math.min(startCol, endCol), Math.max(startCol, endCol));
            } else if (i == startLine) {
                sb.append(line.substring(startCol)).append("\n");
            } else if (i == endLine) {
                sb.append(line, 0, endCol);
            } else {
                sb.append(line).append("\n");
            }
        }

        return sb.toString();
    }

    private void insertText(String text) {
        String[] insertLines = text.split("\n", -1);
        String currentLine = lines.get(cursorLine);
        String beforeCursor = currentLine.substring(0, cursorColumn);
        String afterCursor = currentLine.substring(cursorColumn);

        lines.set(cursorLine, beforeCursor + insertLines[0]);
        cursorColumn += insertLines[0].length();

        for (int i = 1; i < insertLines.length; i++) {
            cursorLine++;
            lines.add(cursorLine, insertLines[i]);
            cursorColumn = insertLines[i].length();
        }

        lines.set(cursorLine, lines.get(cursorLine) + afterCursor);
    }

    private final List<UndoAction> undoStack = new ArrayList<>();
    private final List<UndoAction> redoStack = new ArrayList<>();

    private static class UndoAction {
        List<String> lines;
        int cursorLine;
        int cursorColumn;

        UndoAction(List<String> lines, int cursorLine, int cursorColumn) {
            this.lines = new ArrayList<>(lines);
            this.cursorLine = cursorLine;
            this.cursorColumn = cursorColumn;
        }
    }

    private void addUndoAction() {
        undoStack.add(new UndoAction(lines, cursorLine, cursorColumn));
        redoStack.clear();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            UndoAction action = undoStack.remove(undoStack.size() - 1);
            redoStack.add(new UndoAction(lines, cursorLine, cursorColumn));
            lines = action.lines;
            cursorLine = action.cursorLine;
            cursorColumn = action.cursorColumn;
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            UndoAction action = redoStack.remove(redoStack.size() - 1);
            undoStack.add(new UndoAction(lines, cursorLine, cursorColumn));
            lines = action.lines;
            cursorLine = action.cursorLine;
            cursorColumn = action.cursorColumn;
        }
    }

}