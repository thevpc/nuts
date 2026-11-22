package net.thevpc.nuts.runtime.standalone.format.tson.parser.custom;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NPositionedCharReader;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.runtime.standalone.elem.item.*;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementLineImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementLineUtils;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.*;

public class TsonCustomLexer implements NGenerator<NElementTokenImpl> {
    private final NPositionedCharReader reader;

    private final TsonStringLexer strings;
    private final TsonNumberLexer numbers;

    public TsonCustomLexer(String reader) {
        this(new StringReader(reader));
    }

    public TsonCustomLexer(Reader reader) {
        this.reader = new NPositionedCharReader(reader);
        this.strings = new TsonStringLexer(this.reader);
        this.numbers = new TsonNumberLexer(this.reader);
    }

    public List<NElementTokenImpl> all() {
        List<NElementTokenImpl> ret = new ArrayList<>();
        while (true) {
            NElementTokenImpl u = next();
            if (u == null) {
                break;
            }
            ret.add(u);
        }
        return ret;
    }

    public NElementTokenImpl continueReadBullet(NElementTokenType type, String chars, int line, int column, long pos) {
        StringBuilder image = new StringBuilder();
        int count = 0;
        while (chars.indexOf(reader.peek()) >= 0) {
            image.append((char) reader.read());
            count++;
            if (count >= 10) break; // safety
        }
        String s = image.toString();
        return new NElementTokenImpl(s, type, String.valueOf(s.charAt(0)), count, line, column, pos, s, null);
    }

