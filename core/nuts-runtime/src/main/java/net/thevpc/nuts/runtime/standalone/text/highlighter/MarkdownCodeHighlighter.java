package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NStringUtils;

public class MarkdownCodeHighlighter implements NCodeHighlighter {

    @Override
    public String id() {
        return "markdown";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) return NScorable.DEFAULT_SCORE;
        switch (s) {
            case "markdown":
            case "md":
            case "mkd":
            case "mkdn":
            case "mdx":
            case "text/markdown":
            case "text/x-markdown":
                return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public NText tokenToText(String text, String nodeType, NTexts txt) {
        return txt.ofPlain(text);
    }

    @Override
    public NText stringToText(String text, NTexts txt) {
        List<NText> all = new ArrayList<>();
        String[] lines = text.split("\n", -1);

        boolean inFencedCode = false;
        String fenceMarker = null; // "```" or "~~~"

        for (int i = 0; i < lines.length; i++) {
            if (i > 0) all.add(txt.ofPlain("\n"));

            String line = lines[i];

            // ---- Fenced code block ----
            if (!inFencedCode) {
                String fence = detectFenceOpen(line);
                if (fence != null) {
                    inFencedCode = true;
                    fenceMarker = fence.substring(0, 3); // ``` or ~~~
                    highlightFenceLine(line, txt, all);
                    continue;
                }
            } else {
                // inside fenced code: look for closing fence
                String trimmed = NStringUtils.trimLeft(line);
                String finalFenceMarker = fenceMarker;
                if (trimmed.startsWith(fenceMarker) && NStringUtils.trimRight(trimmed).chars().allMatch(c -> c == finalFenceMarker.charAt(0))) {
                    inFencedCode = false;
                    fenceMarker = null;
                    highlightFenceLine(line, txt, all);
                } else {
                    // code content — plain, no inline parsing
                    all.add(txt.ofPlain(line));
                }
                continue;
            }

            // ---- Setext heading: check if NEXT line is === or ---
            if (i + 1 < lines.length) {
                String next = lines[i + 1].trim();
                if (!line.trim().isEmpty() && (isSetextH1(next) || isSetextH2(next))) {
                    NTextStyle headingStyle = isSetextH1(next) ? NTextStyle.keyword() : NTextStyle.separator();
                    all.add(txt.ofStyled(line, headingStyle));
                    continue;
                }
            }

            // ---- Dispatch by first non-space character ----
            highlightLine(line, txt, all);
        }

        return txt.ofList(all.toArray(new NText[0]));
    }

    // -------------------------------------------------------------------------
    // Line-level dispatch
    // -------------------------------------------------------------------------

    private void highlightLine(String line, NTexts txt, List<NText> all) {
        if (line.isEmpty()) return;

        String trimmed = NStringUtils.trimLeft(line);
        int indent = line.length() - trimmed.length();

        // indented code block (4 spaces or 1 tab)
        if (indent >= 4 || (line.length() > 0 && line.charAt(0) == '\t')) {
            all.add(txt.ofPlain(line));
            return;
        }

        // emit leading indent
        if (indent > 0) all.add(txt.ofPlain(line.substring(0, indent)));

        if (trimmed.isEmpty()) return;

        // ATX heading: # ## ### etc.
        if (trimmed.charAt(0) == '#') {
            highlightHeading(trimmed, txt, all);
            return;
        }

        // blockquote: > text
        if (trimmed.charAt(0) == '>') {
            all.add(txt.ofStyled(">", NTextStyle.separator()));
            String rest = trimmed.substring(1);
            if (!rest.isEmpty() && rest.charAt(0) == ' ') {
                all.add(txt.ofPlain(" "));
                rest = rest.substring(1);
            }
            highlightInline(rest, txt, all);
            return;
        }

        // horizontal rule: ---, ***, ___  (3+ of same char, optional spaces)
        if (isHorizontalRule(trimmed)) {
            all.add(txt.ofStyled(trimmed, NTextStyle.separator()));
            return;
        }

        // setext underline lines (=== / ---) that weren't caught by lookahead
        // emit as separator
        if (isSetextH1(trimmed) || isSetextH2(trimmed)) {
            all.add(txt.ofStyled(trimmed, NTextStyle.separator()));
            return;
        }

        // table row: | cell | cell |
        if (trimmed.charAt(0) == '|' || (trimmed.contains("|") && looksLikeTableRow(trimmed))) {
            highlightTableRow(trimmed, txt, all);
            return;
        }

        // unordered list item: - / * / + followed by space
        if ((trimmed.charAt(0) == '-' || trimmed.charAt(0) == '*' || trimmed.charAt(0) == '+')
                && trimmed.length() > 1 && trimmed.charAt(1) == ' ') {
            all.add(txt.ofStyled(String.valueOf(trimmed.charAt(0)), NTextStyle.separator()));
            all.add(txt.ofPlain(" "));
            highlightInline(trimmed.substring(2), txt, all);
            return;
        }

        // ordered list item: 1. / 2) etc.
        int orderedEnd = detectOrderedListMarker(trimmed);
        if (orderedEnd > 0) {
            all.add(txt.ofStyled(trimmed.substring(0, orderedEnd), NTextStyle.separator()));
            all.add(txt.ofPlain(" "));
            highlightInline(trimmed.substring(orderedEnd + 1), txt, all);
            return;
        }

        // HTML block: line starts with <tag
        if (trimmed.charAt(0) == '<') {
            highlightInline(trimmed, txt, all); // inline handles <autolink> and HTML tags
            return;
        }

        // paragraph / plain text
        highlightInline(trimmed, txt, all);
    }

    // -------------------------------------------------------------------------
    // ATX Heading:  ### Title text  {optional closing ###}
    // -------------------------------------------------------------------------

    private void highlightHeading(String line, NTexts txt, List<NText> all) {
        int level = 0;
        while (level < line.length() && line.charAt(level) == '#') level++;
        // hashes
        all.add(txt.ofStyled(line.substring(0, level), NTextStyle.keyword()));
        if (level >= line.length()) return;
        // space after hashes
        if (line.charAt(level) == ' ') {
            all.add(txt.ofPlain(" "));
            level++;
        }
        // optional trailing ### — strip for inline parsing
        String content = line.substring(level);
        String stripped = NStringUtils.trimRight(content);
        int trailStart = stripped.length();
        while (trailStart > 0 && stripped.charAt(trailStart - 1) == '#') trailStart--;
        String trail = NStringUtils.trimLeft(stripped.substring(trailStart));
        String body = trailStart > 0 ? NStringUtils.trimRight(stripped.substring(0, trailStart)) : "";

        if (!body.isEmpty()) highlightInline(body, txt, all);
        else if (!trail.isEmpty()) {
            // the entire thing after space was hashes — treat as trail
            all.add(txt.ofStyled(content, NTextStyle.keyword()));
            return;
        }
        if (!trail.isEmpty()) {
            all.add(txt.ofPlain(" "));
            all.add(txt.ofStyled(trail, NTextStyle.keyword()));
        }
        // trailing spaces after optional closing hashes
        String after = content.substring(stripped.length());
        if (!after.isEmpty()) all.add(txt.ofPlain(after));
    }

    // -------------------------------------------------------------------------
    // Fenced code block opening/closing line:  ```lang  or  ~~~
    // -------------------------------------------------------------------------

    private void highlightFenceLine(String line, NTexts txt, List<NText> all) {
        StringReaderExt ar = new StringReaderExt(line);
        StringBuilder fence = new StringBuilder();
        // leading spaces
        while (ar.hasNext() && ar.peekChar() == ' ') fence.append(ar.readChar());
        // fence chars
        char fc = ar.hasNext() ? ar.peekChar() : 0;
        while (ar.hasNext() && ar.peekChar() == fc) fence.append(ar.readChar());
        all.add(txt.ofStyled(fence.toString(), NTextStyle.separator()));
        // language tag
        if (ar.hasNext()) {
            StringBuilder lang = new StringBuilder();
            while (ar.hasNext()) lang.append(ar.readChar());
            all.add(txt.ofStyled(lang.toString(), NTextStyle.keyword()));
        }
    }

    // -------------------------------------------------------------------------
    // Table row:  | cell | **bold** | `code` |
    // -------------------------------------------------------------------------

    private void highlightTableRow(String line, NTexts txt, List<NText> all) {
        StringReaderExt ar = new StringReaderExt(line);
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '|') {
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                continue;
            }
            if (c == '-' || c == ':') {
                // separator row: |---|:---:|---| — style as separator
                StringBuilder sb = new StringBuilder();
                while (ar.hasNext() && ar.peekChar() != '|') sb.append(ar.readChar());
                all.add(txt.ofStyled(sb.toString(), NTextStyle.separator()));
                continue;
            }
            // cell content — collect to next | then inline-parse
            StringBuilder cell = new StringBuilder();
            while (ar.hasNext() && ar.peekChar() != '|') cell.append(ar.readChar());
            if (cell.length() > 0) highlightInline(cell.toString(), txt, all);
        }
    }

    // -------------------------------------------------------------------------
    // Inline span parsing
    // Handles: **bold** *italic* ~~strike~~ `code` [link](url) ![img](url)
    //          <autolink> &entity; and plain text
    // -------------------------------------------------------------------------

    private void highlightInline(String text, NTexts txt, List<NText> all) {
        StringReaderExt ar = new StringReaderExt(text);

        while (ar.hasNext()) {
            char c = ar.peekChar();

            // ---- Escaped character ----
            if (c == '\\' && ar.hasNext(1)) {
                ar.readChar(); // backslash
                all.add(txt.ofStyled("\\" + ar.readChar(), NTextStyle.separator()));
                continue;
            }

            // ---- Inline code: `code` or ``code`` ----
            if (c == '`') {
                all.addAll(readInlineCode(ar, txt));
                continue;
            }

            // ---- Bold+italic: *** or ___ ----
            if ((c == '*' || c == '_') && ar.hasNext(2)
                    && ar.peekChar(1) == c && ar.peekChar(2) == c) {
                List<NText> inner = tryReadSpan(ar, txt, String.valueOf(new char[]{c, c, c}), NTextStyle.bold());
                if (inner != null) { all.addAll(inner); continue; }
            }

            // ---- Bold: ** or __ ----
            if ((c == '*' || c == '_') && ar.hasNext(1) && ar.peekChar(1) == c) {
                List<NText> inner = tryReadSpan(ar, txt, String.valueOf(new char[]{c, c}), NTextStyle.bold());
                if (inner != null) { all.addAll(inner); continue; }
            }

            // ---- Italic: * or _ ----
            if (c == '*' || c == '_') {
                List<NText> inner = tryReadSpan(ar, txt, String.valueOf(c), NTextStyle.italic());
                if (inner != null) { all.addAll(inner); continue; }
            }

            // ---- Strikethrough: ~~ ----
            if (c == '~' && ar.hasNext(1) && ar.peekChar(1) == '~') {
                List<NText> inner = tryReadSpan(ar, txt, "~~", NTextStyle.separator());
                if (inner != null) { all.addAll(inner); continue; }
            }

            // ---- Image: ![alt](url) ----
            if (c == '!' && ar.hasNext(1) && ar.peekChar(1) == '[') {
                List<NText> link = tryReadLink(ar, txt, true);
                if (link != null) { all.addAll(link); continue; }
            }

            // ---- Link: [text](url) or [text][ref] ----
            if (c == '[') {
                List<NText> link = tryReadLink(ar, txt, false);
                if (link != null) { all.addAll(link); continue; }
            }

            // ---- Autolink / HTML tag: <...> ----
            if (c == '<') {
                List<NText> tag = tryReadAutolink(ar, txt);
                if (tag != null) { all.addAll(tag); continue; }
            }

            // ---- HTML entity: &name; or &#123; ----
            if (c == '&') {
                List<NText> entity = tryReadEntity(ar, txt);
                if (entity != null) { all.addAll(entity); continue; }
            }

            // ---- Plain character ----
            all.add(txt.ofPlain(String.valueOf(ar.readChar())));
        }
    }

    // -------------------------------------------------------------------------
    // Inline code  `code`  ``code with ` inside``
    // -------------------------------------------------------------------------

    private List<NText> readInlineCode(StringReaderExt ar, NTexts txt) {
        // count opening backticks
        StringBuilder open = new StringBuilder();
        while (ar.hasNext() && ar.peekChar() == '`') open.append(ar.readChar());
        String fence = open.toString();

        StringBuilder content = new StringBuilder(fence);
        while (ar.hasNext()) {
            if (ar.peekChar() == '`') {
                StringBuilder close = new StringBuilder();
                while (ar.hasNext() && ar.peekChar() == '`') close.append(ar.readChar());
                content.append(close);
                if (close.toString().equals(fence)) break; // matched
            } else {
                content.append(ar.readChar());
            }
        }
        return Collections.singletonList(txt.ofStyled(content.toString(), NTextStyle.string()));
    }

    // -------------------------------------------------------------------------
    // Emphasis span: delimiter + content + delimiter
    // Returns null if no matching closing delimiter found (caller emits plain)
    // -------------------------------------------------------------------------

    private List<NText> tryReadSpan(StringReaderExt ar, NTexts txt, String delim, NTextStyle style) {
        // peek ahead for closing delimiter
        int delimLen = delim.length();
        int i = delimLen; // skip opening delim
        while (ar.hasNext(i)) {
            char pc = ar.peekChar(i);
            if (pc == '\n') break; // no cross-line spans
            // check for closing delim
            if (pc == delim.charAt(0)) {
                boolean match = true;
                for (int d = 0; d < delimLen; d++) {
                    if (!ar.hasNext(i + d) || ar.peekChar(i + d) != delim.charAt(d)) { match = false; break; }
                }
                if (match && i > delimLen) { // non-empty content
                    StringBuilder sb = new StringBuilder();
                    for (int j = 0; j < i + delimLen; j++) sb.append(ar.readChar());
                    return Collections.singletonList(txt.ofStyled(sb.toString(), style));
                }
            }
            i++;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Link: [text](url "title")  [text][ref]  [text][]
    // Image: ![alt](url)
    // -------------------------------------------------------------------------

    private List<NText> tryReadLink(StringReaderExt ar, NTexts txt, boolean isImage) {
        List<NText> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        if (isImage) sb.append(ar.readChar()); // '!'

        // opening [
        sb.append(ar.readChar());
        // read until closing ] (no nesting for simplicity)
        while (ar.hasNext() && ar.peekChar() != ']' && ar.peekChar() != '\n') {
            sb.append(ar.readChar());
        }
        if (!ar.hasNext() || ar.peekChar() != ']') {
            // no closing bracket — emit as plain
            result.add(txt.ofPlain(sb.toString()));
            return result;
        }
        sb.append(ar.readChar()); // ']'

        if (!ar.hasNext()) {
            result.add(txt.ofStyled(sb.toString(), NTextStyle.keyword()));
            return result;
        }

        if (ar.peekChar() == '(') {
            // inline link: (url "title")
            result.add(txt.ofStyled(sb.toString(), NTextStyle.keyword())); // [text]
            StringBuilder url = new StringBuilder();
            url.append(ar.readChar()); // '('
            int depth = 1;
            while (ar.hasNext() && depth > 0) {
                char c = ar.readChar();
                url.append(c);
                if (c == '(') depth++;
                else if (c == ')') depth--;
            }
            result.add(txt.ofStyled(url.toString(), NTextStyle.string())); // (url)
        } else if (ar.peekChar() == '[') {
            // reference link: [text][ref]
            result.add(txt.ofStyled(sb.toString(), NTextStyle.keyword()));
            StringBuilder ref = new StringBuilder();
            ref.append(ar.readChar()); // '['
            while (ar.hasNext() && ar.peekChar() != ']' && ar.peekChar() != '\n') ref.append(ar.readChar());
            if (ar.hasNext()) ref.append(ar.readChar()); // ']'
            result.add(txt.ofStyled(ref.toString(), NTextStyle.string()));
        } else {
            // collapsed ref [text]
            result.add(txt.ofStyled(sb.toString(), NTextStyle.keyword()));
        }

        return result;
    }

    // -------------------------------------------------------------------------
    // Autolink <http://...> or <email@example.com>, and bare HTML tags
    // -------------------------------------------------------------------------

    private List<NText> tryReadAutolink(StringReaderExt ar, NTexts txt) {
        // peek to find closing >
        int i = 1;
        while (ar.hasNext(i) && ar.peekChar(i) != '>' && ar.peekChar(i) != '\n') i++;
        if (!ar.hasNext(i) || ar.peekChar(i) != '>') return null;

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j <= i; j++) sb.append(ar.readChar());
        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.string()));
    }

    // -------------------------------------------------------------------------
    // HTML entity  &amp;  &#123;  &#x1F4A9;
    // -------------------------------------------------------------------------

    private List<NText> tryReadEntity(StringReaderExt ar, NTexts txt) {
        int i = 1;
        while (ar.hasNext(i) && ar.peekChar(i) != ';' && ar.peekChar(i) != ' ' && ar.peekChar(i) != '\n') i++;
        if (!ar.hasNext(i) || ar.peekChar(i) != ';' || i < 2) return null;

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j <= i; j++) sb.append(ar.readChar());
        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.separator()));
    }

    // -------------------------------------------------------------------------
    // Detection helpers
    // -------------------------------------------------------------------------

    private String detectFenceOpen(String line) {
        String t = NStringUtils.trimLeft(line);
        if (t.startsWith("```") || t.startsWith("~~~")) {
            return t.substring(0, 3);
        }
        return null;
    }

    private boolean isSetextH1(String trimmed) {
        return trimmed.length() >= 1 && trimmed.chars().allMatch(c -> c == '=');
    }

    private boolean isSetextH2(String trimmed) {
        return trimmed.length() >= 1 && trimmed.chars().allMatch(c -> c == '-');
    }

    private boolean isHorizontalRule(String trimmed) {
        // 3+ of the same char (* - _), optional spaces between
        char[] allowed = { '-', '*', '_' };
        for (char ch : allowed) {
            String stripped = trimmed.replace(" ", "");
            if (stripped.length() >= 3 && stripped.chars().allMatch(c -> c == ch)) return true;
        }
        return false;
    }

    private boolean looksLikeTableRow(String line) {
        long pipes = line.chars().filter(c -> c == '|').count();
        return pipes >= 2;
    }

    private int detectOrderedListMarker(String trimmed) {
        int i = 0;
        while (i < trimmed.length() && Character.isDigit(trimmed.charAt(i))) i++;
        if (i == 0 || i >= trimmed.length()) return -1;
        char marker = trimmed.charAt(i);
        if ((marker == '.' || marker == ')') && i + 1 < trimmed.length() && trimmed.charAt(i + 1) == ' ') {
            return i + 1; // position of the space
        }
        return -1;
    }
}