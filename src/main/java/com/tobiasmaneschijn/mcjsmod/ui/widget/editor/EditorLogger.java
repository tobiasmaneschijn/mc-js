package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class EditorLogger {
    public static void logDebugInfo(String methodName, TextEditor editor) {
        if (Minecraft.getInstance().player != null) {

            int cursorLine = editor.getCursor().getLine();
            int cursorColumn = editor.getCursor().getColumn();
            int selectionStartLine = editor.getSelection().getStartLine();
            int selectionStartColumn = editor.getSelection().getStartColumn();
            int selectionEndLine = editor.getSelection().getEndLine();
            int selectionEndColumn = editor.getSelection().getEndColumn();

            Minecraft.getInstance().player.displayClientMessage(Component.literal(
                    String.format("%s - Cursor: (%d, %d), Selection: (%d, %d) to (%d, %d), HasSelection: %b",
                            methodName, cursorLine, cursorColumn,
                            selectionStartLine, selectionStartColumn,
                            selectionEndLine, selectionEndColumn,
                            editor.getSelection().hasSelection()
            )), false);
        }
    }
}
