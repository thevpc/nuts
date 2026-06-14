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

public class JsCodeHighlighter implements NCodeHighlighter {

    protected final Set<String> keywords = new HashSet<>();
    protected final Set<String> builtins = new HashSet<>();

    public JsCodeHighlighter() {
        initKeywords();
        initBuiltins();
    }

    protected void initKeywords() {
        keywords.addAll(Arrays.asList(
                // control flow
                "break", "case", "catch", "continue", "default", "do",
                "else", "finally", "for", "if", "return", "switch",
                "throw", "try", "while",
                // declarations
                "class", "const", "function", "let", "var",
                // OOP
                "delete", "extends", "in", "instanceof", "new",
                "super", "this", "typeof", "void",
                // modules
                "export", "import", "from", "as",
                // async
                "async", "await",
                // generators
                "yield",
                // other
                "debugger", "of", "with",
                // literals
                "false", "null", "true", "undefined"
        ));
    }

    protected void initBuiltins() {
        builtins.addAll(Arrays.asList(
                "Array", "Boolean", "console", "Date", "Error", "Function",
                "JSON", "Map", "Math", "Number", "Object", "Promise",
                "Proxy", "Reflect", "RegExp", "Set", "String", "Symbol",
                "WeakMap", "WeakRef", "WeakSet",
                "parseInt", "parseFloat", "isNaN", "isFinite",
                "encodeURI", "encodeURIComponent", "decodeURI", "decodeURIComponent",
                "setTimeout", "setInterval", "clearTimeout", "clearInterval",
                "fetch", "globalThis", "Infinity", "NaN"
        ));
    }

