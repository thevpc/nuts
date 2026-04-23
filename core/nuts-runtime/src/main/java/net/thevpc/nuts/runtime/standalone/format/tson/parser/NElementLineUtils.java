package net.thevpc.nuts.runtime.standalone.format.tson.parser;

import net.thevpc.nuts.elem.NElementLine;
import net.thevpc.nuts.util.NBlankable;

import java.util.ArrayList;
import java.util.List;

public class NElementLineUtils {
    public static String extractPureContent(List<NElementLine> lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            NElementLine line = lines.get(i);
            sb.append(line.content());
            if(i<lines.size()-1) {
                if (line.newline() != null) {
                    sb.append(line.newline().value());
                }
            }
        }
        return sb.toString();
    }

    public static List<NElementLine> normalizeElementLines(List<NElementLine> lines) {
        if (lines == null || lines.isEmpty()) {
            return lines;
        }

        // Compute common whitespace prefix from non-blank content lines only
        String commonPrefix = null;
        for (NElementLine line : lines) {
            String content = line.content();
            if (NBlankable.isBlank(content)) {
                continue;
            }
            if (commonPrefix == null) {
                commonPrefix = leadingWhitespace(content);
            } else {
                commonPrefix = commonPrefix(commonPrefix, leadingWhitespace(content));
            }
            if (commonPrefix.isEmpty()) {
                break;
            }
        }

        if (commonPrefix == null || commonPrefix.isEmpty()) {
            return lines;
        }

        final String prefix = commonPrefix;
        List<NElementLine> result = new ArrayList<>(lines.size());
        for (NElementLine line : lines) {
            String content = line.content();
            String existingPadding = line.startPadding() == null ? "" : line.startPadding();
            if (NBlankable.isBlank(content)) {
                // Blank line: move content entirely into startPadding, content becomes ""
                String newPadding = existingPadding + (content == null ? "" : content);
                line = NElementLine.ofElementLine(
                        line.prefix(),
                        line.startMarker(),
                        newPadding,
                        "",
                        line.endPadding(),
                        line.endMarker(),
                        line.newline()
                );
            } else {
                // Non-blank line: strip common prefix from content, append it to startPadding
                String newPadding = existingPadding + prefix;
                String newContent = content.substring(prefix.length());
                line = NElementLine.ofElementLine(
                        line.prefix(),
                        line.startMarker(),
                        newPadding,
                        newContent,
                        line.endPadding(),
                        line.endMarker(),
                        line.newline()
                );
            }
            result.add(line);
        }
        return result;
    }

    private static String leadingWhitespace(String s) {
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        return s.substring(0, i);
    }

    private static String commonPrefix(String a, String b) {
        int len = Math.min(a.length(), b.length());
        int i = 0;
        while (i < len && a.charAt(i) == b.charAt(i)) {
            i++;
        }
        return a.substring(0, i);
    }
}
