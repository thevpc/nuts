package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Syntax highlighter for Scala 2 and Scala 3 sources.
 * <p>
 * Handles the constructs that are specific enough to matter when reading Scala:
 * nested block comments, triple-quoted and interpolated strings, character and
 * symbol literals, annotations, back-quoted identifiers and numeric suffixes.
 */
public class ScalaCodeHighlighter implements NCodeHighlighter {

    private final Set<String> keywords = new HashSet<>();
    private final Set<String> types = new HashSet<>();

    public ScalaCodeHighlighter() {
        keywords.addAll(Arrays.asList(
                // hard keywords
                "abstract", "case", "catch", "class", "def", "do", "else", "enum",
                "export", "extends", "false", "final", "finally", "for", "forSome",
                "if", "implicit", "import", "lazy", "macro", "match", "new", "null",
                "object", "override", "package", "private", "protected", "return",
                "sealed", "super", "then", "this", "throw", "trait", "true", "try",
                "type", "val", "var", "while", "with", "yield",
                // Scala 3 keywords and soft keywords
                "as", "derives", "end", "extension", "given", "infix", "inline",
                "opaque", "open", "transparent", "using", "_"
        ));
        types.addAll(Arrays.asList(
                // core types
                "Any", "AnyRef", "AnyVal", "Nothing", "Null", "Unit",
                "Boolean", "Byte", "Char", "Short", "Int", "Long", "Float", "Double", "String",
                // standard library types commonly seen in signatures
                "Option", "Some", "None", "Either", "Left", "Right", "Try", "Success", "Failure",
                "Future", "Promise", "Await", "Duration",
                "List", "Nil", "Seq", "IndexedSeq", "Vector", "Array", "ArrayBuffer", "ListBuffer",
                "Set", "HashSet", "TreeSet", "LinkedHashSet",
                "Map", "HashMap", "TreeMap", "LinkedHashMap",
                "Iterable", "Iterator", "Traversable", "Range", "Stream", "LazyList",
                "Function", "PartialFunction", "Ordering", "Ordered", "Tuple",
                "BigInt", "BigDecimal", "ClassTag", "TypeTag", "Manifest",
                "Console", "Predef", "App"
        ));
    }

    @Override
    public String id() {
        return "scala";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if (s == null) {
            return NScorable.DEFAULT_SCORE;
        }
        switch (s) {
            case "scala":
            case "sc":
            case "scala3":
            case "sbt":
            case "text/scala":
            case "text/x-scala":
            case "application/x-scala": {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public NText tokenToText(String text, String nodeType) {
        return NText.ofPlain(text);
    }

    @Override
    public NText stringToText(String text) {
        List<NText> all = new ArrayList<>();
        StringReaderExt ar = new StringReaderExt(text);
        while (ar.hasNext()) {
            char c = ar.peekChar();

            if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
                continue;
            }

            // Comments: // line, /* nested block */
            if (c == '/' && ar.hasNext(1) && ar.peekChar(1) == '/') {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSlashSlashComments(ar)));
                continue;
            }
            if (c == '/' && ar.hasNext(1) && ar.peekChar(1) == '*') {
                all.addAll(Arrays.asList(readNestedBlockComment(ar)));
                continue;
            }

            // Strings
            if (c == '"') {
                all.addAll(readString(ar, false));
                continue;
            }

            // Character literal 'x', escape '\n' or Scala 2 symbol literal 'name
            if (c == '\'') {
                all.addAll(readCharOrSymbol(ar));
                continue;
            }

            // Back-quoted identifier: `a-b`
            if (c == '`') {
                all.addAll(readBackQuotedIdentifier(ar));
                continue;
            }

            // Annotation @Override / @foo.bar
            if (c == '@' && ar.hasNext(1) && isIdentifierStart(ar.peekChar(1))) {
                all.addAll(readAnnotation(ar));
                continue;
            }

            // Numbers
            if (Character.isDigit(c) || (c == '.' && ar.hasNext(1) && Character.isDigit(ar.peekChar(1)))) {
                all.addAll(readNumber(ar));
                continue;
            }

            // Identifiers, keywords and interpolated string prefixes s"..." f"..." raw"..."
            if (isIdentifierStart(c)) {
                List<NText> id = readIdentifier(ar);
                if (ar.hasNext() && ar.peekChar() == '"') {
                    // any identifier directly followed by a quote is an interpolator prefix
                    NText prefix = id.size() == 1 ? id.get(0) : NText.ofList(id.toArray(new NText[0]));
                    List<NText> parts = new ArrayList<>();
                    parts.add(NText.ofStyled(prefix, NTextStyle.annotation()));
                    parts.addAll(readString(ar, true));
                    all.addAll(parts);
                } else {
                    all.addAll(id);
                }
                continue;
            }

            // Brackets, commas, semicolons and dots
            switch (c) {
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                case ',':
                case ';':
                case '.':
                    all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                    continue;
                default:
                    break;
            }

            // Symbolic operators: + - * / % :: => -> <- =:= etc.
            if (isOperatorChar(c)) {
                StringBuilder sb = new StringBuilder();
                while (ar.hasNext() && isOperatorChar(ar.peekChar())) {
                    char oc = ar.peekChar();
                    if (oc == '/' && ar.hasNext(1)
                            && (ar.peekChar(1) == '/' || ar.peekChar(1) == '*')) {
                        break;
                    }
                    sb.append(ar.readChar());
                }
                all.add(NText.ofStyled(sb.toString(), NTextStyle.operator()));
                continue;
            }

            all.add(NText.ofPlain(String.valueOf(ar.readChar())));
        }
        return NText.ofList(all.toArray(new NText[0]));
    }

