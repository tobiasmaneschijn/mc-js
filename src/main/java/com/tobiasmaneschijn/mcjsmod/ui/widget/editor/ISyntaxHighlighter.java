package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

import java.util.List;

interface ISyntaxHighlighter {
    List<TextSegment> highlightLine(String line);
}
