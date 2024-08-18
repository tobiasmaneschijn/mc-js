package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

public class Selection {
    private int startLine;
    private int startColumn;
    private int endLine;
    private int endColumn;

    public Selection() {
        clear();
    }

    public void setStart(int line, int column) {
        this.startLine = line;
        this.startColumn = column;
    }

    public void setEnd(int line, int column) {
        this.endLine = line;
        this.endColumn = column;
    }

    public boolean hasSelection() {
        return startLine != -1 && endLine != -1 &&
                (startLine != endLine || startColumn != endColumn);
    }

    public void clear() {
        startLine = endLine = -1;
        startColumn = endColumn = -1;
    }

    public void normalize() {
        if (startLine > endLine || (startLine == endLine && startColumn > endColumn)) {
            // Swap start and end if selection is right-to-left
            int tempLine = startLine;
            int tempColumn = startColumn;
            startLine = endLine;
            startColumn = endColumn;
            endLine = tempLine;
            endColumn = tempColumn;
        }
    }

    public SelectionInfo getSelectionInfo(int line, int maxColumn) {
        if (!hasSelection()) return null;

        int startLine = Math.min(this.startLine, this.endLine);
        int endLine = Math.max(this.startLine, this.endLine);

        if (line < startLine || line > endLine) return null;

        int startCol, endCol;
        if (this.startLine < this.endLine || (this.startLine == this.endLine && this.startColumn < this.endColumn)) {
            // Left-to-right selection
            startCol = (line == this.startLine) ? this.startColumn : 0;
            endCol = (line == this.endLine) ? this.endColumn : maxColumn;
        } else {
            // Right-to-left selection
            startCol = (line == this.endLine) ? this.endColumn : 0;
            endCol = (line == this.startLine) ? this.startColumn : maxColumn;
        }

        return new SelectionInfo(startCol, endCol);
    }



    // Getters and setters
    public int getStartLine() { return startLine; }
    public int getStartColumn() { return startColumn; }
    public int getEndLine() { return endLine; }
    public int getEndColumn() { return endColumn; }

    public void setStartLine(int startLine) { this.startLine = startLine; }
    public void setStartColumn(int startColumn) { this.startColumn = startColumn; }
    public void setEndLine(int endLine) { this.endLine = endLine; }
    public void setEndColumn(int endColumn) { this.endColumn = endColumn; }

    public record SelectionInfo(int startX, int endX) {}
}