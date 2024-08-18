package com.tobiasmaneschijn.mcjsmod.ui.widget.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// JavaScriptSyntaxHighlighter.java
class JavaScriptSyntaxHighlighter implements ISyntaxHighlighter {
    @Override
    public List<TextSegment> highlightLine(String line) {
        List<TextSegment> segments = new ArrayList<>();

        // Keywords
        String keywords = "\\b(var|let|const|if|else|for|while|do|switch|case|break|continue|" +
                "function|return|try|catch|finally|throw|class|extends|new|this|super|" +
                "import|export|async|await|yield)\\b";

        // Built-in objects and functions
        String builtIns = "\\b(Array|Object|String|Number|Boolean|Function|Symbol|RegExp|Date|" +
                "Math|JSON|console|window|document|null|undefined|NaN|Infinity)\\b";

        // Operators
        String operators = "[=+\\-*/%&|^~<>!?:]";

        // Numbers
        String numbers = "\\b\\d+(\\.\\d*)?([eE][+-]?\\d+)?\\b";

        // Strings
        String strings = "\"(?:\\\\\"|[^\"])*\"|'(?:\\\\'|[^'])*'|`(?:\\\\`|[^`])*`";

        // Comments (single-line only for simplicity)
        String comments = "//.*$";

        Pattern pattern = Pattern.compile(
                String.join("|", keywords, builtIns, operators, numbers, strings, comments),
                Pattern.MULTILINE
        );

        Matcher matcher = pattern.matcher(line);
        int lastEnd = 0;

        while (matcher.find()) {
            // Add any text before the match as plain text
            if (matcher.start() > lastEnd) {
                segments.add(new TextSegment(line.substring(lastEnd, matcher.start()), 0xFFFFFF));
            }

            String match = matcher.group();
            int color;

            if (match.matches(keywords)) {
                color = 0xFF7777; // Red for keywords
            } else if (match.matches(builtIns)) {
                color = 0x7777FF; // Blue for built-ins
            } else if (match.matches(operators)) {
                color = 0xFFFF77; // Yellow for operators
            } else if (match.matches(numbers)) {
                color = 0x77FFFF; // Cyan for numbers
            } else if (match.matches(strings)) {
                color = 0x77FF77; // Green for strings
            } else if (match.matches(comments)) {
                color = 0x777777; // Gray for comments
            } else {
                color = 0xFFFFFF; // White for unrecognized (shouldn't happen)
            }

            segments.add(new TextSegment(match, color));
            lastEnd = matcher.end();
        }

        // Add any remaining text as plain text
        if (lastEnd < line.length()) {
            segments.add(new TextSegment(line.substring(lastEnd), 0xFFFFFF));
        }

        return segments;
    }
}
