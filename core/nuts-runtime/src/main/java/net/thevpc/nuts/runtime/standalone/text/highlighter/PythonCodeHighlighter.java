package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;

import java.util.*;

public class PythonCodeHighlighter implements NCodeHighlighter {

    private final Set<String> keywords = new HashSet<>();
    private final Set<String> builtins = new HashSet<>();

    public PythonCodeHighlighter() {
        // keywords
        keywords.addAll(Arrays.asList(
                "False", "None", "True",
                "and", "as", "assert", "async", "await",
                "break", "class", "continue", "def", "del",
                "elif", "else", "except", "finally", "for",
                "from", "global", "if", "import", "in",
                "is", "lambda", "nonlocal", "not", "or",
                "pass", "raise", "return", "try", "while",
                "with", "yield"
        ));
        // common builtins — styled distinctly (e.g. NTextStyle.option() or similar)
        builtins.addAll(Arrays.asList(
                "abs", "all", "any", "bin", "bool", "breakpoint",
                "bytearray", "bytes", "callable", "chr", "classmethod",
                "compile", "complex", "delattr", "dict", "dir", "divmod",
                "enumerate", "eval", "exec", "filter", "float", "format",
                "frozenset", "getattr", "globals", "hasattr", "hash",
                "help", "hex", "id", "input", "int", "isinstance",
                "issubclass", "iter", "len", "list", "locals", "map",
                "max", "memoryview", "min", "next", "object", "oct",
                "open", "ord", "pow", "print", "property", "range",
                "repr", "reversed", "round", "set", "setattr", "slice",
                "sorted", "staticmethod", "str", "sum", "super", "tuple",
                "type", "vars", "zip"
        ));
    }

    @Override
    public String id() {
        return "python";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) {
            return NScorable.DEFAULT_SCORE;
        }
        switch (s) {
            case "python":
            case "py":
            case "python3":
            case "text/x-python":
            {
                return NScorable.DEFAULT_SCORE;
            }
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
        StringReaderExt ar = new StringReaderExt(text);

        while (ar.hasNext()) {
            char c = ar.peekChar();

            // --- Whitespace ---
            if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
                continue;
            }

            // --- Comment ---
            if (c == '#') {
                StringBuilder sb = new StringBuilder();
                while (ar.hasNext() && ar.peekChar() != '\n') {
                    sb.append(ar.readChar());
                }
                all.add(txt.ofStyled(sb.toString(), NTextStyle.comments()));
                continue;
            }

            // --- Decorator ---
            if (c == '@') {
                StringBuilder sb = new StringBuilder();
                sb.append(ar.readChar()); // @
                while (ar.hasNext() && (Character.isLetterOrDigit(ar.peekChar()) || ar.peekChar() == '_' || ar.peekChar() == '.')) {
                    sb.append(ar.readChar());
                }
                all.add(txt.ofStyled(sb.toString(), NTextStyle.annotation()));
                continue;
            }

            // --- String literals: f/b/r/u prefixes + triple or single quotes ---
            if (isStringStart(ar)) {
                all.addAll(readString(ar, txt));
                continue;
            }

            // --- Numbers ---
            if (Character.isDigit(c) || (c == '.' && ar.hasNext(1) && Character.isDigit(ar.peekChar(1)))) {
                all.addAll(readNumber(ar, txt));
                continue;
            }

            // --- Identifiers and keywords ---
            if (Character.isLetter(c) || c == '_') {
                StringBuilder sb = new StringBuilder();
                while (ar.hasNext() && (Character.isLetterOrDigit(ar.peekChar()) || ar.peekChar() == '_')) {
                    sb.append(ar.readChar());
                }
                String word = sb.toString();
                if (keywords.contains(word)) {
                    all.add(txt.ofStyled(word, NTextStyle.keyword()));
                } else if (builtins.contains(word)) {
                    // use 'option' style to visually separate builtins from user identifiers
                    all.add(txt.ofStyled(word, NTextStyle.option()));
                } else {
                    all.add(txt.ofPlain(word));
                }
                continue;
            }

            // --- Operators and separators ---
            switch (c) {
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                case ',':
                case ':':
                case ';':
                case '=':
                case '+':
                case '-':
                case '*':
                case '/':
                case '%':
                case '<':
                case '>':
                case '!':
                case '&':
                case '|':
                case '^':
                case '~':
                case '.':
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    break;
                default:
                    all.add(txt.ofPlain(String.valueOf(ar.readChar())));
                    break;
            }
        }

