package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

import java.util.ArrayList;
import java.util.List;

class UndoAction {
    private final List<String> lines;
    private final Cursor cursor;

    public UndoAction(List<String> lines, Cursor cursor) {
        this.lines = new ArrayList<>(lines);
        this.cursor = new Cursor(cursor);
    }

    // Getters...
}