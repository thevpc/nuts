package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.runtime.standalone.xtra.expr.StringReaderExt;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class for small, lexer based highlighters.
 * <p>
 * It implements the common plumbing (whitespace, comments, strings, numbers,
 * identifiers, operators) so a new language highlighter only has to declare
 * its keywords/types and, when needed, override a handful of hooks.
 */
public abstract class AbstractCodeHighlighter implements NCodeHighlighter {

    private final String id;

    protected final Set<String> keywords = new LinkedHashSet<>();
    protected final Set<String> types = new LinkedHashSet<>();
    protected final Set<String> booleanLiterals = new LinkedHashSet<>();
    protected final Set<String> nullLiterals = new LinkedHashSet<>();
    protected final Set<String> lineComments = new LinkedHashSet<>();

    protected String blockCommentOpen;
    protected String blockCommentClose;
    protected boolean nestedBlockComments;

    protected final Set<Character> stringQuotes = new LinkedHashSet<>();
    protected boolean tripleQuotes;
    protected boolean tripleStringsUseEscapes;
    protected boolean charLiterals;
    protected boolean backtickStrings;
    protected boolean backtickIdentifiers;
    protected boolean interpolation;
    protected boolean annotations;

    protected String operatorChars = "+-*/%=<>!&|^~?:";
    protected String separatorChars = "()[]{}.,;";
    protected String numberSuffixChars = "uUlLfFdDiI";

    protected AbstractCodeHighlighter(String id) {
        this.id = id;
        this.stringQuotes.add('"');
    }