        return txt.ofList(all.toArray(new NText[0]));
    }

    // -------------------------------------------------------------------------
    // String detection
    // -------------------------------------------------------------------------

    /**
     * Returns true if the reader is positioned at the start of a Python string:
     * optional prefix (f, b, r, u, rb, br, fr, rf — case-insensitive) followed by ' or "
     */
    private boolean isStringStart(StringReaderExt ar) {
        int i = 0;
        char c0 = ar.peekChar(i);
        // optional prefix characters
        if (isPrefixChar(c0)) {
            i++;
            if (ar.hasNext(i) && isPrefixChar(ar.peekChar(i))) {
                i++;
            }
        }
        return ar.hasNext(i) && (ar.peekChar(i) == '"' || ar.peekChar(i) == '\'');
    }

    private boolean isPrefixChar(char c) {
        return c == 'f' || c == 'F' || c == 'b' || c == 'B'
                || c == 'r' || c == 'R' || c == 'u' || c == 'U';
    }

    // -------------------------------------------------------------------------
    // String reading
    // -------------------------------------------------------------------------

    private List<NText> readString(StringReaderExt ar, NTexts txt) {
        List<NText> result = new ArrayList<>();

        // collect prefix
        StringBuilder prefix = new StringBuilder();
        while (ar.hasNext() && isPrefixChar(ar.peekChar())) {
            prefix.append(ar.readChar());
        }

        boolean isFString = prefix.toString().toLowerCase().contains("f");
        char quote = ar.peekChar(); // ' or "

        // detect triple quote
        boolean triple = ar.hasNext(2)
                && ar.peekChar(1) == quote
                && ar.peekChar(2) == quote;

        if (triple) {
            result.addAll(readTripleString(ar, txt, prefix.toString(), quote, isFString));
        } else {
            result.addAll(readSingleLineString(ar, txt, prefix.toString(), quote, isFString));
        }
        return result;
    }

    /**
     * Triple-quoted string: may span multiple lines.
     * f-strings: {expr} blocks are highlighted as plain (expression content is arbitrary).
     */
    private List<NText> readTripleString(StringReaderExt ar, NTexts txt, String prefix, char quote, boolean isFString) {
        List<NText> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        // opening prefix + triple quote
        current.append(prefix);
        current.append(ar.readChar()); // quote
        current.append(ar.readChar()); // quote
        current.append(ar.readChar()); // quote

        while (ar.hasNext()) {
            if (ar.peekChar() == '\\') {
                current.append(ar.readChar());
                if (ar.hasNext()) current.append(ar.readChar());
                continue;
            }
            // closing triple quote
            if (ar.peekChar() == quote && ar.hasNext(1) && ar.peekChar(1) == quote
                    && ar.hasNext(2) && ar.peekChar(2) == quote) {
                current.append(ar.readChar());
                current.append(ar.readChar());
                current.append(ar.readChar());
                break;
            }
            // f-string interpolation
            if (isFString && ar.peekChar() == '{') {
                if (ar.hasNext(1) && ar.peekChar(1) == '{') {
                    // escaped brace
                    current.append(ar.readChar());
                    current.append(ar.readChar());
                    continue;
                }
                flushString(current, txt, result);
                result.addAll(readFStringExpr(ar, txt));
                continue;
            }
            current.append(ar.readChar());
        }

        flushString(current, txt, result);
        return result;
    }

    /**
     * Single-line string (single or double quoted, no newline crossing).
     */
    private List<NText> readSingleLineString(StringReaderExt ar, NTexts txt, String prefix, char quote, boolean isFString) {
        List<NText> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        current.append(prefix);
        current.append(ar.readChar()); // opening quote

        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\n') break; // unterminated — stop gracefully
            if (c == '\\') {
                current.append(ar.readChar());
                if (ar.hasNext()) current.append(ar.readChar());
                continue;
            }
            if (c == quote) {
                current.append(ar.readChar());
                break;
            }
            if (isFString && c == '{') {
                if (ar.hasNext(1) && ar.peekChar(1) == '{') {
                    current.append(ar.readChar());
                    current.append(ar.readChar());
                    continue;
                }
                flushString(current, txt, result);
                result.addAll(readFStringExpr(ar, txt));
                continue;
            }
            current.append(ar.readChar());
        }

        flushString(current, txt, result);
        return result;
    }

    /**
     * Read a {expr} block inside an f-string.
     * The braces are styled as separator; the content inside as plain
     * (it's arbitrary Python — deep re-parsing would be recursive overkill).
     */
    private List<NText> readFStringExpr(StringReaderExt ar, NTexts txt) {
        List<NText> result = new ArrayList<>();
        StringBuilder expr = new StringBuilder();
        expr.append(ar.readChar()); // '{'
        int depth = 1;
        while (ar.hasNext() && depth > 0) {
            char c = ar.readChar();
            expr.append(c);
            if (c == '{') depth++;
            else if (c == '}') depth--;
        }
        result.add(txt.ofStyled(expr.toString(), NTextStyle.separator()));
        return result;
    }

    private void flushString(StringBuilder sb, NTexts txt, List<NText> result) {
        if (sb.length() > 0) {
            result.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
            sb.setLength(0);
        }
    }

    // -------------------------------------------------------------------------
    // Number reading
    // -------------------------------------------------------------------------

    /**
     * Handles: integers, floats, hex (0x), octal (0o), binary (0b),
     * underscores as digit separators (1_000_000), and exponents (1.5e-3).
     */
    private List<NText> readNumber(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();

        if (ar.peekChar() == '0' && ar.hasNext(1)) {
            char next = ar.peekChar(1);
            if (next == 'x' || next == 'X') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && isHexDigitOrUnderscore(ar.peekChar())) sb.append(ar.readChar());
                return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
            }
            if (next == 'o' || next == 'O') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && (isOctalDigit(ar.peekChar()) || ar.peekChar() == '_')) sb.append(ar.readChar());
                return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
            }
            if (next == 'b' || next == 'B') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && (ar.peekChar() == '0' || ar.peekChar() == '1' || ar.peekChar() == '_')) sb.append(ar.readChar());
                return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
            }
        }

        // integer or float part
        while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) sb.append(ar.readChar());

        // decimal point
        if (ar.hasNext() && ar.peekChar() == '.' && ar.hasNext(1) && Character.isDigit(ar.peekChar(1))) {
            sb.append(ar.readChar()); // '.'
            while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) sb.append(ar.readChar());
        }

        // exponent
        if (ar.hasNext() && (ar.peekChar() == 'e' || ar.peekChar() == 'E')) {
            sb.append(ar.readChar());
            if (ar.hasNext() && (ar.peekChar() == '+' || ar.peekChar() == '-')) sb.append(ar.readChar());
            while (ar.hasNext() && Character.isDigit(ar.peekChar())) sb.append(ar.readChar());
        }

        // optional complex suffix
        if (ar.hasNext() && (ar.peekChar() == 'j' || ar.peekChar() == 'J')) {
            sb.append(ar.readChar());
        }

        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
    }

    private boolean isHexDigitOrUnderscore(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F') || c == '_';
    }

    private boolean isOctalDigit(char c) {
        return c >= '0' && c <= '7';
    }
}