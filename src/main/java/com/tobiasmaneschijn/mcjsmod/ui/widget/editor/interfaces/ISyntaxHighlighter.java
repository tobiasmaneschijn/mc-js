package com.tobiasmaneschijn.mcjsmod.ui.widget.editor.interfaces;

import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.record.TextSegment;

import java.util.List;

public interface ISyntaxHighlighter {
    List<TextSegment> highlightLine(String line);
}