    @Override
    public String id() {
        return id;
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
            if (handleSpecial(ar, all)) {
                continue;
            }
            char c = ar.peekChar();
            if (Character.isWhitespace(c)) {
                all.addAll(Arrays.asList(StringReaderExtUtils.readSpaces(ar)));
                continue;
            }
            if (isLineCommentStart(ar)) {
                all.addAll(readLineComment(ar));
                continue;
            }
            if (isBlockCommentStart(ar)) {
                all.addAll(readBlockComment(ar));
                continue;
            }
            if (isStringStart(ar)) {
                all.addAll(readString(ar));
                continue;
            }
            if (isCharLiteralStart(ar)) {
                all.addAll(readCharLiteral(ar));
                continue;
            }
            if (isNumberStart(ar)) {
                all.addAll(readNumber(ar));
                continue;
            }
            if (backtickIdentifiers && c == '`') {
                all.addAll(readBacktickIdentifier(ar));
                continue;
            }
            if (isIdentifierStart(c)) {
                all.addAll(readIdentifier(ar));
                continue;
            }
            if (annotations && c == '@' && ar.hasNext(1) && isIdentifierStart(ar.peekChar(1))) {
                all.addAll(readAnnotation(ar));
                continue;
            }
            if (separatorChars.indexOf(c) >= 0) {
                all.add(NText.ofStyled(String.valueOf(ar.readChar()), NTextStyle.separator()));
                continue;
            }
            if (isOperatorChar(c)) {
                all.addAll(readOperatorRun(ar));
                continue;
            }
            all.add(NText.ofPlain(String.valueOf(ar.readChar())));
        }
        return NText.ofList(all.toArray(new NText[0]));
    }

    // -------------------------------------------------------------------------
    // Extension hooks
    // -------------------------------------------------------------------------

    /**
     * Language specific constructs (directives, symbols, variables, heredocs...).
     * Return {@code true} when the reader has been advanced.
     */
    protected boolean handleSpecial(StringReaderExt ar, List<NText> all) {
        return false;
    }

    protected boolean isIdentifierStart(char c) {
        return Character.isJavaIdentifierStart(c) || c == '_' || c == '$';
    }

    protected boolean isIdentifierPart(char c) {
        return Character.isJavaIdentifierPart(c) || c == '$';
    }

    protected NTextStyles styleForWord(String word) {
        if (booleanLiterals.contains(word)) {
            return NTextStyles.of(NTextStyle.bool());
        }
        if (nullLiterals.contains(word)) {
            return NTextStyles.of(NTextStyle.keyword());
        }
        if (keywords.contains(word)) {
            return NTextStyles.of(NTextStyle.keyword());
        }
        if (types.contains(word)) {
            return NTextStyles.of(NTextStyle.option());
        }
        return null;
    }

    protected boolean isLineCommentStart(StringReaderExt ar) {
        for (String prefix : lineComments) {
            if (startsWith(ar, prefix)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isBlockCommentStart(StringReaderExt ar) {
        return blockCommentOpen != null && startsWith(ar, blockCommentOpen);
    }

    protected boolean isStringStart(StringReaderExt ar) {
        char c = ar.peekChar();
        return stringQuotes.contains(c) || (backtickStrings && c == '`');
    }

    protected boolean isCharLiteralStart(StringReaderExt ar) {
        if (!charLiterals || ar.peekChar() != '\'') {
            return false;
        }
        if (ar.hasNext(1) && ar.peekChar(1) == '\\') {
            return true;
        }
        return ar.hasNext(2) && ar.peekChar(2) == '\'';
    }

    protected boolean isNumberStart(StringReaderExt ar) {
        char c = ar.peekChar();
        return Character.isDigit(c) || (c == '.' && ar.hasNext(1) && Character.isDigit(ar.peekChar(1)));
    }

    protected boolean isOperatorChar(char c) {
        return operatorChars.indexOf(c) >= 0;
    }

    protected boolean isInterpolationStart(StringReaderExt ar) {
        return interpolation && ar.peekChar() == '$'
                && ar.hasNext(1) && (ar.peekChar(1) == '{' || isIdentifierStart(ar.peekChar(1)));
    }

    /**
     * Reads an interpolation hole. The default implementation supports
     * {@code ${expr}} and {@code $name}.
     */
    protected List<NText> readInterpolation(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // $
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
        } else {
            while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
                sb.append(ar.readChar());
            }
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.variable()));
    }

    // -------------------------------------------------------------------------
    // Comments
    // -------------------------------------------------------------------------

    protected List<NText> readLineComment(StringReaderExt ar) {
        String prefix = null;
        for (String p : lineComments) {
            if (startsWith(ar, p)) {
                prefix = p;
                break;
            }
        }
        if (prefix == null) {
            return Collections.singletonList(NText.ofPlain(String.valueOf(ar.readChar())));
        }
        StringBuilder sb = new StringBuilder(prefix);
        ar.nextChars(prefix.length());
        while (ar.hasNext() && ar.peekChar() != '\n' && ar.peekChar() != '\r') {
            sb.append(ar.readChar());
        }
        if (ar.hasNext()) {
            char c = ar.readChar();
            sb.append(c);
            if (c == '\r' && ar.hasNext() && ar.peekChar() == '\n') {
                sb.append(ar.readChar());
            }
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.comments()));
    }

    protected List<NText> readBlockComment(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder(blockCommentOpen);
        ar.nextChars(blockCommentOpen.length());
        int depth = 1;
        while (ar.hasNext() && depth > 0) {
            if (nestedBlockComments && startsWith(ar, blockCommentOpen)) {
                sb.append(blockCommentOpen);
                ar.nextChars(blockCommentOpen.length());
                depth++;
            } else if (startsWith(ar, blockCommentClose)) {
                sb.append(blockCommentClose);
                ar.nextChars(blockCommentClose.length());
                depth--;
            } else {
                sb.append(ar.readChar());
            }
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.comments()));
    }

    // -------------------------------------------------------------------------
    // Strings and interpolation
    // -------------------------------------------------------------------------

    protected List<NText> readString(StringReaderExt ar) {
        char quote = ar.peekChar();
        if (tripleQuotes && ar.peekChars(3).equals("" + quote + quote + quote)) {
            return readTripleString(ar, quote);
        }
        if (backtickStrings && quote == '`') {
            return readBacktickString(ar);
        }
        return readQuotedString(ar, quote, true);
    }

    protected List<NText> readTripleString(StringReaderExt ar, char quote) {
        List<NText> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(ar.nextChars(3));
        String fence = "" + quote + quote + quote;
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (startsWith(ar, fence)) {
                sb.append(ar.nextChars(3));
                break;
            }
            if (isInterpolationStart(ar)) {
                flush(sb, all, NTextStyle.string());
                all.addAll(readInterpolation(ar));
                continue;
            }
            if (tripleStringsUseEscapes && ar.peekChar() == '\\') {
                flush(sb, all, NTextStyle.string());
                sb.append(ar.readChar());
                if (ar.hasNext()) {
                    sb.append(ar.readChar());
                }
                all.add(NText.ofStyled(sb.toString(), NTextStyle.separator()));
                sb.setLength(0);
                continue;
            }
            sb.append(ar.readChar());
        }
        flush(sb, all, NTextStyle.string());
        return all;
    }

    protected List<NText> readQuotedString(StringReaderExt ar, char quote, boolean escapes) {
        List<NText> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
        while (ar.hasNext()) {
            char c = ar.peekChar();
            if (isInterpolationStart(ar)) {
                flush(sb, all, NTextStyle.string());
                all.addAll(readInterpolation(ar));
                continue;
            }
            if (escapes && c == '\\') {
                flush(sb, all, NTextStyle.string());
                sb.append(ar.readChar());
                if (ar.hasNext()) {
                    sb.append(ar.readChar());
                }
                all.add(NText.ofStyled(sb.toString(), NTextStyle.separator()));
                sb.setLength(0);
                continue;
            }
            if (c == quote) {
                sb.append(ar.readChar());
                break;
            }
            if (c == '\r' || c == '\n') {
                break;
            }
            sb.append(ar.readChar());
        }
        flush(sb, all, NTextStyle.string());
        return all;
    }

    protected List<NText> readBacktickString(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
        while (ar.hasNext()) {
            char c = ar.readChar();
            sb.append(c);
            if (c == '`') {
                break;
            }
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.string()));
    }

    protected List<NText> readBacktickIdentifier(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
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

    protected List<NText> readCharLiteral(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
        if (ar.hasNext() && ar.peekChar() == '\\') {
            sb.append(ar.readChar());
            if (ar.hasNext()) {
                sb.append(ar.readChar());
            }
        } else if (ar.hasNext()) {
            sb.append(ar.readChar());
        }
        if (ar.hasNext() && ar.peekChar() == '\'') {
            sb.append(ar.readChar());
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.string()));
    }

    // -------------------------------------------------------------------------
    // Identifiers, annotations, numbers, operators
    // -------------------------------------------------------------------------

    protected List<NText> readIdentifier(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar());
        while (ar.hasNext() && isIdentifierPart(ar.peekChar())) {
            sb.append(ar.readChar());
        }
        String word = sb.toString();
        NTextStyles style = styleForWord(word);
        if (style != null) {
            return Collections.singletonList(NText.ofStyled(word, style));
        }
        return Collections.singletonList(NText.ofPlain(word));
    }

    protected List<NText> readAnnotation(StringReaderExt ar) {        StringBuilder sb = new StringBuilder();
        sb.append(ar.readChar()); // @
        while (ar.hasNext() && (isIdentifierPart(ar.peekChar()) || ar.peekChar() == '.' || ar.peekChar() == ':')) {
            sb.append(ar.readChar());
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.annotation()));
    }

    protected List<NText> readNumber(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        if (ar.peekChar() == '.') {
            sb.append(ar.readChar());
            while (ar.hasNext() && (Character.isDigit(ar.peekChar()) || ar.peekChar() == '_')) {
                sb.append(ar.readChar());
            }
            readExponent(ar, sb);
            readNumberSuffix(ar, sb);
            return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.number()));
        }
        if (ar.peekChar() == '0' && ar.hasNext(1)) {
            char next = ar.peekChar(1);
            if (next == 'x' || next == 'X') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && isHexDigitOrUnderscore(ar.peekChar())) {
                    sb.append(ar.readChar());
                }
                readNumberSuffix(ar, sb);
                return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.number()));
            }
            if (next == 'b' || next == 'B') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && (ar.peekChar() == '0' || ar.peekChar() == '1' || ar.peekChar() == '_')) {
                    sb.append(ar.readChar());
                }
                readNumberSuffix(ar, sb);
                return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.number()));
            }
            if (next == 'o' || next == 'O') {
                sb.append(ar.readChar()).append(ar.readChar());
                while (ar.hasNext() && ((ar.peekChar() >= '0' && ar.peekChar() <= '7') || ar.peekChar() == '_')) {
                    sb.append(ar.readChar());
                }
                readNumberSuffix(ar, sb);
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
        readNumberSuffix(ar, sb);
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.number()));
    }

    protected void readExponent(StringReaderExt ar, StringBuilder sb) {
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

    protected void readNumberSuffix(StringReaderExt ar, StringBuilder sb) {
        while (ar.hasNext() && numberSuffixChars.indexOf(ar.peekChar()) >= 0) {
            sb.append(ar.readChar());
        }
    }

    protected List<NText> readOperatorRun(StringReaderExt ar) {
        StringBuilder sb = new StringBuilder();
        while (ar.hasNext() && isOperatorChar(ar.peekChar())) {
            if (isLineCommentStart(ar) || isBlockCommentStart(ar)) {
                break;
            }
            sb.append(ar.readChar());
        }
        return Collections.singletonList(NText.ofStyled(sb.toString(), NTextStyle.operator()));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    protected boolean startsWith(StringReaderExt ar, String s) {
        return ar.peekChars(s.length()).equals(s);
    }

    protected boolean isHexDigitOrUnderscore(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F') || c == '_';
    }

    protected void flush(StringBuilder sb, List<NText> all, NTextStyle style) {
        if (sb.length() > 0) {
            all.add(NText.ofStyled(sb.toString(), style));
            sb.setLength(0);
        }
    }
}