    // -------------------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------------------

    /**
     * Scala block comments nest, unlike Java/C block comments.
     */
    private NText[] readNestedBlockComment(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // '/'
        sb.append(ar.readChar()); // '*'
        int depth = 1;
        while (ar.hasNext() && depth > 0) {
            char c = ar.readChar();
            sb.append(c);
            if (c == '/' && ar.hasNext() && ar.peekChar() == '*') {
                sb.append(ar.readChar());
                depth++;
            } else if (c == '*' && ar.hasNext() && ar.peekChar() == '/') {
                sb.append(ar.readChar());
                depth--;
            }
        }
        return new NText[]{NText.ofStyled(sb.toString(), NTextStyle.comments())};
    }

    // -------------------------------------------------------------------------
    // Strings and interpolation
    // -------------------------------------------------------------------------

    private List<NText> readString(StringReaderExt ar, boolean interpolated) {
        if (ar.hasNext(2) && ar.peekChar(1) == '"' && ar.peekChar(2) == '"') {
            return readTripleQuotedString(ar, interpolated);
        }
        return readSingleQuotedString(ar, interpolated);
    }

    private List<NText> readSingleQuotedString(StringReaderExt ar, boolean interpolated) {
        List<NText> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // opening quote
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == '\\') {
                flushString(sb, all);
                sb.append(ar.readChar());
                if (ar.hasNext()) {
                    sb.append(ar.readChar());
                }
                all.add(NText.ofStyled(sb.toString(), NTextStyle.separator()));
                sb.setLength(0);
                continue;
            }
            if (c == '"') {
                sb.append(ar.readChar());
                break;
            }
            if (c == '\r' || c == '\n') {
                // unterminated string: stop at end of line, never swallow the rest
                break;
            }
            if (interpolated && c == '$') {
                if (ar.hasNext(1) && ar.peekChar(1) == '$') {
                    sb.append(ar.readChar());
                    sb.append(ar.readChar());
                    continue;
                }
                flushString(sb, all);
                all.addAll(readInterpolation(ar));
                continue;
            }
            sb.append(ar.readChar());
        }
        flushString(sb, all);
        return all;
    }

    private List<NText> readTripleQuotedString(StringReaderExt ar, boolean interpolated) {
        List<NText> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
        sb.append(ar.readChar());
        sb.append(ar.readChar()); // opening """
        while (ar.hasNext()) {
            if (ar.peekChars(3).equals("\"\"\"")) {
                sb.append(ar.nextChars(3));
                break;
            }
            char c = ar.peekChar();
            if (interpolated && c == '$') {
                if (ar.hasNext(1) && ar.peekChar(1) == '$') {
                    sb.append(ar.readChar());
                    sb.append(ar.readChar());
                    continue;
                }
                flushString(sb, all);
                all.addAll(readInterpolation(ar));
                continue;
            }
            sb.append(ar.readChar());
        }
        flushString(sb, all);
        return all;
    }

    /**
     * Reads ${expr}, $name or a lone $ inside an interpolated string.
     */
    private List<NText> readInterpolation(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // '$'
        if (ar.hasNext() && ar.peekChar() == '{') {
            sb.append(ar.readChar());
            int depth = 1;
            while (ar.hasNext() && depth > 0) {
                char c = ar.readChar();
                sb.append(c);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                }
            }
            return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.variable()));
        }
        if (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
            while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
                sb.append(ar.readChar());
            }
            return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.variable()));
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.string()));
    }

    private void flushString(StringBuilder sb, List<NText> all) {
        if (sb.length() > 0) {
            all.add(NText.ofStyled(sb.toString(), NTextStyle.string()));
            sb.setLength(0);
        }
    }

    // -------------------------------------------------------------------------
    // Character, symbol and back-quoted literals
    // -------------------------------------------------------------------------

    private List<NText> readCharOrSymbol(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // '
        if (!ar.hasNext()) {
            return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.operator()));
        }
        char c = ar.peekChar();
        if (c == '\\') {
            sb.append(ar.readChar());
            if (ar.hasNext()) {
                sb.append(ar.readChar());
            }
            if (ar.hasNext() && ar.peekChar() == '\'') {
                sb.append(ar.readChar());
            }
            return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.string()));
        }
        if (ar.hasNext(1) && ar.peekChar(1) == '\'') {
            sb.append(ar.readChar()); // char
            sb.append(ar.readChar()); // closing quote
            return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.string()));
        }
        if (isIdentifierStart(c)) {
            // Scala 2 symbol literal: 'name
            while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
                sb.append(ar.readChar());
            }
            return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.annotation()));
        }
        // Scala 3 quoted code: '{ ... }, '[...]
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.operator()));
    }

    private List<NText> readBackQuotedIdentifier(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // `
        while (ar.hasNext()) {
            char c = ar.readChar();
            sb.append(c);
            if (c == '`') {
                break;
            }
            if (c == '\r' || c == '\n') {
                break;
            }
        }
        return Collections.singletonList(NText.ofPlain(sb.toString()));
    }

    private List<NText> readAnnotation(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // @
        while (ar.hasNext() && (isIdentifierPart(ar.peekChar()) || ar.peekChar() == '.')) {
            sb.append(ar.readChar());
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.annotation()));
    }

    // -------------------------------------------------------------------------
    // Identifiers
    // -------------------------------------------------------------------------

    private List<NText> readIdentifier(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
        while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
            sb.append(ar.readChar());
        }
        String word = sb.toString();
        if (keywords.contains(word) || "_".equals(word)) {
            return Collections.singletonList(NText.ofStyled(word, NTextStyle.keyword()));
        }
        if ("true".equals(word) || "false".equals(word)) {
            return Collections.singletonList(NText.ofStyled(word, NTextStyle.bool()));
        }
        if (types.contains(word)) {
            return Collections.singletonList(NText.ofStyled(word, NTextStyle.option()));
        }
        return Collections.singletonList(NText.ofPlain(word));
    }

    private boolean isIdentifierStart(char c) {
        return Character.isJavaIdentifierStart(c) || c == '_' || c == '$';
    }

    private boolean isIdentifierPart(char c) {
        return Character.isJavaIdentifierPart(c) || c == '$';
    }

    // -------------------------------------------------------------------------
    // Numbers
    // -------------------------------------------------------------------------

    private List<NText> readNumber(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();

        if (ar.peekChar() == '.') {
            sb.append(ar.readChar());
            while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) {
                sb.append(ar.readChar());
            }
            readExponent(ar, sb);
            readNumericSuffix(ar, sb);
            return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.number()));
        }

        if (ar.peekChar() == '0' && ar.hasNext(1)) {
            char next = ar.peekChar(1);
            if (next == 'x' || next == 'X') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && isHexDigitOrUnderscore(ar.peekChar())) {
                    sb.append(ar.readChar());
                }
                readNumericSuffix(ar, sb);
                return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.number()));
            }
            if (next == 'b' || next == 'B') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && (ar.peekChar() == '0' || ar.peekChar() == '1' || ar.peekChar() == '_')) {
                    sb.append(ar.readChar());
                }
                readNumericSuffix(ar, sb);
                return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.number()));
            }
        }

        while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) {
            sb.append(ar.readChar());
        }
        if (ar.hasNext() && ar.peekChar() == '.' && ar.hasNext(1) && Character.isDigit(ar.peekChar(1))) {
            sb.append(ar.readChar());
            while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) {
                sb.append(ar.readChar());
            }
        }
        readExponent(ar, sb);
        readNumericSuffix(ar, sb);
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.number()));
    }

    private void readExponent(StringReaderExt ar, StringBuilder sb) {
        if (ar.hasNext() && (ar.peekChar() == 'e' || ar.peekChar() == 'E')) {
            sb.append(ar.readChar());
            if (ar.hasNext() && (ar.peekChar() == '+' || ar.peekChar() == '-')) {
                sb.append(ar.readChar());
            }
            while (ar.hasNext() && Character.isDigit(ar.peekChar())) {
                sb.append(ar.readChar());
            }
        }
    }

    private void readNumericSuffix(StringReaderExt ar, StringBuilder sb) {
        if (ar.hasNext()) {
            char c = ar.peekChar();
            if (c == 'L' || c == 'l' || c == 'F' || c == 'f' || c == 'D' || c == 'd') {
                sb.append(ar.readChar());
            }
        }
    }

    private boolean isHexDigitOrUnderscore(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F') || c == '_';
    }

    private boolean isOperatorChar(char c) {
        return "!#%&*+-/:<=>?@\\^|~⇒→←≥≤≠".indexOf(c) >= 0;
    }
}
