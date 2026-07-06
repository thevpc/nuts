package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.*;

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

public class YamlCodeHighlighter implements NCodeHighlighter {

    private static final Set<String> SPECIAL_VALUES = new HashSet<>(Arrays.asList(
            "true", "false", "null", "~",
            "True", "False", "Null",
            "TRUE", "FALSE", "NULL",
            ".inf", ".Inf", ".INF",
            "-.inf", "-.Inf", "-.INF",
            ".nan", ".NaN", ".NAN"
    ));

    @Override
    public String id() {
        return "yaml";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) return NScorable.DEFAULT_SCORE;
        switch (s) {
            case "yaml":
            case "yml":
            case "text/yaml":
            case "application/yaml":
            case "application/x-yaml":
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

        // block scalar state: when >0 we are inside a literal/folded block
        // and every indented line is a string continuation
        int blockScalarIndent = -1;

        for (int i = 0; i < lines.length; i++) {
            if (i > 0) all.add(txt.ofPlain("\n"));

            String line = lines[i];
            if (line.isEmpty()) continue;

            // inside a block scalar: consume as string until de-dented
            if (blockScalarIndent >= 0) {
                int lineIndent = leadingSpaces(line);
                if (lineIndent > blockScalarIndent || line.trim().isEmpty()) {
                    all.add(txt.ofStyled(line, NTextStyle.string()));
                    continue;
                }
                // de-indented — exit block scalar mode
                blockScalarIndent = -1;
            }

            // detect block scalar indicator at end of a value position (| or >)
            int detectedBlockIndent = detectBlockScalar(line);

            highlightLine(line, txt, all);

            if (detectedBlockIndent >= 0) {
                blockScalarIndent = detectedBlockIndent;
            }
        }

        return txt.ofList(all.toArray(new NText[0]));
    }

    // -------------------------------------------------------------------------
    // Block scalar detection
    // Returns the indent level of the block content (indent of the key line),
    // or -1 if this line doesn't end with a block scalar indicator.
    // -------------------------------------------------------------------------

    private int detectBlockScalar(String line) {
        String trimmed = NStringUtils.stripRight(line);
        if (trimmed.isEmpty()) return -1;
        char last = trimmed.charAt(trimmed.length() - 1);
        // allow chomping indicators: |-, |+, >-, >+, |2-, etc.
        if (last == '-' || last == '+') {
            if (trimmed.length() < 2) return -1;
            last = trimmed.charAt(trimmed.length() - 2);
        }
        // allow explicit indent indicator digit between | /> and chomping
        if (Character.isDigit(last)) {
            if (trimmed.length() < 2) return -1;
            last = trimmed.charAt(trimmed.length() - 2);
        }
        if (last == '|' || last == '>') {
            return leadingSpaces(line);
        }
        return -1;
    }

    // -------------------------------------------------------------------------
    // Single line highlighting
    // -------------------------------------------------------------------------

