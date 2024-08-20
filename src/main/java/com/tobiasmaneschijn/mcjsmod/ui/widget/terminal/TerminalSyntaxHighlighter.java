package com.tobiasmaneschijn.mcjsmod.ui.widget.terminal;


import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.interfaces.ISyntaxHighlighter;
import com.tobiasmaneschijn.mcjsmod.ui.widget.editor.record.TextSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalSyntaxHighlighter implements ISyntaxHighlighter {
    @Override
    public List<TextSegment> highlightLine(String line) {
        List<TextSegment> segments = new ArrayList<>();

        // Terminal prompt
        String prompt = "^>\\s?";

        // JavaScript keywords (for JS commands)
        String jsKeywords = "\\b(var|let|const|if|else|for|while|function|return|class|console)\\b";

        // Terminal commands
        String commands = "\\b(clear|help|exit|js)\\b";

        // Strings
        String strings = "\"(?:\\\\\"|[^\"])*\"|'(?:\\\\'|[^'])*'";

        // Numbers
        String numbers = "\\b\\d+(\\.\\d*)?([eE][+-]?\\d+)?\\b";

        // Error messages
        String errorMessages = "^ERROR:.*$";

        // Output indicators
        String outputIndicators = "^(Output:)";

        Pattern pattern = Pattern.compile(
                String.join("|", prompt, jsKeywords, commands, strings, numbers, errorMessages, outputIndicators),
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

            if (match.matches(prompt)) {
                color = 0x00FF00; // Green for prompt
            } else if (match.matches(jsKeywords)) {
                color = 0xFF7777; // Red for JS keywords
            } else if (match.matches(commands)) {
                color = 0x00FFFF; // Cyan for terminal commands
            } else if (match.matches(strings)) {
                color = 0x77FF77; // Light green for strings
            } else if (match.matches(numbers)) {
                color = 0xFFFF77; // Yellow for numbers
            } else if (match.matches(errorMessages)) {
                color = 0xFF0000; // Bright red for error messages
            } else if (match.matches(outputIndicators)) {
                color = 0x0000FF; // Blue for output indicators
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