    public NElementTokenImpl next() {
        StringBuilder currentError = new StringBuilder();
        int errline = 0;
        int errcolumn = 0;
        long errpos = 0;
        while (true) {
            int c = reader.peek();
            if (c == -1) {
                if (currentError.length() > 0) {
                    return new NElementTokenImpl(
                            currentError.toString(),
                            NElementTokenType.UNKNOWN,
                            "",
                            0,
                            errline, errcolumn, errpos,
                            currentError.length(), NMsg.ofC("unexpected token")
                    );
                }
                return null;
            }
            int line = reader.line();
            int column = reader.column();
            long pos = reader.pos();
            switch (c) {
                case '{':
                    return asChar(c, NElementTokenType.LBRACE);
                case '}':
                    return asChar(c, NElementTokenType.RBRACE);
                case '[': {
                    NElementTokenImpl b = asBullet(line, column, pos);
                    if (b != null) {
                        return b;
                    }
                    return asChar(c, NElementTokenType.LBRACK);
                }
                case ']':
                    return asChar(c, NElementTokenType.RBRACK);
                case '(':
                    return asChar(c, NElementTokenType.LPAREN);
                case ')':
                    return asChar(c, NElementTokenType.RPAREN);
                case '#':
                    return asOperator123(c, NOperatorSymbol.HASH, NOperatorSymbol.HASH2, NOperatorSymbol.HASH3);
                case '.':
                    return asOperator123(c, NOperatorSymbol.DOT, NOperatorSymbol.DOT2, NOperatorSymbol.DOT3);
                case '●':
                case '•': {
                    return continueReadBullet(NElementTokenType.UNORDERED_LIST, "●•", line, column, pos);
                }
                case '■':
                case '▪': {
                    return continueReadBullet(NElementTokenType.ORDERED_LIST, "■▪", line, column, pos);
                }
                case '¶': {
                    int c2 = reader.peekAt(1);
                    if (c2 == '¶') {
                        return readBlockString();
                    }
                    return readLineString();
                }
                case ';': {
                    int c2 = reader.peek();
                    if (c2 == ';') {
                        reader.read(2);
                        return new NElementTokenImpl(";;", NElementTokenType.SEMICOLON2, ";;", 0, line, column, pos, c, null);
                    }
                    return asChar(c, NElementTokenType.SEMICOLON);
                }
                case ',':
                    return asChar(c, NElementTokenType.COMMA);
                case ':': {
                    int n = reader.peekAt(1);
                    switch (n) {
                        case ':': {
                            reader.read(2);
                            return new NElementTokenImpl("::", NElementTokenType.OPERATOR_SYMBOL, "::", 0, line, column, pos, NOperatorSymbol.COLON2, null);
                        }
                        case '=': {
                            n = reader.peekAt(2);
                            if (n == '=') {
                                reader.read(3);
                                return new NElementTokenImpl(":==", NElementTokenType.OPERATOR_SYMBOL, ":==", 0, line, column, pos, NOperatorSymbol.COLON_EQ2, null);
                            }
                            reader.read(2);
                            return new NElementTokenImpl(":=", NElementTokenType.OPERATOR_SYMBOL, ":=", 0, line, column, pos, NOperatorSymbol.COLON_EQ, null);
                        }
                        default: {
                            return asChar(c, NElementTokenType.COLON);
                        }
                    }
                }
                case '/': {
                    int n = reader.peekAt(1);
                    switch (n) {
                        case '/': {
                            return readLineComments();
                        }
                        case '*': {
                            return readBlockComments();
                        }
                        default: {
                            return asChar(c, NOperatorSymbol.DIV);
                        }
                    }
                }
                case '\\': {
                    return asOperator123(c, NOperatorSymbol.BACKSLASH, NOperatorSymbol.BACKSLASH2, NOperatorSymbol.BACKSLASH3);
                }
                case '?':
                    return asOperator123(c, NOperatorSymbol.INTERROGATION, NOperatorSymbol.INTERROGATION2, NOperatorSymbol.INTERROGATION3);
                case '!': {
                    return asOperator123Eq(c, NOperatorSymbol.NOT, NOperatorSymbol.NOT2, NOperatorSymbol.NOT3, NOperatorSymbol.NOT_EQ, NOperatorSymbol.NOT_EQ2);
                }
                case '~': {
                    return asOperator123Eq(c, NOperatorSymbol.TILDE, NOperatorSymbol.TILDE2, NOperatorSymbol.TILDE3, NOperatorSymbol.TILDE_EQ, NOperatorSymbol.TILDE_EQ2);
                }
                case '%':
                    return asOperator123(c, NOperatorSymbol.REM, NOperatorSymbol.REM2, NOperatorSymbol.REM3);
                case '&':
                    return asOperator123(c, NOperatorSymbol.AND, NOperatorSymbol.AND2, NOperatorSymbol.AND3);
                case '|':
                    // | || |||  (previously split into single '|' tokens)
                    return asOperator123(c, NOperatorSymbol.PIPE, NOperatorSymbol.PIPE2, NOperatorSymbol.PIPE3);
                case '^': {
                    return continueReadStream(c, line, column, pos);
                }
                case '@': {
                    int n = reader.peekAt(1);
                    if (n == c) {
                        int n2 = reader.peekAt(2);
                        if (n2 == c) {
                            reader.read(3);
                            String i = NOperatorSymbol.AT3.lexeme();
                            return new NElementTokenImpl(i, NElementTokenType.OPERATOR_SYMBOL, i, 0, line, column, pos, NOperatorSymbol.AT3, null);
                        } else {
                            reader.read(2);
                            String i = NOperatorSymbol.AT2.lexeme();
                            return new NElementTokenImpl(i, NElementTokenType.OPERATOR_SYMBOL, i, 0, line, column, pos, NOperatorSymbol.AT2, null);
                        }
                    }
                    return asChar(c, NElementTokenType.AT);
                }
                case '<': {
                    int c1 = reader.peekAt(1);
                    if (c1 == '<') {
                        if (reader.peekAt(2) == '<') {
                            reader.read(3);
                            return new NElementTokenImpl("<<<", NElementTokenType.OPERATOR_SYMBOL, "<<<", 0, line, column, pos, NOperatorSymbol.LT3, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl("<<", NElementTokenType.OPERATOR_SYMBOL, "<<", 0, line, column, pos, NOperatorSymbol.LT2, null);
                    } else if (c1 == '>') {
                        reader.read(2);
                        return new NElementTokenImpl("<>", NElementTokenType.OPERATOR_SYMBOL, "<>", 0, line, column, pos, NOperatorSymbol.LT_GT, null);
                    } else if (c1 == '=') {
                        int c2 = reader.peekAt(2);
                        if (c2 == '=') {
                            reader.read(3);
                            return new NElementTokenImpl("<==", NElementTokenType.OPERATOR_SYMBOL, "<==", 0, line, column, pos, NOperatorSymbol.LT_EQ2, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl("<=", NElementTokenType.OPERATOR_SYMBOL, "<=", 0, line, column, pos, NOperatorSymbol.LTE, null);
                    } else if (c1 == '-') {
                        int c2 = reader.peekAt(2);
                        if (c2 == '-') {
                            reader.read(3); // <--x
                            return new NElementTokenImpl("<--", NElementTokenType.OPERATOR_SYMBOL, "<--", 0, line, column, pos, NOperatorSymbol.LT_MINUS2, null);
                        } else if (Character.isDigit(c2)) {
                            //<-3
                            reader.read(); // <--x
                            return new NElementTokenImpl("<", NElementTokenType.OPERATOR_SYMBOL, "<", 0, line, column, pos, NOperatorSymbol.LT, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl("<-", NElementTokenType.OPERATOR_SYMBOL, "<-", 0, line, column, pos, NOperatorSymbol.LT, null);
                    }
                    reader.read();
                    return new NElementTokenImpl("<", NElementTokenType.OPERATOR_SYMBOL, "<", 0, line, column, pos, NOperatorSymbol.LT, null);
                }

                case '-': {
                    int c1 = reader.peekAt(1);
                    if (c1 == ':') {
                        String s = reader.read(2);
                        return new NElementTokenImpl(s, NElementTokenType.OPERATOR_SYMBOL, s, 0, line, column, pos, NOperatorSymbol.MINUS_COLON, null);
                    }
                    if (c1 == '>') {
                        // ->  (e.g. ranges [1 -> 5]); previously split into '-' '>'
                        String s = reader.read(2);
                        return new NElementTokenImpl(s, NElementTokenType.OPERATOR_SYMBOL, s, 0, line, column, pos, NOperatorSymbol.MINUS_GT, null);
                    }
                    if (c1 == '-' && reader.peekAt(2) == '>') {
                        // -->
                        String s = reader.read(3);
                        return new NElementTokenImpl(s, NElementTokenType.OPERATOR_SYMBOL, s, 0, line, column, pos, NOperatorSymbol.MINUS2_GT, null);
                    }
                    if (Character.isDigit(c1)) {
                        return continueReadNumber();
                    }
                    return asOperator123Eq(c, NOperatorSymbol.MINUS, NOperatorSymbol.MINUS2, NOperatorSymbol.MINUS3, NOperatorSymbol.MINUS_EQ, NOperatorSymbol.MINUS_EQ2);
                }
                case '+': {
                    int c1 = reader.peekAt(1);
                    if (Character.isDigit(c1)) {
                        return continueReadNumber();
                    }
                    return asOperator123Eq(c, NOperatorSymbol.PLUS, NOperatorSymbol.PLUS2, NOperatorSymbol.PLUS3, NOperatorSymbol.PLUS_EQ, NOperatorSymbol.PLUS_EQ2);
                }

                case '*': {
                    return asOperator123Eq(c, NOperatorSymbol.MUL, NOperatorSymbol.MUL2, NOperatorSymbol.MUL3, NOperatorSymbol.MUL_EQ, NOperatorSymbol.MUL_EQ2);
                }

                case '=': {
                    int c1 = reader.peekAt(1);
                    if (c1 == '=') {
                        int c2 = reader.peekAt(2);
                        if (c2 == '=') {
                            reader.read(3);
                            return new NElementTokenImpl("===", NElementTokenType.OPERATOR_SYMBOL, "===", 0, line, column, pos, NOperatorSymbol.EQ3, null);
                        } else if (c2 == '>') {
                            reader.read(3);
                            return new NElementTokenImpl("==>", NElementTokenType.OPERATOR_SYMBOL, "==>", 0, line, column, pos, NOperatorSymbol.EQ2_GT, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl("==", NElementTokenType.OPERATOR_SYMBOL, "==", 0, line, column, pos, NOperatorSymbol.EQ2, null);
                    } else if (c1 == '>') {
                        reader.read(2);
                        return new NElementTokenImpl("=>", NElementTokenType.OPERATOR_SYMBOL, "=>", 0, line, column, pos, NOperatorSymbol.EQ_GT, null);
                    }
                    reader.read();
                    return new NElementTokenImpl("=", NElementTokenType.OPERATOR_SYMBOL, "=", 0, line, column, pos, NOperatorSymbol.EQ, null);
                }
                case '>': {
                    int c1 = reader.peekAt(1);
                    if (c1 == '>') {
                        if (reader.peekAt(2) == '>') {
                            reader.read(3);
                            return new NElementTokenImpl(">>>", NElementTokenType.OPERATOR_SYMBOL, ">>>", 0, line, column, pos, NOperatorSymbol.GT3, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl(">>", NElementTokenType.OPERATOR_SYMBOL, ">>", 0, line, column, pos, NOperatorSymbol.GT2, null);
                    } else if (c1 == '=') {
                        reader.read(2);
                        return new NElementTokenImpl(">=", NElementTokenType.OPERATOR_SYMBOL, ">=", 0, line, column, pos, NOperatorSymbol.GTE, null);
                    }
                    reader.read();
                    return new NElementTokenImpl(">", NElementTokenType.OPERATOR_SYMBOL, ">", 0, line, column, pos, NOperatorSymbol.GT, null);
                }
                case '"':
                case '\'':
                case '`': {
                    return readFencedString(c, line, column, pos);
                }
                case ' ': {
                    StringBuilder sb = new StringBuilder();
                    while (reader.peek() == ' ') {
                        sb.append((char) reader.read());
                    }
                    return new NElementTokenImpl(sb.toString(), NElementTokenType.SPACE, " ", 0, line, column, pos, c, null);
                }
                case '\t': {
                    StringBuilder sb = new StringBuilder();
                    while (reader.peek() == '\t') {
                        sb.append((char) reader.read());
                    }
                    return new NElementTokenImpl(sb.toString(), NElementTokenType.SPACE, "\t", 0, line, column, pos, c, null);
                }
                case '\n': {
                    reader.read();
                    return new NElementTokenImpl("\n", NElementTokenType.NEWLINE, "\n", 0, line, column, pos, c, null);
                }
                case '\r': {
                    reader.read();
                    if (reader.peek() == '\n') {
                        reader.read();
                        return new NElementTokenImpl("\r\n", NElementTokenType.NEWLINE, "\r\n", 0, line, column, pos, c, null);
                    }
                    return new NElementTokenImpl("\r", NElementTokenType.NEWLINE, "\r", 0, line, column, pos, c, null);
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    return continueReadTemporalOrNumber();
                }
                case '_':
                case '$': {
                    return continueReadIdentifier();
                }
                default: {
                    if (Character.isWhitespace(c)) {
                        StringBuilder sb = new StringBuilder();
                        char c0 = (char) reader.read();
                        sb.append(c0);
                        while (reader.canRead()) {
                            int peek = reader.peek();
                            if (peek == '\n' || peek == '\r' || peek == '\t' || peek == ' ' || !Character.isWhitespace(c)) {
                                break;
                            }
                            sb.append((char) reader.read());
                        }
                        return new NElementTokenImpl(sb.toString(), NElementTokenType.SPACE, String.valueOf(c0), 0, line, column, pos, c, null);
                    }
                    if (Character.isUnicodeIdentifierStart(c)) {
                        return continueReadIdentifier();
                    }
                    // c is only peeked here (not consumed yet).
                    int codePoint = c;
                    // Handle Surrogate Pairs to support 4-byte Unicode
                    if (Character.isHighSurrogate((char) c)) {
                        int next = reader.peekAt(1);
                        if (next >= 0 && Character.isLowSurrogate((char) next)) {
                            // Combine them into a single 32-bit Code Point
                            codePoint = Character.toCodePoint((char) c, (char) next);
                        }
                    }

                    // Convert the full Code Point to a String for mapping
                    // (never String.valueOf(int): that yields "124" instead of "|")
                    String symbolStr = new String(Character.toChars(codePoint));

                    NOptional<NOperatorSymbol> cc = NOperatorSymbol.parse(symbolStr);
                    if (cc.isPresent()) {
                        reader.read(symbolStr.length());
                        return new NElementTokenImpl(symbolStr, NElementTokenType.OPERATOR_SYMBOL, symbolStr, 0, line, column, pos,
                                cc.get(), null
                        );
                    }
                    if (codePoint < 128) {
                        break;
                    }
                    int type = Character.getType(codePoint);
                    if (type == Character.MATH_SYMBOL ||
                            type == Character.OTHER_SYMBOL ||
                            type == Character.DASH_PUNCTUATION ||
                            type == Character.MODIFIER_SYMBOL) {
                        reader.read(symbolStr.length());
                        return new NElementTokenImpl(symbolStr, NElementTokenType.OPERATOR_SYMBOL, symbolStr, 0, line, column, pos,
                                NOperatorSymbol.UNKNOWN, null
                        );
                    }
                    break;
                }
            }
            if (currentError.length() == 0) {
                errline = reader.line();
                errcolumn = reader.column();
                errpos = reader.pos();
            }
            char c0 = (char) reader.read();
            currentError.append(c0);
        }
    }


    private NElementType readBinaryBlockStart(StringBuilder fillInto, StringBuilder image) {
        int p0 = reader.peekAt(0);
        if (p0 != '^') {
            return null;
        }
        int p1 = reader.peekAt(1);
        if (p1 == '[') {
            String s = reader.read(2);
            image.append(s);
            return NElementType.BINARY_STREAM;
        }
        if (p1 == '{') {
            String s = reader.read(2);
            image.append(s);
            return NElementType.CHAR_STREAM;
        }
        if (Character.isLetter(p1)) {
            int max = 1024;
            int i = 2;
            while (i < max) {
                int c = reader.peekAt(i);
                if (c == '[') {
                    String s = reader.read(i + 1);
                    fillInto.append(s, 1, s.length() - 1);
                    image.append(s);
                    return NElementType.BINARY_STREAM;
                } else if (c == '{') {
                    String s = reader.read(i + 1);
                    fillInto.append(s, 1, s.length() - 1);
                    image.append(s);
                    return NElementType.CHAR_STREAM;
                } else if (Character.isLetterOrDigit(c)) {
                    int c2 = reader.peekAt(i + 1);
                    if (c2 == '[') {
                        String s = reader.read(i + 2);
                        fillInto.append(s, 1, s.length() - 1);
                        image.append(s);
                        return NElementType.BINARY_STREAM;
                    }
                    if (c2 == '{') {
                        String s = reader.read(i + 2);
                        fillInto.append(s, 1, s.length() - 1);
                        image.append(s);
                        return NElementType.CHAR_STREAM;
                    }
                    i++;
                } else {
                    return null;
                }
            }
        }
        return null;
    }

    private NElementTokenImpl continueReadStream(int c, int line, int column, long pos) {
        StringBuilder n = new StringBuilder();
        StringBuilder img = new StringBuilder();
        NElementType r = readBinaryBlockStart(n, img);
        if (r == NElementType.BINARY_STREAM) {
            StringBuilder content = new StringBuilder();
            StringBuilder image = new StringBuilder(img.toString());
            while (true) {
                int next = reader.read();
                if (next == -1) break;
                if (next == ']') {
                    image.append(']');
                    break;
                }
                content.append((char) next);
                image.append((char) next);
            }
            byte[] data = Base64.getDecoder().decode(content.toString().replaceAll("\\s", ""));
            NInputStreamProvider provider = () -> new ByteArrayInputStream(data);
            return new NElementTokenImpl(image.toString(), NElementTokenType.BINARY_STREAM, "stream-binary", 0, line, column, pos,
                    NElement.ofBinaryStream(provider), null);
        } else if (r == NElementType.CHAR_STREAM) {
            StringBuilder content = new StringBuilder();
            String endMarker = "^" + n + "}";
            StringBuilder image = new StringBuilder(img.toString());
            while (true) {
                if (reader.peek(endMarker)) {
                    reader.read(endMarker.length());
                    image.append(endMarker);
                    break;
                }
                int next = reader.read();
                if (next == -1) break;
                content.append((char) next);
                image.append((char) next);
            }
            NReaderProvider provider = () -> new StringReader(content.toString());
            return new NElementTokenImpl(image.toString(), NElementTokenType.CHAR_STREAM, "stream-text", 0, line, column, pos,
                    NElement.ofCharStream(provider)
                    , null);
        }
        int p1 = reader.peekAt(1);
        if (p1 == '^') {
            int p2 = reader.peekAt(2);
            if (p2 == '^') {
                String op = reader.read(3);
                return new NElementTokenImpl(op, NElementTokenType.OPERATOR_SYMBOL, op, 0, line, column, pos, NOperatorSymbol.HAT3, null);
            }
            String op = reader.read(2);
            return new NElementTokenImpl(op, NElementTokenType.OPERATOR_SYMBOL, op, 0, line, column, pos, NOperatorSymbol.HAT2, null);
        } else if (p1 == '=') {
            int p2 = reader.peekAt(2);
            if (p2 == '=') {
                String op = reader.read(3);
                return new NElementTokenImpl(op, NElementTokenType.OPERATOR_SYMBOL, op, 0, line, column, pos, NOperatorSymbol.HAT_EQ2, null);
            }
            String op = reader.read(2);
            return new NElementTokenImpl(op, NElementTokenType.OPERATOR_SYMBOL, op, 0, line, column, pos, NOperatorSymbol.HAT_EQ, null);
        }
        String op = reader.read(1);
        return new NElementTokenImpl(op, NElementTokenType.OPERATOR_SYMBOL, op, 0, line, column, pos, NOperatorSymbol.HAT, null);
    }

    private NElementTokenImpl readLineComments() {
        return readMultiLineString("//", NElementTokenType.LINE_COMMENT);
    }

    private NElementTokenImpl readBlockComments() {
        int line0 = reader.line();
        int column0 = reader.column();
        long pos0 = reader.pos();

        StringBuilder image = new StringBuilder("/*");
        List<NElementLine> lines = new ArrayList<>();

        reader.read(2); // consume /*

        boolean firstLine = true;
        NMsg error = null;

        while (true) {
            // 1. Identify leading whitespace (Potential Prefix)
            String currentPrefix = readSpaces();

            // 2. Identify a potential Marker (like '*' in Javadoc)
            String currentMarker = "";
            String currentPadding = "";

            if (reader.peek() == '*' && reader.peekAt(1) != '/') {
                currentMarker = String.valueOf((char) reader.read()); // consume '*'
                currentPadding = readSpaces(); // consume spaces after '*'
            }

            // 3. Read the actual content until EOL or */
            StringBuilder contentBuf = new StringBuilder();
            while (true) {
                int c = reader.peek();
                if (c == -1 || (c == '*' && reader.peekAt(1) == '/')) break;

                NNewLineMode nl = reader.readNewLine();
                if (nl != null) {
                    // End of this line
                    lines.add(new NElementLineImpl(
                            currentPrefix, currentMarker, currentPadding, contentBuf.toString(), "", "", nl
                    ));
                    // Update image (reconstructing the source exactly)
                    image.append(currentPrefix).append(currentMarker).append(currentPadding)
                            .append(contentBuf).append(nl.value());
                    break;
                }
                contentBuf.append((char) reader.read());
            }

            // Check if we exited because of the closing tag */
            if (reader.peek() == '*' && reader.peekAt(1) == '/') {
                lines.add(new NElementLineImpl(
                        currentPrefix, currentMarker, currentPadding, contentBuf.toString(), "", "", null
                ));
                image.append(currentPrefix).append(currentMarker).append(currentPadding)
                        .append(contentBuf).append("*/");
                reader.read(2); // consume */
                break;
            }

            if (reader.peek() == -1) {
                error = NMsg.ofC("Unclosed block comment");
                break;
            }
        }

        return new NElementTokenImpl(
                image.toString(), NElementTokenType.BLOCK_COMMENT, "/*", 0,
                line0, column0, pos0,
                new LinesAndContent(lines),
                error
        );
    }



    private String readSpaces() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int c = reader.peek();
            if (c < 0) {
                break;
            }
            if (c == ' ' || c == '\t') {
                sb.append(reader.readChar());
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private NElementTokenImpl asOperator123Eq(int c, NOperatorSymbol t1, NOperatorSymbol t2, NOperatorSymbol t3, NOperatorSymbol eq1, NOperatorSymbol eq2) {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        int n = reader.peekAt(1);
        if (n == c) {
            int n2 = reader.peekAt(2);
            if (n2 == c) {
                reader.read(3);
                String i = t3.lexeme();
                return new NElementTokenImpl(i, NElementTokenType.OPERATOR_SYMBOL, i, 0, line, column, pos, t3, null);
            } else {
                reader.read(2);
                String i = t2.lexeme();
                return new NElementTokenImpl(i, NElementTokenType.OPERATOR_SYMBOL, i, 0, line, column, pos, t2, null);
            }
        } else if (n == '=') {
            int n2 = reader.peekAt(2);
            if (n2 == '=') {
                reader.read(3);
                String i = eq2.lexeme();
                return new NElementTokenImpl(i, NElementTokenType.OPERATOR_SYMBOL, i, 0, line, column, pos, eq2, null);
            } else {
                reader.read(2);
                String i = eq1.lexeme();
                return new NElementTokenImpl(i, NElementTokenType.OPERATOR_SYMBOL, i, 0, line, column, pos, eq1, null);
            }
        } else {
            return asChar(c, t1);
        }
    }

    private NElementTokenImpl asOperator123(int c, NOperatorSymbol t1, NOperatorSymbol t2, NOperatorSymbol t3) {
        int n = reader.peekAt(1);
        if (n == c) {
            int line = reader.line();
            int column = reader.column();
            long pos = reader.pos();
            int n2 = reader.peekAt(2);
            if (n2 == c) {
                reader.read(3);
                String i = t3.lexeme();
                return new NElementTokenImpl(i, NElementTokenType.OPERATOR_SYMBOL, i, 0, line, column, pos, t3, null);
            } else {
                reader.read(2);
                String i = t2.lexeme();
                return new NElementTokenImpl(i, NElementTokenType.OPERATOR_SYMBOL, i, 0, line, column, pos, t2, null);
            }
        }
        return asChar(c, t1);
    }

    /**
     * Reads a fenced (quoted) string whose opening fence is the maximal run of
     * quote characters at the current position.
     * <ul>
     * <li>run of 1 → N=1 (simple quoted string)</li>
     * <li>run of exactly 2 → empty string (open + immediately closed, both
     * quotes consumed). N=2 is not a valid fence length.</li>
     * <li>run of 3+ → N = run length (arbitrary fenced string)</li>
     * </ul>
     */


    public NElementTokenImpl readFencedString(int c, int line, int column, long pos) {
        return strings.readFencedString(c, line, column, pos);
    }

    public NElementTokenImpl readFenced(char c0, int N, NElementTokenType tokenType, NElementType elementType, int line, int column, long pos) {
        return strings.readFenced(c0, N, tokenType, elementType, line, column, pos);
    }

    public NElementTokenImpl readQuoted(char c0, NElementTokenType elementTokenType, NElementType elementType) {
        return strings.readQuoted(c0, elementTokenType, elementType);
    }

    public NElementTokenImpl readTripleQuoted(char c0, NElementTokenType tokenType, NElementType elementType) {
        return strings.readTripleQuoted(c0, tokenType, elementType);
    }

    public static String toFencedString(String value, char c, int N, boolean unterminated) {
        return TsonStringLexer.toFencedString(value, c, N, unterminated);
    }

    public static String encodeStringContent(String value, char c, int N) {
        return TsonStringLexer.encodeStringContent(value, c, N);
    }

    public NElementTokenImpl readLineString() {
        return strings.readLineString();
    }

    public NElementTokenImpl readBlockString() {
        return strings.readBlockString();
    }

    public NElementTokenImpl readMultiLineString(String marker, NElementTokenType tokenType) {
        return strings.readMultiLineString(marker, tokenType);
    }

    private NElementTokenImpl continueReadNumber() {
        return numbers.continueReadNumber();
    }

    private NElementTokenImpl continueReadTemporalOrNumber() {
        return numbers.continueReadTemporalOrNumber();
    }
    public static class LinesAndContent {
        public List<NElementLine> lines;
        public String content;

        public LinesAndContent(List<NElementLine> lines) {
            this.lines = NElementLineUtils.normalizeElementLines(lines);
            this.content = NElementLineUtils.extractPureContent(this.lines);
        }
    }

    private NElementTokenImpl continueReadIdentifier() {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        StringBuilder image = new StringBuilder();
        while (true) {
            int c = reader.peek();
            if (c == -1) {
                break;
            }
            if (Character.isUnicodeIdentifierPart(c) || c == '$') {
                image.append((char) reader.read());
            } else if (c == '.') {
                if (image.length() > 0 && image.charAt(image.length() - 1) != '.') {
                    int next = reader.peekAt(1);
                    if (Character.isUnicodeIdentifierPart(next) || next == '$') {
                        image.append((char) reader.read());
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else if (c == '-') {
                if (image.length() > 0 && image.charAt(image.length() - 1) != '-') {
                    int next = reader.peekAt(1);
                    if (Character.isUnicodeIdentifierPart(next)) {
                        image.append((char) reader.read());
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        String s = image.toString();
        NElementTokenType type = NElementTokenType.NAME;
        Object value = s;
        switch (s) {
            case "true":
                type = NElementTokenType.TRUE;
                value = NElement.ofTrue();
                break;
            case "false":
                type = NElementTokenType.FALSE;
                value = NElement.ofFalse();
                break;
            case "null":
                type = NElementTokenType.NULL;
                value = NElement.ofNull();
                break;
        }
        return new NElementTokenImpl(s, type, "", 0, line, column, pos, value, null);
    }

    private NElementTokenImpl asBullet(int line, int column, long pos) {
        if (reader.peekAt(0) != '[') {
            return null;
        }
        int c1 = reader.peekAt(1);
        NElementTokenType type;
        switch (c1) {
            case '●':
            case '•':
            case '.': {
                type = NElementTokenType.UNORDERED_LIST;
                break;
            }
            case '■':
            case '▪':
            case '#': {
                type = NElementTokenType.ORDERED_LIST;
                break;
            }
            default: {
                return null;
            }
        }
        int index = 2;
        while (true) {
            int c2 = reader.peekAt(index);
            if (c2 == c1) {
                index++;
            } else if (c2 == ']') {
                String image = reader.read(index + 1);
                return new NElementTokenImpl(
                        image,
                        type,
                        "[" + ((char) c1) + "]",
                        image.length() - 2,
                        line, column, pos,
                        image,
                        null
                );
            } else {
                return null;
            }
        }
    }

    private NElementTokenImpl asChar(int c, NElementTokenType tt) {
        String image = String.valueOf((char) c);
        reader.read();
        return new NElementTokenImpl(image, tt, image, 0, reader.line(), reader.column(), reader.pos(), c, null);
    }

    private NElementTokenImpl asChar(int c, NOperatorSymbol tt) {
        String image = String.valueOf((char) c);
        reader.read();
        return new NElementTokenImpl(image, NElementTokenType.OPERATOR_SYMBOL, image, 0, reader.line(), reader.column(), reader.pos(), tt, null);
    }
}