    private void highlightLine(String line, NTexts txt, List<NText> all) {
        StringReaderExt ar = new StringReaderExt(line);

        // leading indent — emit as plain
        while (ar.hasNext() && ar.peekChar() == ' ') {
            all.add(txt.ofPlain(String.valueOf(ar.readChar())));
        }
        if (!ar.hasNext()) return;

        char first = ar.peekChar();

        // --- Document markers: --- and ...
        if (ar.peekChars("---") || ar.peekChars("...")) {
            all.add(txt.ofStyled(readToEnd(ar), NTextStyle.keyword()));
            return;
        }

        // --- Comment line
        if (first == '#') {
            all.add(txt.ofStyled(readToEnd(ar), NTextStyle.comments()));
            return;
        }

        // --- List item: starts with "- "
        if (first == '-' && ar.hasNext(1) && (ar.peekChar(1) == ' ' || ar.peekChar(1) == '\n' || !ar.hasNext(1))) {
            all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator())); // '-'
            if (ar.hasNext() && ar.peekChar() == ' ') {
                all.add(txt.ofPlain(String.valueOf(ar.readChar())));
            }
            // rest of the line is a value (might be a key: value inline)
            highlightValue(ar, txt, all);
            return;
        }

        // --- Try to read a key: value pair
        // Key can be: bare word, quoted string, or complex key with '?'
        if (first == '?') {
            // explicit key marker
            all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
            if (ar.hasNext() && ar.peekChar() == ' ') all.add(txt.ofPlain(String.valueOf(ar.readChar())));
            highlightKey(ar, txt, all);
            return;
        }

        // attempt key: value  (key ends at ':' followed by space or EOL)
        if (tryHighlightKeyValue(ar, txt, all)) return;

        // fallback: treat whole line as a value
        highlightValue(ar, txt, all);
    }

    // -------------------------------------------------------------------------
    // Key: value  detection and highlighting
    // -------------------------------------------------------------------------

    /**
     * Tries to identify a "key: value" pattern.
     * Returns true if it consumed the line as a key-value pair.
     */
    private boolean tryHighlightKeyValue(StringReaderExt ar, NTexts txt, List<NText> all) {
        // peek ahead to find ': ' or ':' at EOL
        int colonPos = findKeyColon(ar);
        if (colonPos < 0) return false;

        List<NText> keyTokens = new ArrayList<>();
        highlightKey(ar, txt, keyTokens);

        // verify we're now at the colon we found
        if (!ar.hasNext() || ar.peekChar() != ':') {
            // key reader consumed differently — emit what we have and bail
            all.addAll(keyTokens);
            return true;
        }

        all.addAll(keyTokens);
        all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator())); // ':'

        if (!ar.hasNext()) return true;

        // optional space after colon
        if (ar.peekChar() == ' ') {
            all.add(txt.ofPlain(String.valueOf(ar.readChar())));
        }

        if (ar.hasNext()) {
            highlightValue(ar, txt, all);
        }
        return true;
    }

    /**
     * Find position of ':' that acts as key-value separator.
     * Must be followed by ' ', '\t', or end of string.
     * Handles quoted keys by skipping over them.
     */
    private int findKeyColon(StringReaderExt ar) {
        int i = 0;
        boolean inSingle = false, inDouble = false;
        while (ar.hasNext(i)) {
            char c = ar.peekChar(i);
            if (inDouble) {
                if (c == '\\') { i += 2; continue; }
                if (c == '"') inDouble = false;
                i++; continue;
            }
            if (inSingle) {
                if (c == '\'') inSingle = false;
                i++; continue;
            }
            if (c == '"') { inDouble = true; i++; continue; }
            if (c == '\'') { inSingle = true; i++; continue; }
            if (c == ':') {
                // valid separator: followed by space/tab/EOL or end of input
                if (!ar.hasNext(i + 1) || ar.peekChar(i + 1) == ' ' || ar.peekChar(i + 1) == '\t') {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    // -------------------------------------------------------------------------
    // Key highlighting
    // -------------------------------------------------------------------------

    private void highlightKey(StringReaderExt ar, NTexts txt, List<NText> all) {
        if (!ar.hasNext()) return;
        char c = ar.peekChar();

        if (c == '"' || c == '\'') {
            // quoted key
            List<NText> quoted = readQuotedString(ar, txt);
            // re-style as keyword (key style) instead of string
            StringBuilder raw = new StringBuilder();
            for (NText t : quoted) {
                if (t instanceof NTextPlain) raw.append(((NTextPlain) t).value());
                else if (t instanceof NTextStyled) raw.append(((NTextStyled) t).child()); // approximate
            }
            all.add(txt.ofStyled(raw.toString(), NTextStyle.keyword()));
            return;
        }

        // bare key: read until ':' or end of line
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext() && ar.peekChar() != ':' && ar.peekChar() != '\n') {
            sb.append(ar.readChar());
        }
        all.add(txt.ofStyled(NStringUtils.stripRight(sb.toString()), NTextStyle.keyword()));
    }

    // -------------------------------------------------------------------------
    // Value highlighting
    // -------------------------------------------------------------------------

    private void highlightValue(StringReaderExt ar, NTexts txt, List<NText> all) {
        if (!ar.hasNext()) return;

        skipSpaces(ar, txt, all);
        if (!ar.hasNext()) return;

        char c = ar.peekChar();

        // inline comment — nothing else on this line
        if (c == '#') {
            all.add(txt.ofStyled(readToEnd(ar), NTextStyle.comments()));
            return;
        }

        // anchor definition &name
        if (c == '&') {
            StringBuilder sb = new StringBuilder();
            while (ar.hasNext() && !Character.isWhitespace(ar.peekChar())) sb.append(ar.readChar());
            all.add(txt.ofStyled(sb.toString(), NTextStyle.annotation()));
            skipSpaces(ar, txt, all);
            if (!ar.hasNext()) return;
            c = ar.peekChar();
        }

        // alias *name
        if (c == '*') {
            StringBuilder sb = new StringBuilder();
            while (ar.hasNext() && !Character.isWhitespace(ar.peekChar())) sb.append(ar.readChar());
            all.add(txt.ofStyled(sb.toString(), NTextStyle.annotation()));
            return;
        }

        // tag !!type or !tag
        if (c == '!') {
            StringBuilder sb = new StringBuilder();
            while (ar.hasNext() && !Character.isWhitespace(ar.peekChar())) sb.append(ar.readChar());
            all.add(txt.ofStyled(sb.toString(), NTextStyle.annotation()));
            skipSpaces(ar, txt, all);
            if (!ar.hasNext()) return;
            c = ar.peekChar();
        }

        // block scalar indicators | and >
        if (c == '|' || c == '>') {
            all.add(txt.ofStyled(readToEnd(ar), NTextStyle.separator()));
            return;
        }

        // flow sequence [ ... ] or flow mapping { ... }
        if (c == '[' || c == '{') {
            highlightFlowCollection(ar, txt, all);
            return;
        }

        // quoted string value
        if (c == '"' || c == '\'') {
            all.addAll(readQuotedString(ar, txt));
            // trailing comment after quoted value
            skipSpaces(ar, txt, all);
            if (ar.hasNext() && ar.peekChar() == '#') {
                all.add(txt.ofStyled(readToEnd(ar), NTextStyle.comments()));
            }
            return;
        }

        // bare value — read to end or inline comment
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext()) {
            // inline comment: '#' preceded by a space
            if (ar.peekChar() == '#' && sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
                // trim trailing space from value, emit comment
                String val = NStringUtils.stripRight(sb.toString());
                all.add(styleScalarValue(val, txt));
                all.add(txt.ofPlain(" "));
                all.add(txt.ofStyled(readToEnd(ar), NTextStyle.comments()));
                return;
            }
            sb.append(ar.readChar());
        }

        String val = NStringUtils.stripRight(sb.toString());
        if (!val.isEmpty()) all.add(styleScalarValue(val, txt));
    }

    /**
     * Style a bare scalar: special values (true/false/null/~) as keyword,
     * numbers as number, everything else as plain.
     */
    private NText styleScalarValue(String val, NTexts txt) {
        if (SPECIAL_VALUES.contains(val)) {
            return txt.ofStyled(val, NTextStyle.keyword());
        }
        if (isNumber(val)) {
            return txt.ofStyled(val, NTextStyle.number());
        }
        return txt.ofPlain(val);
    }

    // -------------------------------------------------------------------------
    // Flow collections  [a, b, c]  {k: v, k2: v2}
    // -------------------------------------------------------------------------

    private void highlightFlowCollection(StringReaderExt ar, NTexts txt, List<NText> all) {
        // Simple approach: tokenize as separators, strings, values
        // without full recursive parse (flow collections are rarely deeply nested in practice)
        char open = ar.peekChar();
        char close = (open == '[') ? ']' : '}';
        int depth = 0;

        while (ar.hasNext()) {
            char c = ar.peekChar();

            if (c == open) {
                depth++;
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                continue;
            }
            if (c == close) {
                depth--;
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                if (depth == 0) break;
                continue;
            }
            if (c == ',') {
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                continue;
            }
            if (c == ':') {
                all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                continue;
            }
            if (c == '#') {
                all.add(txt.ofStyled(readToEnd(ar), NTextStyle.comments()));
                return;
            }
            if (c == '&' || c == '*' || c == '!') {
                StringBuilder sb = new StringBuilder();
                while (ar.hasNext() && !Character.isWhitespace(ar.peekChar())
                        && ar.peekChar() != ',' && ar.peekChar() != ']' && ar.peekChar() != '}') {
                    sb.append(ar.readChar());
                }
                all.add(txt.ofStyled(sb.toString(), NTextStyle.annotation()));
                continue;
            }
            if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
                continue;
            }
            if (c == '"' || c == '\'') {
                all.addAll(readQuotedString(ar, txt));
                continue;
            }

            // bare scalar token inside flow
            StringBuilder sb = new StringBuilder();
            while (ar.hasNext()) {
                char p = ar.peekChar();
                if (p == ',' || p == ']' || p == '}' || p == ':' || p == '#' || Character.isWhitespace(p)) break;
                sb.append(ar.readChar());
            }
            String token = sb.toString();
            if (!token.isEmpty()) all.add(styleScalarValue(token, txt));
        }

        // trailing comment
        skipSpaces(ar, txt, all);
        if (ar.hasNext() && ar.peekChar() == '#') {
            all.add(txt.ofStyled(readToEnd(ar), NTextStyle.comments()));
        }
    }

    // -------------------------------------------------------------------------
    // Quoted strings  "..."  '...'
    // YAML single-quoted: '' is the only escape (double single-quote)
    // YAML double-quoted: standard \n \t \ uXXXX etc.
    // -------------------------------------------------------------------------

    private List<NText> readQuotedString(StringReaderExt ar, NTexts txt) {
        char quote = ar.peekChar();
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // opening quote

        if (quote == '\'') {
            while (ar.hasNext()) {
                char c = ar.readChar();
                sb.append(c);
                if (c == '\'') {
                    // '' inside single-quoted string = escaped single quote
                    if (ar.hasNext() && ar.peekChar() == '\'') {
                        sb.append(ar.readChar());
                    } else {
                        break; // closing quote
                    }
                }
            }
        } else {
            // double-quoted
            while (ar.hasNext()) {
                char c = ar.readChar();
                sb.append(c);
                if (c == '\\' && ar.hasNext()) {
                    sb.append(ar.readChar());
                    continue;
                }
                if (c == '"') break;
            }
        }

        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.string()));
    }

    // -------------------------------------------------------------------------
    // Number detection for bare scalars
    // Covers: integers, floats, hex (0x), octal (0o), scientific, +/- signs
    // -------------------------------------------------------------------------

    private boolean isNumber(String val) {
        if (val.isEmpty()) return false;
        String v = val;
        if (v.startsWith("+") || v.startsWith("-")) v = v.substring(1);
        if (v.isEmpty()) return false;
        // hex
        if (v.startsWith("0x") || v.startsWith("0X")) {
            return v.substring(2).chars().allMatch(c -> "0123456789abcdefABCDEF_".indexOf(c) >= 0);
        }
        // octal
        if (v.startsWith("0o") || v.startsWith("0O")) {
            return v.substring(2).chars().allMatch(c -> "01234567_".indexOf(c) >= 0);
        }
        // try parsing as double (covers int, float, scientific)
        try {
            Double.parseDouble(v.replace("_", ""));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void skipSpaces(StringReaderExt ar, NTexts txt, List<NText> all) {
        while (ar.hasNext() && ar.peekChar() == ' ') {
            all.add(txt.ofPlain(String.valueOf(ar.readChar())));
        }
    }

    private String readToEnd(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext()) sb.append(ar.readChar());
        return sb.toString();
    }

    private int leadingSpaces(String line) {
        int i = 0;
        while (i < line.length() && line.charAt(i) == ' ') i++;
        return i;
    }
}