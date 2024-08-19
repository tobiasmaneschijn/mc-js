package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.interfaces.IEditorRenderer;
import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.record.TextSegment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

class DefaultEditorRenderer implements IEditorRenderer {

    private static final int LINE_HEIGHT = 12;
    private static final int PADDING = 5;

    @Override
    public void render(GuiGraphics guiGraphics, TextEditor editor) {
        renderBackground(guiGraphics, editor);
        renderLineNumbers(guiGraphics, editor);
        renderText(guiGraphics, editor);
        renderCursor(guiGraphics, editor);
    }

    private void renderBackground(GuiGraphics guiGraphics, TextEditor editor) {
        guiGraphics.fill(editor.getX(), editor.getY(), editor.getX() + editor.getWidth(), editor.getY() + editor.getHeight(), 0xFF000000);
    }

    private void renderLineNumbers(GuiGraphics guiGraphics, TextEditor editor) {
        Font font = Minecraft.getInstance().font;
        int y = editor.getY() + PADDING - editor.getScrollOffset() * LINE_HEIGHT;
        for (int i = editor.getScrollOffset(); i < Math.min(editor.getLines().size(), editor.getScrollOffset() + editor.getVisibleLines()); i++) {
            guiGraphics.drawString(font, String.valueOf(i + 1), editor.getX() + PADDING, y, 0xAAAAAA);
            y += LINE_HEIGHT;
        }
    }

    private void renderText(GuiGraphics guiGraphics, TextEditor editor) {
        Font font = Minecraft.getInstance().font;
        int y = editor.getY() + PADDING - editor.getScrollOffset() * LINE_HEIGHT;
        List<String> lines = editor.getLines();
        int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;

        for (int i = editor.getScrollOffset(); i < Math.min(lines.size(), editor.getScrollOffset() + editor.getVisibleLines()); i++) {
            String line = lines.get(i);
            List<TextSegment> segments = editor.getSyntaxHighlighter().highlightLine(line);
            int x = editor.getX() + lineNumberWidth;

            Selection.SelectionInfo selectionInfo = editor.getSelection().getSelectionInfo(i, line.length());

            if (selectionInfo != null) {
                int highlightStartX = editor.getX() + lineNumberWidth + font.width(line.substring(0, selectionInfo.startX()));
                int highlightEndX = editor.getX() + lineNumberWidth + font.width(line.substring(0, selectionInfo.endX()));
                guiGraphics.fill(highlightStartX, y, highlightEndX, y + LINE_HEIGHT, 0x80808080);
            }

            for (TextSegment segment : segments) {
                guiGraphics.drawString(font, segment.text(), x, y, segment.color());
                x += font.width(segment.text());
            }

            y += LINE_HEIGHT;
        }
    }

    private void renderCursor(GuiGraphics guiGraphics, TextEditor editor) {
        List<String> lines = editor.getLines();
        Cursor cursor = editor.getCursor();
        if (editor.isFocused() && cursor.getLine() >= 0 && cursor.getLine() < lines.size()) {
            Font font = Minecraft.getInstance().font;
            int lineNumberWidth = font.width(String.valueOf(lines.size())) + PADDING * 2;
            String currentLine = lines.get(cursor.getLine());
            int safeColumn = Math.min(cursor.getColumn(), currentLine.length());
            int cursorX = editor.getX() + lineNumberWidth + font.width(currentLine.substring(0, safeColumn));
            int cursorY = editor.getY() + PADDING + (cursor.getLine() - editor.getScrollOffset()) * LINE_HEIGHT;

            if (cursorY >= editor.getY() && cursorY + LINE_HEIGHT <= editor.getY() + editor.getHeight()) {
                guiGraphics.fill(cursorX, cursorY, cursorX + 1, cursorY + LINE_HEIGHT, 0xFFFFFFFF);
            }
        }
    }
}
