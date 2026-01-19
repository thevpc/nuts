package net.thevpc.nuts.runtime.standalone.format.tson.parser.custom;

import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;

public class TsonCommentsHelper {
    public static String normalizeBlockComment(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        // Remove /* and */
        String content = raw;
        if (content.startsWith("/*")) content = content.substring(2);
        if (content.endsWith("*/")) content = content.substring(0, content.length() - 2);

        String[] lines = content.split("\n", -1);

        // Trim leading/trailing blank lines
        int start = 0, end = lines.length;
        while (start < end && lines[start].trim().isEmpty()) start++;
        while (end > start && lines[end - 1].trim().isEmpty()) end--;
        if (start >= end) return "";

        // Detect if starred comment
        boolean isStarred = isLikelyStarredComment(lines, start, end);

        // Step 1: Preprocess lines (remove stars if starred)
        List<String> processedLines = new ArrayList<>();
        for (int i = start; i < end; i++) {
            String line = lines[i];
            if (isStarred) {
                line = removeLeadingStars(line);
            }
            processedLines.add(line);
        }

        // Step 2: Compute min indent (ignoring blank lines)
        int minIndent = Integer.MAX_VALUE;
        for (String line : processedLines) {
            if (line.trim().isEmpty()) continue;
            int indent = leadingWhitespaceLength(line);
            minIndent = Math.min(minIndent, indent);
        }
        if (minIndent == Integer.MAX_VALUE) minIndent = 0;

        // Step 3: Remove min indent and trim trailing whitespace
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < processedLines.size(); i++) {
            String line = processedLines.get(i);
            if (i > 0) result.append('\n');

            String normalizedLine;
            if (line.length() <= minIndent) {
                normalizedLine = ""; // line is all whitespace or empty
            } else {
                normalizedLine = line.substring(minIndent);
            }

            // ✅ TRIM TRAILING WHITESPACE
            normalizedLine = NStringUtils.trimRight(normalizedLine);

            result.append(normalizedLine);
        }

        return result.toString();
    }

    private static int leadingWhitespaceLength(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    private static String removeLeadingStars(String line) {
        int leadingWhitespace = 0;
        while (leadingWhitespace < line.length() &&
                Character.isWhitespace(line.charAt(leadingWhitespace))) {
            leadingWhitespace++;
        }

        if (leadingWhitespace == line.length()) {
            return line; // blank line
        }

        String afterWhitespace = line.substring(leadingWhitespace);

        // Remove exactly one '*' if present, plus optional space after
        if (afterWhitespace.startsWith("*")) {
            String afterStar = afterWhitespace.substring(1);
            // Remove one space if present after *
            if (afterStar.startsWith(" ")) {
                afterStar = afterStar.substring(1);
            }
            return line.substring(0, leadingWhitespace) + afterStar;
        }

        return line;
    }

    private static boolean isLikelyStarredComment(String[] lines, int start, int end) {
        int starredCount = 0;
        int totalNonEmpty = 0;
        for (int i = start; i < end; i++) {
            String trimmed = NStringUtils.trimLeft(lines[i]);
            if (!trimmed.isEmpty()) {
                totalNonEmpty++;
                if (trimmed.startsWith("*")) {
                    starredCount++;
                }
            }
        }
        // If majority of non-empty lines start with *, treat as starred
        return totalNonEmpty > 0 && (starredCount * 2 >= totalNonEmpty);
    }

}
