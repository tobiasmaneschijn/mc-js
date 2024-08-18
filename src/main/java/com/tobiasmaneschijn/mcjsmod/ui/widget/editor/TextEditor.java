package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;



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

public class TextEditor extends AbstractWidget {
    private final List<String> lines;
    private final Cursor cursor;
    private final Selection selection;
    private final IEditorRenderer renderer;
    private final IInputHandler inputHandler;
    private final ISyntaxHighlighter syntaxHighlighter;
    private final UndoManager undoManager;
    private int scrollOffset;
    private static final int LINE_HEIGHT = 12;
    private static final int PADDING = 5;

    public TextEditor(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        this.lines = new ArrayList<>();
        this.lines.add("");
        this.cursor = new Cursor();
        this.selection = new Selection();
        this.renderer = new DefaultEditorRenderer();
        this.inputHandler = new DefaultInputHandler(this);
        this.syntaxHighlighter = new JavaScriptSyntaxHighlighter();
        this.undoManager = new UndoManager();
        this.scrollOffset = 0;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return inputHandler.handleMouseClick(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return inputHandler.handleMouseDrag(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.literal("Text Editor"));
    }

    // Getters and setters
    public List<String> getLines() { return lines; }
    public Cursor getCursor() { return cursor; }
    public Selection getSelection() { return selection; }
    public ISyntaxHighlighter getSyntaxHighlighter() { return syntaxHighlighter; }
    public UndoManager getUndoManager() { return undoManager; }
    public int getScrollOffset() { return scrollOffset; }
    public void setScrollOffset(int offset) { this.scrollOffset = offset; }

    public String getText() {
        return String.join("\n", lines);
    }

    public void setText(String text) {
        this.lines.clear();
        this.lines.addAll(List.of(text.split("\n", -1)));
        this.cursor.setLine(Math.min(cursor.getLine(), lines.size() - 1));
        this.cursor.setColumn(Math.min(cursor.getColumn(), lines.get(cursor.getLine()).length()));
        selection.clear();
    }

    public int getVisibleLines() {
        return (this.height - PADDING * 2) / LINE_HEIGHT;
    }

    public void updateCursorPosition(double mouseX, double mouseY) {
        if (mouseX < this.getX() || mouseX > this.getX() + this.width ||
                mouseY < this.getY() || mouseY > this.getY() + this.height) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;


        cursor.setLine(scrollOffset + (int)((mouseY - this.getY() - PADDING) / LINE_HEIGHT));
        cursor.setLine(Math.max(0, Math.min(cursor.getLine(), lines.size() - 1)));

        String line = lines.get(cursor.getLine());
        int x = this.getX() + lineNumberWidth;
        int textX = (int)mouseX - x;

        cursor.setColumn(0);
        for (int i = 0; i <= line.length(); i++) {
            int width = font.width(line.substring(0, i));
            if (width > textX) {
                cursor.setColumn(i);
                if (i > 0 && textX < width - font.width(line.substring(i - 1, i)) / 2) {
                    cursor.setColumn(cursor.getColumn() - 1);
                }
                break;
            }
        }
        cursor.setColumn(Math.min(cursor.getColumn(), line.length()));
    }

    public void deleteSelection() {
        if (!selection.hasSelection()) return;

        int startLine = Math.min(selection.getStartLine(), selection.getEndLine());
        int endLine = Math.max(selection.getStartLine(), selection.getEndLine());
        int startColumn, endColumn;

        if (selection.getStartLine() < selection.getEndLine() || (selection.getStartLine() == selection.getEndLine() && selection.getStartColumn() < selection.getEndColumn())) {
            // Left-to-right selection
            startColumn = selection.getStartColumn();
            endColumn = selection.getEndColumn();
        } else {
            // Right-to-left selection
            startColumn = selection.getEndColumn();
            endColumn = selection.getStartColumn();
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

        cursor.setLine(startLine);
        cursor.setColumn(startColumn);
        undoManager.addUndoAction(new UndoAction(lines, cursor));
        selection.clear();
    }

}