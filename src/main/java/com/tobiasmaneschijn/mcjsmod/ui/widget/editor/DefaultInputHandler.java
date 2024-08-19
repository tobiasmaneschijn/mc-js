package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static com.tobiasmaneschijn.mcjsmod.ui.widget.editor.EditorLogger.logDebugInfo;

class DefaultInputHandler implements IInputHandler {
    private final TextEditor editor;

    public DefaultInputHandler(TextEditor editor) {
        this.editor = editor;
    }

    @Override
    public boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        try {
     /*       if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Key pressed: " + keyCode), false);
            }*/

            Cursor cursor = editor.getCursor();
            Selection selection = editor.getSelection();
            List<String> lines = editor.getLines();
            UndoManager undoManager = editor.getUndoManager();

            // Ensure cursor is within valid range
            cursor.setLine(Math.max(0, Math.min(cursor.getLine(), lines.size() - 1)));
            cursor.setColumn(Math.max(0, Math.min(cursor.getColumn(), lines.get(cursor.getLine()).length())));

            boolean isCtrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
            boolean isShiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

            if (isCtrlPressed) {
                switch (keyCode) {
                    case 65 -> { selectAll(); return true; } // Ctrl + A
                    case 67 -> { copy(); return true; } // Ctrl + C
                    case 86 -> { paste(); return true; } // Ctrl + V
                    case 88 -> { cut(); return true; } // Ctrl + X
                    case 68 -> { duplicateLine(); return true; } // Ctrl + D
                    case 90 -> { undoManager.undo(); return true; } // Ctrl + Z
                    case 89 -> { undoManager.redo(); return true; } // Ctrl + Y
                }
            } else if (isShiftPressed) {
                switch (keyCode) {
                    case 263 -> { // Shift + Left arrow
                        if (!selection.hasSelection()) {
                            selection.setStart(cursor.getLine(), cursor.getColumn());
                        }
                        moveCursorLeft(cursor, lines);
                        selection.setEnd(cursor.getLine(), cursor.getColumn());
                        return true;
                    }
                    case 262 -> { // Shift + Right arrow
                        if (!selection.hasSelection()) {
                            selection.setStart(cursor.getLine(), cursor.getColumn());
                        }
                        moveCursorRight(cursor, lines);
                        selection.setEnd(cursor.getLine(), cursor.getColumn());
                        return true;
                    }
                    case 265 -> { // Shift + Up arrow
                        if (!selection.hasSelection()) {
                            selection.setStart(cursor.getLine(), cursor.getColumn());
                        }
                        moveCursorUp(cursor, lines);
                        selection.setEnd(cursor.getLine(), cursor.getColumn());
                        return true;
                    }
                    case 264 -> { // Shift + Down arrow
                        if (!selection.hasSelection()) {
                            selection.setStart(cursor.getLine(), cursor.getColumn());
                        }
                        moveCursorDown(cursor, lines);
                        selection.setEnd(cursor.getLine(), cursor.getColumn());
                        return true;
                    }
                }
            }

            switch (keyCode) {
                case 256 -> { editor.setFocused(false); return true; } // Escape
                case 259 -> { // Backspace
                    if (selection.hasSelection()) {
                        editor.deleteSelection();
                    } else if (cursor.getColumn() > 0) {
                        deleteCharacterBefore(cursor, lines);
                        undoManager.addUndoAction(new UndoAction(lines, cursor));
                    } else if (cursor.getLine() > 0) {
                        mergeLineWithPrevious(cursor, lines);
                        undoManager.addUndoAction(new UndoAction(lines, cursor));
                    }
                    selection.clear();
                    return true;
                }
                case 261 -> { // Delete
                    if (selection.hasSelection()) {
                        editor.deleteSelection();
                    } else if (cursor.getColumn() < lines.get(cursor.getLine()).length()) {
                        deleteCharacterAfter(cursor, lines);
                        undoManager.addUndoAction(new UndoAction(lines, cursor));
                    } else if (cursor.getLine() < lines.size() - 1) {
                        mergeLineWithNext(cursor, lines);
                        undoManager.addUndoAction(new UndoAction(lines, cursor));
                    }
                    selection.clear();
                    return true;
                }
                case 260 -> { cursor.setColumn(0); return true; } // Home
                case 269 -> { cursor.setColumn(lines.get(cursor.getLine()).length()); return true; } // End
                case 266 -> { // Page up
                    editor.setScrollOffset(Math.max(0, editor.getScrollOffset() - editor.getVisibleLines()));
                    return true;
                }
                case 267 -> { // Page down
                    editor.setScrollOffset(Math.min(lines.size() - editor.getVisibleLines(), editor.getScrollOffset() + editor.getVisibleLines()));
                    return true;
                }
                case 257 -> { // Enter
                    if (selection.hasSelection()) {
                        editor.deleteSelection();
                    }
                    insertNewLine(cursor, lines);
                    undoManager.addUndoAction(new UndoAction(lines, cursor));
                    return true;
                }
                case 263 -> { // Left arrow
                    if (selection.hasSelection()) {
                        selection.clear();
                    }
                    moveCursorLeft(cursor, lines);
                    return true;
                }
                case 262 -> { // Right arrow
                    if (selection.hasSelection()) {
                        selection.clear();
                    }
                    moveCursorRight(cursor, lines);
                    return true;
                }
                case 265 -> { // Up arrow
                    if (selection.hasSelection()) {
                        selection.clear();
                    }
                    moveCursorUp(cursor, lines);
                    return true;
                }
                case 264 -> { // Down arrow
                    if (selection.hasSelection()) {
                        selection.clear();
                    }
                    moveCursorDown(cursor, lines);
                    return true;
                }
                case 258 -> { // Tab
                    if (selection.hasSelection()) {
                        selection.clear();
                    }
                    insertTab(cursor, lines);
                    undoManager.addUndoAction(new UndoAction(lines, cursor));
                    return true;
                }
            }
        } catch (Exception e) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Error in keyPressed: " + e.getMessage()), false);
            }
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private void moveCursorLeft(Cursor cursor, List<String> lines) {
        if (cursor.getColumn() > 0) {
            cursor.setColumn(cursor.getColumn() - 1);
        } else if (cursor.getLine() > 0) {
            cursor.setLine(cursor.getLine() - 1);
            cursor.setColumn(lines.get(cursor.getLine()).length());
        }
    }

    private void moveCursorRight(Cursor cursor, List<String> lines) {
        if (cursor.getColumn() < lines.get(cursor.getLine()).length()) {
            cursor.setColumn(cursor.getColumn() + 1);
        } else if (cursor.getLine() < lines.size() - 1) {
            cursor.setLine(cursor.getLine() + 1);
            cursor.setColumn(0);
        }
    }

    private void moveCursorUp(Cursor cursor, List<String> lines) {
        if (cursor.getLine() > 0) {
            cursor.setLine(cursor.getLine() - 1);
            cursor.setColumn(Math.min(cursor.getColumn(), lines.get(cursor.getLine()).length()));
        }
    }

    private void moveCursorDown(Cursor cursor, List<String> lines) {
        if (cursor.getLine() < lines.size() - 1) {
            cursor.setLine(cursor.getLine() + 1);
            cursor.setColumn(Math.min(cursor.getColumn(), lines.get(cursor.getLine()).length()));
        }
    }

    private void deleteCharacterBefore(Cursor cursor, List<String> lines) {
        String currentLine = lines.get(cursor.getLine());
        lines.set(cursor.getLine(), currentLine.substring(0, cursor.getColumn() - 1) + currentLine.substring(cursor.getColumn()));
        cursor.setColumn(cursor.getColumn() - 1);
    }

    private void deleteCharacterAfter(Cursor cursor, List<String> lines) {
        String currentLine = lines.get(cursor.getLine());
        lines.set(cursor.getLine(), currentLine.substring(0, cursor.getColumn()) + currentLine.substring(cursor.getColumn() + 1));
    }

    private void mergeLineWithPrevious(Cursor cursor, List<String> lines) {
        String currentLine = lines.remove(cursor.getLine());
        cursor.setLine(cursor.getLine() - 1);
        cursor.setColumn(lines.get(cursor.getLine()).length());
        lines.set(cursor.getLine(), lines.get(cursor.getLine()) + currentLine);
    }

    private void mergeLineWithNext(Cursor cursor, List<String> lines) {
        String nextLine = lines.remove(cursor.getLine() + 1);
        lines.set(cursor.getLine(), lines.get(cursor.getLine()) + nextLine);
    }

    private void insertNewLine(Cursor cursor, List<String> lines) {
        String currentLine = lines.get(cursor.getLine());
        String newLine = currentLine.substring(cursor.getColumn());
        lines.set(cursor.getLine(), currentLine.substring(0, cursor.getColumn()));
        lines.add(cursor.getLine() + 1, newLine);
        cursor.setLine(cursor.getLine() + 1);
        cursor.setColumn(0);
    }

    private void insertTab(Cursor cursor, List<String> lines) {
        String currentLine = lines.get(cursor.getLine());
        lines.set(cursor.getLine(), currentLine.substring(0, cursor.getColumn()) + "    " + currentLine.substring(cursor.getColumn()));
        cursor.setColumn(cursor.getColumn() + 4);
    }

    @Override
    public boolean handleCharTyped(char codePoint, int modifiers) {

        boolean hasSelection = editor.getSelection().hasSelection();
        Cursor cursor = editor.getCursor();
        int cursorLine = editor.getCursor().getLine();
        int cursorColumn = editor.getCursor().getColumn();
        List<String> lines = editor.getLines();



        try {
            if (hasSelection) {
                editor.deleteSelection();
            }
            String currentLine = lines.get(cursorLine);
            lines.set(cursorLine, currentLine.substring(0, cursorColumn) + codePoint + currentLine.substring(cursorColumn));
            cursor.setColumn(cursorColumn + 1 );
            editor.getSelection().clear();
            editor.getUndoManager().addUndoAction(new UndoAction(lines, cursor));
            return true;
        } catch (Exception e) {
            Minecraft.getInstance().player.displayClientMessage(Component.literal("Error in charTyped: " + e.getMessage()), false);
            e.printStackTrace();
            return false;
        }
    }

    private void selectAll() {
        Selection selection = editor.getSelection();
        Cursor cursor = editor.getCursor();
        List<String> lines = editor.getLines();

        selection.setStart(0, 0);
        selection.setEnd(lines.size() - 1, lines.get(lines.size() - 1).length());

        cursor.setLine(lines.size() - 1);
        cursor.setColumn(lines.get(lines.size() - 1).length());
    }

        private void copy() {
            Selection selection = editor.getSelection();
            if (selection.hasSelection()) {
                String selectedText = getSelectedText();
                Minecraft.getInstance().keyboardHandler.setClipboard(selectedText);
            }
        }

        private void cut() {
        Selection selection = editor.getSelection();
        UndoManager undoManager = editor.getUndoManager();

        if (selection.hasSelection()) {
            copy();
            editor.deleteSelection();
            undoManager.addUndoAction(new UndoAction(editor.getLines(), editor.getCursor()));
        }
    }

    private void paste() {
        String clipboardText = Minecraft.getInstance().keyboardHandler.getClipboard();
        Selection selection = editor.getSelection();
        UndoManager undoManager = editor.getUndoManager();

        if (!clipboardText.isEmpty()) {
            if (selection.hasSelection()) {
                editor.deleteSelection();
            }
            insertText(clipboardText);
            undoManager.addUndoAction(new UndoAction(editor.getLines(), editor.getCursor()));
        }
    }

    private void duplicateLine() {
        Cursor cursor = editor.getCursor();
        List<String> lines = editor.getLines();
        UndoManager undoManager = editor.getUndoManager();

        String currentLine = lines.get(cursor.getLine());
        lines.add(cursor.getLine() + 1, currentLine);
        cursor.setLine(cursor.getLine() + 1);
        undoManager.addUndoAction(new UndoAction(lines, cursor));
    }

    private String getSelectedText() {
        Selection selection = editor.getSelection();
        List<String> lines = editor.getLines();

        if (!selection.hasSelection()) return "";

        StringBuilder sb = new StringBuilder();
        int startLine = Math.min(selection.getStartLine(), selection.getEndLine());
        int endLine = Math.max(selection.getStartLine(), selection.getEndLine());
        int startCol = (startLine == selection.getStartLine()) ? selection.getStartColumn() : selection.getEndColumn();
        int endCol = (endLine == selection.getEndLine()) ? selection.getEndColumn() : selection.getStartColumn();

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
        Cursor cursor = editor.getCursor();
        List<String> lines = editor.getLines();

        String[] insertLines = text.split("\n", -1);
        String currentLine = lines.get(cursor.getLine());
        String beforeCursor = currentLine.substring(0, cursor.getColumn());
        String afterCursor = currentLine.substring(cursor.getColumn());

        lines.set(cursor.getLine(), beforeCursor + insertLines[0]);
        cursor.setColumn(cursor.getColumn() + insertLines[0].length());

        for (int i = 1; i < insertLines.length; i++) {
            cursor.setLine(cursor.getLine() + 1);
            lines.add(cursor.getLine(), insertLines[i]);
            cursor.setColumn(insertLines[i].length());
        }

        lines.set(cursor.getLine(), lines.get(cursor.getLine()) + afterCursor);
    }
    @Override
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (editor.isMouseOver(mouseX, mouseY)) {
            editor.setFocused(true);
            editor.updateCursorPosition(mouseX, mouseY);
            if (button == 0) { // Left click
                Cursor cursor = editor.getCursor();
                Selection selection = editor.getSelection();
                int cursorLine = cursor.getLine();
                int cursorColumn = cursor.getColumn();
                selection.setStartLine(cursorLine);
                selection.setStartColumn(cursorColumn);
                selection.setEndLine(cursorLine);
                selection.setEndColumn(cursorColumn);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && editor.isMouseOver(mouseX, mouseY)) {
            editor.updateCursorPosition(mouseX, mouseY);
            editor.getSelection().setEndLine(editor.getCursor().getLine());
            editor.getSelection().setEndColumn(editor.getCursor().getColumn());
            return true;
        }
        return false;
    }

    // Helper methods...
}