    @Override
    public String id() {
        return "js";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) return NScorable.DEFAULT_SCORE;
        switch (s) {
            case "js":
            case "javascript":
            case "mjs":
            case "cjs":
            case "text/javascript":
            case "application/javascript":
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
        StringReaderExt ar = new StringReaderExt(text);

        while (ar.hasNext()) {
            char c = ar.peekChar();

            if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
                continue;
            }

            // Comments
            if (c == '/') {
                if (ar.hasNext(1) && ar.peekChar(1) == '/') {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(ar)));
                    continue;
                }
                if (ar.hasNext(1) && ar.peekChar(1) == '*') {
                    all.addAll(Arrays.asList(StringReaderExtUtils.readSlashStarComments(ar)));
                    continue;
                }
                // Regex literal: only valid after operator/keyword context.
                // Heuristic: if previous non-space token was an operator or opening bracket, it's a regex.
                // We approximate by trying to read a regex; fall back to separator if it looks wrong.
                List<NText> regex = tryReadRegex(ar, txt);
                if (regex != null) {
                    all.addAll(regex);
                } else {
                    all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                }
                continue;
            }

            // Decorator (stage 3 proposal, widely used via Babel/TS)
            if (c == '@') {
                StringBuilder sb = new StringBuilder();
                sb.append(ar.readChar()); // @
                while (ar.hasNext() && (Character.isLetterOrDigit(ar.peekChar()) || ar.peekChar() == '_' || ar.peekChar() == '.')) {
                    sb.append(ar.readChar());
                }
                all.add(txt.ofStyled(sb.toString(), NTextStyle.annotation()));
                continue;
            }

            // Template literal
            if (c == '`') {
                all.addAll(readTemplateLiteral(ar, txt));
                continue;
            }

            // Single / double quoted strings
            if (c == '"' || c == '\'') {
                all.addAll(Arrays.asList(
                        c == '"'
                                ? StringReaderExtUtils.readJSDoubleQuotesString(ar)
                                : StringReaderExtUtils.readJSSimpleQuotes(ar)
                ));
                continue;
            }

            // Numbers
            if (Character.isDigit(c) || (c == '.' && ar.hasNext(1) && Character.isDigit(ar.peekChar(1)))) {
                all.addAll(readNumber(ar, txt));
                continue;
            }

            // Identifiers / keywords / builtins
            if (Character.isLetter(c) || c == '_' || c == '$') {
                all.addAll(readIdentifier(ar, txt));
                continue;
            }

            // Separators / operators
            all.add(txt.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
        }

        return txt.ofList(all.toArray(new NText[0]));
    }

    // -------------------------------------------------------------------------
    // Identifier
    // -------------------------------------------------------------------------

    protected List<NText> readIdentifier(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext() && (Character.isLetterOrDigit(ar.peekChar()) || ar.peekChar() == '_' || ar.peekChar() == '$')) {
            sb.append(ar.readChar());
        }
        String word = sb.toString();
        if (keywords.contains(word)) {
            return Collections.singletonList(txt.ofStyled(word, NTextStyle.keyword()));
        }
        if (builtins.contains(word)) {
            return Collections.singletonList(txt.ofStyled(word, NTextStyle.option()));
        }
        return Collections.singletonList(txt.ofPlain(word));
    }

    // -------------------------------------------------------------------------
    // Template literal  `hello ${name}!`
    // -------------------------------------------------------------------------

    protected List<NText> readTemplateLiteral(StringReaderExt ar, NTexts txt) {
        List<NText> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        current.append(ar.readChar()); // opening `

        while (ar.hasNext()) {
            char c = ar.peekChar();

            if (c == '\\') {
                current.append(ar.readChar());
                if (ar.hasNext()) current.append(ar.readChar());
                continue;
            }
            if (c == '`') {
                current.append(ar.readChar());
                break;
            }
            if (c == '$' && ar.hasNext(1) && ar.peekChar(1) == '{') {
                flushString(current, txt, result);
                result.addAll(readTemplateExpr(ar, txt));
                continue;
            }
            current.append(ar.readChar());
        }

        flushString(current, txt, result);
        return result;
    }

    /**
     * Reads ${expr} inside a template literal.
     * Braces are styled as separator; inner content is plain
     * (full re-parse would be recursive; not worth the complexity).
     */
    private List<NText> readTemplateExpr(StringReaderExt ar, NTexts txt) {
        StringBuilder expr = new StringBuilder();
        expr.append(ar.readChar()); // '$'
        expr.append(ar.readChar()); // '{'
        int depth = 1;
        while (ar.hasNext() && depth > 0) {
            char c = ar.readChar();
            expr.append(c);
            if (c == '{') depth++;
            else if (c == '}') depth--;
        }
        return Collections.singletonList(txt.ofStyled(expr.toString(), NTextStyle.separator()));
    }

    // -------------------------------------------------------------------------
    // Regex literal  /pattern/flags
    // Heuristic: must be non-empty, no unescaped newline, flags are [gimsuy]*
    // Returns null if it doesn't look like a valid regex (caller emits '/' as separator)
    // -------------------------------------------------------------------------

    protected List<NText> tryReadRegex(StringReaderExt ar, NTexts txt) {
        // We need to speculatively read; save state isn't available on StringReaderExt
        // so we commit only if it passes basic validation.
        // Peek ahead manually.
        int i = 1; // skip leading '/'
        if (!ar.hasNext(i)) return null;
        if (ar.peekChar(i) == '/' || ar.peekChar(i) == '*') return null; // comment
        if (ar.peekChar(i) == ' ' || ar.peekChar(i) == '\n') return null; // division

        // scan until closing unescaped '/' or newline
        boolean inClass = false;
        while (ar.hasNext(i)) {
            char c = ar.peekChar(i);
            if (c == '\n') return null; // unterminated
            if (c == '\\') { i += 2; continue; }
            if (c == '[') { inClass = true; i++; continue; }
            if (c == ']') { inClass = false; i++; continue; }
            if (c == '/' && !inClass) { i++; break; }
            i++;
        }

        // collect flags
        while (ar.hasNext(i) && "gimsuyv".indexOf(ar.peekChar(i)) >= 0) i++;

        // now commit: consume i characters
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) sb.append(ar.readChar());
        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.string()));
    }

    // -------------------------------------------------------------------------
    // Numbers: 0x, 0o, 0b, BigInt (n suffix), floats, exponents
    // -------------------------------------------------------------------------

    protected List<NText> readNumber(StringReaderExt ar, NTexts txt) {
        StringBuilder sb = new StringBuilder();

        if (ar.peekChar() == '0' && ar.hasNext(1)) {
            char next = ar.peekChar(1);
            if (next == 'x' || next == 'X') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && isHexOrUnderscore(ar.peekChar())) sb.append(ar.readChar());
                if (ar.hasNext() && ar.peekChar() == 'n') sb.append(ar.readChar()); // BigInt
                return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
            }
            if (next == 'o' || next == 'O') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && (ar.peekChar() >= '0' && ar.peekChar() <= '7' || ar.peekChar() == '_')) sb.append(ar.readChar());
                if (ar.hasNext() && ar.peekChar() == 'n') sb.append(ar.readChar());
                return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
            }
            if (next == 'b' || next == 'B') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && (ar.peekChar() == '0' || ar.peekChar() == '1' || ar.peekChar() == '_')) sb.append(ar.readChar());
                if (ar.hasNext() && ar.peekChar() == 'n') sb.append(ar.readChar());
                return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
            }
        }

        while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) sb.append(ar.readChar());
        if (ar.hasNext() && ar.peekChar() == '.' && ar.hasNext(1) && Character.isDigit(ar.peekChar(1))) {
            sb.append(ar.readChar());
            while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) sb.append(ar.readChar());
        }
        if (ar.hasNext() && (ar.peekChar() == 'e' || ar.peekChar() == 'E')) {
            sb.append(ar.readChar());
            if (ar.hasNext() && (ar.peekChar() == '+' || ar.peekChar() == '-')) sb.append(ar.readChar());
            while (ar.hasNext() && Character.isDigit(ar.peekChar())) sb.append(ar.readChar());
        }
        if (ar.hasNext() && ar.peekChar() == 'n') sb.append(ar.readChar()); // BigInt

        return Collections.singletonList(txt.ofStyled(sb.toString(), NTextStyle.number()));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    protected void flushString(StringBuilder sb, NTexts txt, List<NText> result) {
        if (sb.length() > 0) {
            result.add(txt.ofStyled(sb.toString(), NTextStyle.string()));
            sb.setLength(0);
        }
    }

    private boolean isHexOrUnderscore(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F') || c == '_';
    }
}