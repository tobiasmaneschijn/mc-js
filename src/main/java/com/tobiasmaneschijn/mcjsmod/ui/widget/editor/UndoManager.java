package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

import java.util.ArrayList;
import java.util.List;

class UndoManager {
    private final List<UndoAction> undoStack = new ArrayList<>();
    private final List<UndoAction> redoStack = new ArrayList<>();

    public void addUndoAction(UndoAction action) {
        undoStack.add(action);
        redoStack.clear();
    }

    public void undo() {
    }

    public void redo() {
    }
}
