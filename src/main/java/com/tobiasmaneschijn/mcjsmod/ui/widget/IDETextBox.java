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
                int segmentWidth = font.width(segment.text);
                boolean isSelected = isTextSelected(i, x - this.getX() - lineNumberWidth, x - this.getX() - lineNumberWidth + segmentWidth);

                if (isSelected) {
                    guiGraphics.fill(x, y, x + segmentWidth, y + LINE_HEIGHT, 0x80808080);
                }

                guiGraphics.drawString(font, segment.text, x, y, segment.color);
                x += segmentWidth;
            }

            y += LINE_HEIGHT;
        }
    }

    private boolean isTextSelected(int line, int startX, int endX) {
        if (selectionStartLine == -1 || selectionEndLine == -1) return false;

        int startLine = Math.min(selectionStartLine, selectionEndLine);
        int endLine = Math.max(selectionStartLine, selectionEndLine);

        if (line < startLine || line > endLine) return false;

        Font font = Minecraft.getInstance().font;
        String lineText = lines.get(line);

        if (line == startLine && line == endLine) {
            int start = Math.min(selectionStartColumn, selectionEndColumn);
            int end = Math.max(selectionStartColumn, selectionEndColumn);
            int startPixel = font.width(lineText.substring(0, start));
            int endPixel = font.width(lineText.substring(0, end));
            return startX < endPixel && endX > startPixel;
        } else if (line == startLine) {
            int start = selectionStartLine < selectionEndLine ? selectionStartColumn : selectionEndColumn;
            int startPixel = font.width(lineText.substring(0, start));
            return endX > startPixel;
        } else if (line == endLine) {
            int end = selectionStartLine < selectionEndLine ? selectionEndColumn : selectionStartColumn;
            int endPixel = font.width(lineText.substring(0, end));
            return startX < endPixel;
        }

        return true;
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

        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Key pressed: " + keyCode), false);
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
                } else if (cursorLine > 0) {
                    String currentLine = lines.remove(cursorLine);
                    cursorLine--;
                    cursorColumn = lines.get(cursorLine).length();
                    lines.set(cursorLine, lines.get(cursorLine) + currentLine);
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
                } else if (cursorLine < lines.size() - 1) {
                    String currentLine = lines.remove(cursorLine + 1);
                    lines.set(cursorLine, lines.get(cursorLine) + currentLine);
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


    private boolean hasSelection() {
        return selectionStartLine != -1 && selectionEndLine != -1 &&
                (selectionStartLine != selectionEndLine || selectionStartColumn != selectionEndColumn);
    }

    private void deleteSelection() {
        int startLine = Math.min(selectionStartLine, selectionEndLine);
        int endLine = Math.max(selectionStartLine, selectionEndLine);
        int startColumn = selectionStartLine < selectionEndLine ? selectionStartColumn : selectionEndColumn;
        int endColumn = selectionStartLine < selectionEndLine ? selectionEndColumn : selectionStartColumn;

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
    }

    private void clearSelection() {
        selectionStartLine = -1;
        selectionStartColumn = -1;
        selectionEndLine = -1;
        selectionEndColumn = -1;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (hasSelection()) {
            deleteSelection();
        }
        String currentLine = lines.get(cursorLine);
        lines.set(cursorLine, currentLine.substring(0, cursorColumn) + codePoint + currentLine.substring(cursorColumn));
        cursorColumn++;
        clearSelection();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            setFocused(true);
            updateCursorPosition(mouseX, mouseY);
            if (button == 0) { // Left click
                selectionStartLine = cursorLine;
                selectionStartColumn = cursorColumn;
                selectionEndLine = cursorLine;
                selectionEndColumn = cursorColumn;
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
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void updateCursorPosition(double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;

        cursorLine = scrollOffset + (int)((mouseY - this.getY() - PADDING) / LINE_HEIGHT);
        cursorLine = Math.max(0, Math.min(cursorLine, lines.size() - 1));

        String line = lines.get(cursorLine);
        int x = this.getX() + lineNumberWidth;
        int textX = (int)mouseX - x;

        for (cursorColumn = 0; cursorColumn <= line.length(); cursorColumn++) {
            int width = font.width(line.substring(0, cursorColumn));
            if (width > textX) {
                if (cursorColumn > 0 && textX < width - font.width(line.substring(cursorColumn - 1, cursorColumn)) / 2) {
                    cursorColumn--;
                }
                break;
            }
        }
        cursorColumn = Math.min(cursorColumn, line.length());
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