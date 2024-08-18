package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

public class Cursor {
    private int line;
    private int column;

    public Cursor(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public Cursor(Cursor cursor) {
        this.line = cursor.line;
        this.column = cursor.column;
    }

    public  Cursor()
    {
        this.line = 0;
        this.column = 0;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setColumn(int column) {
        this.column = column;
    }


}
