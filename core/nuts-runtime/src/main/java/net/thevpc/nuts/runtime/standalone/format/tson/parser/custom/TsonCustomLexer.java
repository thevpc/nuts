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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NPositionedCharReader;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.runtime.standalone.elem.item.*;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

public class TsonCustomLexer implements NGenerator<NElementTokenImpl> {
    private NPositionedCharReader reader;

    public TsonCustomLexer(String reader) {
        this(new StringReader(reader));
    }

    public TsonCustomLexer(Reader reader) {
        this.reader = new NPositionedCharReader(reader);
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

    public NElementTokenImpl continueReadBullet(NElementTokenType type, String chars,int line, int column, long pos) {
        StringBuilder image = new StringBuilder();
        int count = 0;
        while (chars.indexOf(reader.peek())>=0) {
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
                case '•':
                {
                    return continueReadBullet(NElementTokenType.UNORDERED_LIST,"●•", line, column, pos);
                }
                case '■':
                case '▪':
                {
                    return continueReadBullet(NElementTokenType.ORDERED_LIST, "■▪",line, column, pos);
                }
                case '¶': {
                    int c2 = reader.peekAt(1);
                    if (c2 == '¶') {
                        return continueReadUserMultiLine();
                    }
                    return continueReadUserSingleLine();
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
                            return new NElementTokenImpl("::", NElementTokenType.OP, "::", 0, line, column, pos, NOperatorSymbol.COLON2, null);
                        }
                        case '=': {
                            n = reader.peekAt(2);
                            if (n == '=') {
                                reader.read(3);
                                return new NElementTokenImpl(":==", NElementTokenType.OP, ":==", 0, line, column, pos, NOperatorSymbol.COLON_EQ2, null);
                            }
                            reader.read(2);
                            return new NElementTokenImpl(":=", NElementTokenType.OP, ":=", 0, line, column, pos, NOperatorSymbol.COLON_EQ, null);
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
                            return continueReadLineComments();
                        }
                        case '*': {
                            return continueReadBlockComments();
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
                case '^': {
                    return continueReadStream(c, line, column, pos);
                }
                case '@': {
                    int n = reader.peekAt(1);
                    if (n == c) {
                        int n2 = reader.peekAt(2);
                        char cc = (char) c;
                        if (n2 == c) {
                            reader.read(3);
                            String i = new String(new char[]{cc, cc, cc});
                            return new NElementTokenImpl(i, NElementTokenType.OP, i, 0, line, column, pos, NOperatorSymbol.AT3, null);
                        } else {
                            reader.read(2);
                            String i = new String(new char[]{cc, cc});
                            return new NElementTokenImpl(i, NElementTokenType.OP, i, 0, line, column, pos, NOperatorSymbol.AT2, null);
                        }
                    }
                    return asChar(c, NElementTokenType.AT);
                }
                case '<': {
                    int c1 = reader.peekAt(1);
                    if (c1 == '<') {
                        if (reader.peekAt(2) == '<') {
                            reader.read(3);
                            return new NElementTokenImpl("<<<", NElementTokenType.OP, "<<<", 0, line, column, pos, NOperatorSymbol.LT3, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl("<<", NElementTokenType.OP, "<<", 0, line, column, pos, NOperatorSymbol.LT2, null);
                    } else if (c1 == '>') {
                        reader.read(2);
                        return new NElementTokenImpl("<>", NElementTokenType.OP, "<>", 0, line, column, pos, NOperatorSymbol.LT_GT, null);
                    } else if (c1 == '=') {
                        int c2 = reader.peekAt(2);
                        if (c2 == '=') {
                            reader.read(3);
                            return new NElementTokenImpl("<==", NElementTokenType.OP, "<<<", 0, line, column, pos, NOperatorSymbol.LT_EQ2, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl("<=", NElementTokenType.OP, "<<", 0, line, column, pos, NOperatorSymbol.LTE, null);
                    } else if (c1 == '-') {
                        int c2 = reader.peekAt(2);
                        if (c2 == '-') {
                            reader.read(3); // <--x
                            return new NElementTokenImpl("<--", NElementTokenType.OP, "<<<", 0, line, column, pos, NOperatorSymbol.LT_MINUS2, null);
                        } else if (Character.isDigit(c2)) {
                            //<-3
                            reader.read(); // <--x
                            return new NElementTokenImpl("<", NElementTokenType.OP, "<", 0, line, column, pos, NOperatorSymbol.LT, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl("<=", NElementTokenType.OP, "<<", 0, line, column, pos, NOperatorSymbol.LT2, null);
                    }
                    reader.read();
                    return new NElementTokenImpl("<", NElementTokenType.OP, "&", 0, line, column, pos, NOperatorSymbol.LT, null);
                }

                case '-': {
                    int c1 = reader.peekAt(1);
                    if (c1==':') {
                        String s = reader.read(2);
                        return new NElementTokenImpl(s, NElementTokenType.OP,s, 0, line, column, pos, NOperatorSymbol.MINUS_COLON, null);
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
                            return new NElementTokenImpl("===", NElementTokenType.OP, "===", 0, line, column, pos, NOperatorSymbol.EQ3, null);
                        } else if (c2 == '>') {
                            reader.read(3);
                            return new NElementTokenImpl("==>", NElementTokenType.OP, "==>", 0, line, column, pos, NOperatorSymbol.EQ2_GT, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl("==", NElementTokenType.OP, "==", 0, line, column, pos, NOperatorSymbol.EQ2, null);
                    } else if (c1 == '>') {
                        reader.read(2);
                        return new NElementTokenImpl("=>", NElementTokenType.OP, "=>", 0, line, column, pos, NOperatorSymbol.EQ_GT, null);
                    }
                    reader.read();
                    return new NElementTokenImpl("=", NElementTokenType.OP, "=", 0, line, column, pos, NOperatorSymbol.EQ, null);
                }
                case '>': {
                    int c1 = reader.peekAt(1);
                    if (c1 == '>') {
                        if (reader.peekAt(2) == '>') {
                            reader.read(3);
                            return new NElementTokenImpl(">>>", NElementTokenType.OP, ">>>", 0, line, column, pos, NOperatorSymbol.GT3, null);
                        }
                        reader.read(2);
                        return new NElementTokenImpl(">>", NElementTokenType.OP, ">>", 0, line, column, pos, NOperatorSymbol.GT2, null);
                    } else if (c1 == '=') {
                        reader.read(2);
                        return new NElementTokenImpl(">=", NElementTokenType.OP, ">=", 0, line, column, pos, NOperatorSymbol.GTE, null);
                    }
                    reader.read();
                    return new NElementTokenImpl(">", NElementTokenType.OP, ">", 0, line, column, pos, NOperatorSymbol.GT, null);
                }
                case '"': {
                    if (reader.canRead(2) && reader.peekAt(1) == c && reader.peekAt(2) == c) {
                        reader.read(); // consume first "
                        reader.read(); // second
                        reader.read(); // third
                        return continueReadQ3('"', NElementTokenType.TRIPLE_DOUBLE_QUOTED_STRING, NElementType.TRIPLE_DOUBLE_QUOTED_STRING);
                    }
                    reader.read(); // third
                    return continueReadQ1('\"', NElementTokenType.DOUBLE_QUOTED_STRING, NElementType.DOUBLE_QUOTED_STRING);
                }
                case '\'': {
                    if (reader.canRead(2) && reader.peekAt(1) == c && reader.peekAt(2) == c) {
                        reader.read(); // consume first "
                        reader.read(); // second
                        reader.read(); // third
                        return continueReadQ3('\'', NElementTokenType.TRIPLE_SINGLE_QUOTED_STRING, NElementType.TRIPLE_SINGLE_QUOTED_STRING);
                    }
                    reader.read(); // third
                    return continueReadQ1('\'', NElementTokenType.SINGLE_QUOTED_STRING, NElementType.SINGLE_QUOTED_STRING);
                }
                case '`': {
                    if (reader.canRead(2) && reader.peekAt(1) == c && reader.peekAt(2) == c) {
                        reader.read(); // consume first "
                        reader.read(); // second
                        reader.read(); // third
                        return continueReadQ3('`', NElementTokenType.TRIPLE_BACKTICK_STRING, NElementType.TRIPLE_BACKTICK_STRING);
                    }
                    reader.read(); // third
                    return continueReadQ1('`', NElementTokenType.BACKTICK_STR, NElementType.BACKTICK_STRING);
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
                    int codePoint = c;
                    // 2. Handle Surrogate Pairs to support 4-byte Unicode
                    if (Character.isHighSurrogate((char) c) && reader.canRead()) {
                        int next = reader.peek();
                        if (Character.isLowSurrogate((char) next)) {
                            // Combine them into a single 32-bit Code Point
                            codePoint = Character.toCodePoint((char) c, (char) reader.read());
                        }
                    }

                    // Convert the full Code Point to a String for mapping
                    String symbolStr = Character.isSupplementaryCodePoint(codePoint)
                            ? new String(Character.toChars(codePoint))
                            : String.valueOf((char) codePoint);

                    NOptional<NOperatorSymbol> cc = NOperatorSymbol.parse(symbolStr);
                    if (cc.isPresent()) {
                        return new NElementTokenImpl(String.valueOf(c), NElementTokenType.OP, String.valueOf(c), 0, line, column, pos,
                                cc.get(), null
                        );
                    }
                    if (codePoint < 128) {
                        break;
                    }
                    int type = Character.getType(c);
                    if (type == Character.MATH_SYMBOL||
                            type == Character.OTHER_SYMBOL ||
                            type == Character.DASH_PUNCTUATION ||
                            type == Character.MODIFIER_SYMBOL) {
                        return new NElementTokenImpl(String.valueOf(c), NElementTokenType.OP, String.valueOf(c), 0, line, column, pos,
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
                return new NElementTokenImpl(op, NElementTokenType.OP, op, 0, line, column, pos, NOperatorSymbol.HAT3, null);
            }
            String op = reader.read(2);
            return new NElementTokenImpl(op, NElementTokenType.OP, op, 0, line, column, pos, NOperatorSymbol.HAT2, null);
        } else if (p1 == '=') {
            int p2 = reader.peekAt(2);
            if (p2 == '=') {
                String op = reader.read(3);
                return new NElementTokenImpl(op, NElementTokenType.OP, op, 0, line, column, pos, NOperatorSymbol.HAT_EQ2, null);
            }
            String op = reader.read(2);
            return new NElementTokenImpl(op, NElementTokenType.OP, op, 0, line, column, pos, NOperatorSymbol.HAT_EQ, null);
        }
        String op = reader.read(1);
        return new NElementTokenImpl(op, NElementTokenType.OP, op, 0, line, column, pos, NOperatorSymbol.HAT, null);
    }

    private boolean readLineCommentStart(StringBuilder sb) {
        while (true) {
            int c = reader.peek();
            if (c == -1) {
                break;
            } else if (c == '\n') {
                char cc = (char) reader.read();
                sb.append(cc);
                readLineCommentSuite(sb);
                return true;
            } else if (c == '\r') {
                sb.append((char) reader.read());
                c = reader.peek();
                if (c == '\n') {
                    sb.append((char) reader.read());
                }
                readLineCommentSuite(sb);
                return true;
            }
            char cc = (char) reader.read();
            sb.append(cc);
        }
        return true;
    }

    private boolean readLineCommentSuite(StringBuilder sb) {
        int i = 0;
        while (true) {
            int c = reader.peekAt(i);
            if (c == '\n') {
                return false;
            } else if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '/') {
                c = reader.peekAt(i + 1);
                if (c == '/') {
                    sb.append(reader.read(i + 1));
                    return readLineCommentStart(sb);
                }
                return false;
            }else{
                return false;
            }
        }
    }


    private NElementTokenImpl continueReadLineComments() {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        StringBuilder raw = new StringBuilder("//");
        reader.read(2); // consume //

        readLineCommentStart(raw);
        return new NElementTokenImpl(raw.toString(), NElementTokenType.LINE_COMMENT, "//", 0, line, column, pos,
                raw.toString(), null);
    }

    private NElementTokenImpl continueReadBlockComments() {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        StringBuilder image = new StringBuilder("/*");
        reader.read(2); // consume /*
        NMsg error = null;
        while (true) {
            int c = reader.peek();
            if (c == -1) {
                error = NMsg.ofC("unclosed bloc comment. missing '*/'");
                break;
            }
            if (c == '*' && reader.peekAt(1) == '/') {
                image.append("*/");
                reader.read(2);
                break;
            }
            c = (char) reader.read();
            image.append((char) c);
        }
        return new NElementTokenImpl(image.toString(), NElementTokenType.BLOCK_COMMENT, "/*", 0, line, column, pos,
                image.toString()
                , error);
    }

    private NElementTokenImpl asOperator123Eq(int c, NOperatorSymbol t1, NOperatorSymbol t2, NOperatorSymbol t3, NOperatorSymbol eq1, NOperatorSymbol eq2) {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        int n = reader.peekAt(1);
        if (n == c) {
            int n2 = reader.peekAt(2);
            char cc = (char) c;
            if (n2 == c) {
                reader.read(3);
                String i = new String(new char[]{cc, cc, cc});
                return new NElementTokenImpl(i, NElementTokenType.OP, i, 0, line, column, pos, t3, null);
            } else {
                reader.read(2);
                String i = new String(new char[]{cc, cc});
                return new NElementTokenImpl(i, NElementTokenType.OP, i, 0, line, column, pos, t2, null);
            }
        } else if (n == '=') {
            int n2 = reader.peekAt(2);
            char cc = (char) c;
            if (n2 == '=') {
                reader.read(3);
                String i = new String(new char[]{cc, cc, cc});
                return new NElementTokenImpl(i, NElementTokenType.OP, i, 0, line, column, pos, eq2, null);
            } else {
                reader.read(2);
                String i = new String(new char[]{cc, cc});
                return new NElementTokenImpl(i, NElementTokenType.OP, i, 0, line, column, pos, eq1, null);
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
            char cc = (char) c;
            if (n2 == c) {
                reader.read(3);
                String i = new String(new char[]{cc, cc, cc});
                return new NElementTokenImpl(i, NElementTokenType.OP, i, 0, line, column, pos, t3, null);
            } else {
                reader.read(2);
                String i = new String(new char[]{cc, cc});
                return new NElementTokenImpl(i, NElementTokenType.OP, i, 0, line, column, pos, t2, null);
            }
        }
        return asChar(c, t1);
    }

    private NElementTokenImpl continueReadQ1(char c0, NElementTokenType elementTokenType, NElementType elementType) {
        StringBuilder image = new StringBuilder().append(c0);
        StringBuilder value = new StringBuilder();
        NMsg error = null;
        while (true) {
            int c = reader.read();
            if (c == -1) {
                // EOF without closing "
                error = NMsg.ofC("EOF without closing %s", c0);
                break;
            }
            if (c == c0) {
                if (reader.peek() == c0) {
                    // Found closing "
                    reader.read(); // consume both
                    value.append(c0);
                    image.append(c0).append(c0);
                } else {
                    image.append(c0);
                    break;
                }
            } else {
                image.append((char) c);
                value.append((char) c);
            }
        }
        return new NElementTokenImpl(
                image.toString(),
                elementTokenType,
                String.valueOf(c0),
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        elementType,
                        value.toString(),
                        image.toString()
                ), error
        );
    }

    private NElementTokenImpl continueReadQ3(char c0, NElementTokenType tokenType, NElementType elementType) {
        StringBuilder image = new StringBuilder().append(c0).append(c0).append(c0);
        StringBuilder value = new StringBuilder();

        while (true) {
            // End delimiter """
            if (reader.canRead(3)
                    && reader.peekAt(0) == c0
                    && reader.peekAt(1) == c0
                    && reader.peekAt(2) == c0) {

                reader.read();
                reader.read();
                reader.read();
                image.append(c0).append(c0).append(c0);

                if (reader.canRead(1)
                        && reader.peek() == c0) {
                    //add the last 3 quotes
                    value.append(c0).append(c0).append(c0);
                    //drop this
                    reader.read();
                    image.append(c0);
                    //drop add the rest
                    while (reader.peek() == c0) {
                        reader.read();
                        image.append(c0);
                        value.append(c0);
                    }

                } else {
                    image.append(c0).append(c0).append(c0);
                    break;
                }
            } else {
                // Regular character (verbatim)
                int c = reader.read();
                if (c == -1) break;

                char ch = (char) c;
                image.append(ch);
                value.append(ch);
            }
        }

        return new NElementTokenImpl(
                image.toString(),
                tokenType,
                new String(new char[]{c0, c0, c0}),
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        elementType,
                        value.toString(),
                        image.toString()
                ),
                null
        );
    }

    private NElementTokenImpl continueReadUserSingleLine() {
        StringBuilder image = new StringBuilder();
        StringBuilder content = new StringBuilder();

        // Consume the initial '¶'
        image.append('¶');
        reader.read(); // consume '¶'

        // Read rest of line (up to but not including newline)
        while (true) {
            int c = reader.peek();
            if (c == -1 || c == '\n' || c == '\r') {
                break;
            }
            char ch = (char) reader.read();
            image.append(ch);
            content.append(ch);
        }

        // Consume and append newline to image (but not to content)
        int c = reader.peek();
        if (c == '\r') {
            image.append('\r');
            reader.read();
            if (reader.peek() == '\n') {
                image.append('\n');
                reader.read();
            }
        } else if (c == '\n') {
            image.append('\n');
            reader.read();
        }
        // If EOF, no newline to consume

        return new NElementTokenImpl(
                image.toString(),           // e.g., "¶ hello\n"
                NElementTokenType.LINE_STRING,
                "¶",
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        NElementType.LINE_STRING,
                        content.toString(),
                        image.toString()
                )

                , null
        );
    }

    private NElementTokenImpl continueReadUserMultiLine() {
        StringBuilder image = new StringBuilder();
        StringBuilder content = new StringBuilder();

        // Read first "¶¶"
        image.append("¶¶");
        reader.read(); // ¶
        reader.read(); // ¶

        boolean firstLine = true;

        while (true) {
            // Read rest of current line
            StringBuilder lineContent = new StringBuilder();
            while (true) {
                int c = reader.peek();
                if (c == -1 || c == '\n' || c == '\r') {
                    break;
                }
                char ch = (char) reader.read();
                image.append(ch);
                lineContent.append(ch);
            }

            // Append to content
            if (!firstLine) {
                content.append('\n');
            }
            content.append(lineContent.toString());
            firstLine = false;

            // Consume newline and add to image
            int c = reader.peek();
            if (c == '\r') {
                image.append('\r');
                reader.read();
                if (reader.peek() == '\n') {
                    image.append('\n');
                    reader.read();
                }
            } else if (c == '\n') {
                image.append('\n');
                reader.read();
            } else {
                // EOF
                break;
            }

            // Check for next ¶¶ line
            if (!reader.canRead(2)) {
                break;
            }
            if (reader.peekAt(0) != '¶' || reader.peekAt(1) != '¶') {
                break;
            }

            // Consume next ¶¶
            image.append("¶¶");
            reader.read(); // ¶
            reader.read(); // ¶

            // Optional: consume space after ¶¶
            if (reader.peek() == ' ') {
                image.append(' ');
                reader.read();
            }
        }

        return new NElementTokenImpl(
                image.toString(),           // ← exact source
                NElementTokenType.BLOCK_STRING,
                "¶¶",
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        NElementType.BLOCK_STRING,
                        content.toString(),
                        image.toString()
                )
                // ← clean value
                , null
        );
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

    private NElementTokenImpl continueReadNumber() {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        StringBuilder image = new StringBuilder();

        // 1. Handle Leading Sign (Unary)
        int first = reader.peek();
        if (first == '-' || first == '+') {
            image.append((char) reader.read());
        }

        // 2. NOW check for Special vs Normal
        int p = reader.peek();
        if (p == '0') {
            int next = reader.peekAt(1);
            ConstAndType constantName = getConstantName(1);
            if (constantName != null) {
                // image already has the sign +/-
                image.append((char) reader.read()); // '0'
                image.append(reader.read(constantName.image.length()));
                TSONNumberInfo info = new TSONNumberInfo();
                info.specialNumber = true;
                info.specialConst = true;
                // Capture Keyword (max, min, pinf, ninf, nan)
                info.baseValue = constantName.constName.toLowerCase();
                if (constantName.namedSize != null) {
                    info.unsignedNumber = constantName.namedSize.floatingNumber;
                    info.floatingNumber = constantName.namedSize.floating;
                    info.bits = constantName.namedSize.bits; // BigNum
                }
                info.suffix = consumeSuffix(image);

                info.numberLayout = NNumberLayout.DECIMAL;
                NNumberElement v = applyConstantValue(info, image);
                if (v != null) {
                    return new NElementTokenImpl(image.toString(), NElementTokenType.NUMBER,
                            image.toString(), 0, line, column, pos, v, null);
                }
                return new NElementTokenImpl(image.toString(), NElementTokenType.NUMBER,
                        image.toString(), 0, line, column, pos, new DefaultNNumberElement(
                        NElementType.INT, 0), null);
            }
            if (next == 's' || next == 'u' || next == 'f') {
                // this is zero then size : 0f32, 0s8
                NamedSize ns = getSize(1, true);
                if (ns != null) {
                    image.append((char) reader.read()); // '0'
                    image.append(reader.read(ns.image.length()));

                    TSONNumberInfo info = new TSONNumberInfo();
                    info.specialNumber = false;
                    info.specialConst = false;
                    // Capture Keyword (max, min, pinf, ninf, nan)
                    info.baseValue = "0";
                    info.unsignedNumber = ns.floatingNumber;
                    info.floatingNumber = ns.floating;
                    info.bits = ns.bits; // BigNum
                    info.suffix = consumeSuffix(image);
                    info.numberLayout = NNumberLayout.DECIMAL;

                    return applyZeroValue(info, image, line, column, pos);
                }
            }
        }
        // 3. Normal Number Path (Decimal, Hex, Complex)
        return readNormalNumber(image, line, column, pos);
    }


    private static class NamedSize {
        String image;
        int bits;
        boolean floatingNumber;
        boolean floating;

        public NamedSize(String image, int bits, boolean floatingNumber, boolean floating) {
            this.image = image;
            this.bits = bits;
            this.floatingNumber = floatingNumber;
            this.floating = floating;
        }
    }

    private static class ConstAndType {
        String image;
        String constName;
        NamedSize namedSize;

        public ConstAndType(String image, String constName, NamedSize namedSize) {
            this.image = image;
            this.constName = constName;
            this.namedSize = namedSize;
        }
    }

    private NamedSize getSize(int offset, boolean acceptBig) {
        int c0 = reader.peekAt(offset);
        int o = 0;
        StringBuilder sb = new StringBuilder();
        if (c0 == '_') {
            sb.append((char) c0);
            offset++;
            c0 = reader.peekAt(offset);
        }
        if (c0 == 'u' || c0 == 's' || c0 == 'f') {
            sb.append((char) c0);
            int c1 = reader.peekAt(offset + 1);
            sb.append((char) c1);
            int c2 = reader.peekAt(offset + 2);
            switch (c1) {
                case 'N': {
                    if (acceptBig) {
                        sb.append((char) c2);
                        return new NamedSize(
                                sb.toString(),
                                -1,
                                c0 == 'u',
                                c0 == 'f'
                        );
                    }
                    return null;
                }
                case '8': {
                    if (c2 < '0' || c2 > '9') {
                        return new NamedSize(
                                sb.toString(),
                                8,
                                c0 == 'u',
                                c0 == 'f'
                        );
                    }
                    return null;
                }
                case '1': {
                    if (c2 == '6') {
                        int c3 = reader.peekAt(offset + 3);
                        if (c3 < '0' || c3 > '9') {
                            sb.append((char) c2);
                            return new NamedSize(
                                    sb.toString(),
                                    16,
                                    c0 == 'u',
                                    c0 == 'f'
                            );
                        }
                    }
                    return null;
                }
                case '3': {
                    if (c2 == '2') {
                        int c3 = reader.peekAt(offset + 3);
                        if (c3 < '0' || c3 > '9') {
                            sb.append((char) c2);
                            return new NamedSize(
                                    sb.toString(),
                                    32,
                                    c0 == 'u',
                                    c0 == 'f'
                            );
                        }
                    }
                    return null;
                }
                case '6': {
                    if (c2 == '4') {
                        int c3 = reader.peekAt(offset + 3);
                        if (c3 < '0' || c3 > '9') {
                            sb.append((char) c2);
                            return new NamedSize(
                                    sb.toString(),
                                    64,
                                    c0 == 'u',
                                    c0 == 'f'
                            );
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }

    private ConstAndType getConstantName(int offset0) {
        int offset = offset0;
        int c0 = reader.peekAt(offset);
        if (c0 == '_') {
            offset++;
        }
        int c1 = reader.peekAt(offset + 1);
        int c2 = reader.peekAt(offset + 2);
        int c3 = reader.peekAt(offset + 3);
        switch (c0) {
            case 'm':
            case 'M': {
                if (
                        (c1 == 'a' || c1 == 'A') && (c2 == 'x' || c2 == 'X')
                                ||
                                (c1 == 'i' || c1 == 'I') && (c2 == 'n' || c2 == 'N')
                ) {
                    NamedSize nsize = getSize(offset + 3, false);
                    if (nsize != null) {
                        return new ConstAndType(
                                reader.peek(offset0, offset + 2 + nsize.image.length()),
                                new String(new char[]{(char) c0, (char) c1, (char) c2}),
                                nsize
                        );
                    }
                    if (c3 <= 0 || c3 == '_') {
                        return new ConstAndType(
                                reader.peek(offset0, offset + 2),
                                new String(new char[]{(char) c0, (char) c1, (char) c2}),
                                nsize
                        );
                    }
                    return null;
                }
                break;
            }
            case 'p':
            case 'P': {
                if ((c1 == 'i' || c1 == 'I') && (c2 == 'n' || c2 == 'N') && (c3 == 'f' || c3 == 'F')) {
                    NamedSize nsize = getSize(offset + 4, false);
                    if (nsize != null) {
                        return new ConstAndType(
                                reader.peek(offset0, offset + 3 + nsize.image.length()),
                                new String(new char[]{(char) c0, (char) c1, (char) c2, (char) c3}),
                                nsize
                        );
                    }
                    int c4 = reader.peekAt(offset + 4);
                    if (c4 <= 0 || c4 == '_') {
                        return new ConstAndType(
                                reader.peek(offset0, offset + 3),
                                new String(new char[]{(char) c0, (char) c1, (char) c2, (char) c3}),
                                nsize
                        );
                    }
                }
                break;
            }
            case 'n':
            case 'N': {
                if ((c1 == 'i' || c1 == 'I') && (c2 == 'n' || c2 == 'N') && (c3 == 'f' || c3 == 'F')) {
                    NamedSize nsize = getSize(offset + 4, false);
                    if (nsize != null) {
                        return new ConstAndType(
                                reader.peek(offset0, offset + 3 + nsize.image.length()),
                                new String(new char[]{(char) c0, (char) c1, (char) c2, (char) c3}),
                                nsize
                        );
                    }
                    int c4 = reader.peekAt(offset + 4);
                    if (c4 <= 0 || c4 == '_') {
                        return new ConstAndType(
                                reader.peek(offset0, offset + 3),
                                new String(new char[]{(char) c0, (char) c1, (char) c2, (char) c3}),
                                nsize
                        );
                    }
                }
                if ((c1 == 'a' || c1 == 'A') && (c2 == 'n' || c2 == 'N')) {
                    NamedSize nsize = getSize(offset + 3, false);
                    if (nsize != null) {
                        return new ConstAndType(
                                reader.peek(offset0, offset + 2 + nsize.image.length()),
                                new String(new char[]{(char) c0, (char) c1, (char) c2}),
                                nsize
                        );
                    }
                    if (c3 <= 0 || c3 == '_') {
                        return new ConstAndType(
                                reader.peek(offset0, offset + 2),
                                new String(new char[]{(char) c0, (char) c1, (char) c2}),
                                nsize
                        );
                    }
                    return null;
                }
                break;
            }
        }
        return null;
    }

    private NElementTokenImpl applyZeroValue(TSONNumberInfo info, StringBuilder image, int line, int col, long pos) {
        if (info.baseValue == null) {
            // This was the old 0s32 style (no constant name)
            // We'll treat it as 0
            if (info.unsignedNumber) {
                switch (info.bits) {
                    case 8:
                        return new NElementTokenImpl(image.toString(), NElementTokenType.NUMBER,
                                image.toString(), 0, line, col, pos,
                                new DefaultNNumberElement(NElementType.UBYTE, (short) 0, NNumberLayout.DECIMAL, "", image.toString(), null, null)
                                , null);
                    case 16:
                        return new NElementTokenImpl(image.toString(),
                                NElementTokenType.NUMBER,
                                image.toString(), 0, line, col, pos,
                                new DefaultNNumberElement(NElementType.USHORT, 0, NNumberLayout.DECIMAL, "", image.toString(), null, null)
                                , null);
                    case 32:
                        return new NElementTokenImpl(image.toString(),
                                NElementTokenType.NUMBER,
                                image.toString(), 0, line, col, pos,
                                new DefaultNNumberElement(NElementType.UINT, 0L, NNumberLayout.DECIMAL, "", image.toString(), null, null)
                                , null);
                    case 64:
                        return new NElementTokenImpl(image.toString(),
                                NElementTokenType.NUMBER,
                                image.toString(), 0, line, col, pos,
                                new DefaultNNumberElement(NElementType.ULONG, BigInteger.ZERO, NNumberLayout.DECIMAL, "", image.toString(), null, null)
                                , null);
                }
            } else {
                switch (info.bits) {
                    case 8:
                        return new NElementTokenImpl(image.toString(), NElementTokenType.NUMBER,
                                image.toString(), 0, line, col, pos,
                                new DefaultNNumberElement(NElementType.BYTE, (byte) 0, NNumberLayout.DECIMAL, "", image.toString(), null, null)
                                , null);
                    case 16:
                        return new NElementTokenImpl(image.toString(), NElementTokenType.NUMBER,
                                image.toString(), 0, line, col, pos,
                                new DefaultNNumberElement(NElementType.SHORT, (short) 0, NNumberLayout.DECIMAL, "", image.toString(), null, null)
                                , null);
                    case 32:
                        return new NElementTokenImpl(image.toString(), NElementTokenType.NUMBER,
                                image.toString(), 0, line, col, pos,
                                new DefaultNNumberElement(NElementType.INT, (int) 0, NNumberLayout.DECIMAL, "", image.toString(), null, null)
                                , null);
                    case 64:
                        return new NElementTokenImpl(image.toString(), NElementTokenType.NUMBER,
                                image.toString(), 0, line, col, pos,
                                new DefaultNNumberElement(NElementType.LONG, 0L, NNumberLayout.DECIMAL, "", image.toString(), null, null)
                                , null);
                    case -1:
                        return new NElementTokenImpl(image.toString(), NElementTokenType.NUMBER,
                                image.toString(), 0, line, col, pos,
                                new DefaultNNumberElement(NElementType.BIG_INT, BigInteger.ZERO, NNumberLayout.DECIMAL, "", image.toString(), null, null)
                                , null);
                }
            }
        }
        throw new NUnexpectedException(NMsg.ofC("should never happen"));
    }

    private NNumberElement applyConstantValue(TSONNumberInfo info, StringBuilder image) {
        String name = info.baseValue;
        if (info.floatingNumber) {
            if (info.bits == 32) {
                switch (name) {
                    case "max":
                        return new DefaultNNumberElement(
                                NElementType.FLOAT,
                                Float.MAX_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    case "min":
                        return new DefaultNNumberElement(
                                NElementType.FLOAT,
                                Float.MIN_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    case "pinf":
                        return new DefaultNNumberElement(
                                NElementType.FLOAT,
                                Float.POSITIVE_INFINITY,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    case "ninf":
                        return new DefaultNNumberElement(
                                NElementType.FLOAT,
                                Float.NEGATIVE_INFINITY,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    case "nan":
                        return new DefaultNNumberElement(
                                NElementType.FLOAT,
                                Float.NaN,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                }
            } else {
                switch (name) {
                    case "max":
                        return new DefaultNNumberElement(
                                NElementType.DOUBLE,
                                Double.MAX_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    case "min":
                        return new DefaultNNumberElement(
                                NElementType.DOUBLE,
                                Double.MIN_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    case "pinf":
                        return new DefaultNNumberElement(
                                NElementType.DOUBLE,
                                Double.POSITIVE_INFINITY,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    case "ninf":
                        return new DefaultNNumberElement(
                                NElementType.DOUBLE,
                                Double.NEGATIVE_INFINITY,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    case "nan":
                        return new DefaultNNumberElement(
                                NElementType.DOUBLE,
                                Double.NaN,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                }
            }
        }

        if (info.unsignedNumber) {
            switch (info.bits) {
                case 8: {
                    if ("max".equals(name) || "pinf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.UBYTE,
                                (short) 255,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    } else if ("min".equals(name) || "ninf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.UBYTE,
                                (short) 0,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    }
                    break;
                }
                case 16: {
                    if ("max".equals(name) || "pinf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.USHORT,
                                (int) 65535,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    } else if ("min".equals(name) || "ninf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.USHORT,
                                (int) 0,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    }
                    break;
                }
                case 32:
                    if ("max".equals(name) || "pinf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.UINT,
                                4294967295L,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    } else if ("min".equals(name) || "ninf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.UINT,
                                0L,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    }
                    break;
                case 64: {
                    if ("max".equals(name) || "pinf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.ULONG,
                                new BigInteger("18446744073709551615"),
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    } else if ("min".equals(name) || "ninf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.ULONG,
                                BigInteger.ZERO,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    }
                    break;
                }
            }
        } else {
            switch (info.bits) {
                case -2: // no type defined
                {
                    switch (name) {
                        case "max": {
                            return new DefaultNNumberElement(
                                    NElementType.INT,
                                    Integer.MAX_VALUE,
                                    info.numberLayout,
                                    info.suffix,
                                    image.toString(), null, null
                            );
                        }
                        case "min": {
                            return new DefaultNNumberElement(
                                    NElementType.INT,
                                    Integer.MIN_VALUE,
                                    info.numberLayout,
                                    info.suffix,
                                    image.toString(), null, null
                            );
                        }
                        case "nan": {
                            return new DefaultNNumberElement(
                                    NElementType.DOUBLE,
                                    Double.NaN,
                                    info.numberLayout,
                                    info.suffix,
                                    image.toString(), null, null
                            );
                        }
                        case "pinf": {
                            return new DefaultNNumberElement(
                                    NElementType.DOUBLE,
                                    Double.POSITIVE_INFINITY,
                                    info.numberLayout,
                                    info.suffix,
                                    image.toString(), null, null
                            );
                        }
                        case "ninf": {
                            return new DefaultNNumberElement(
                                    NElementType.DOUBLE,
                                    Double.NEGATIVE_INFINITY,
                                    info.numberLayout,
                                    info.suffix,
                                    image.toString(), null, null
                            );
                        }
                    }
                    break;
                }
                case 8: {
                    if ("max".equals(name) || "pinf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.BYTE,
                                Byte.MAX_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    } else if ("min".equals(name) || "ninf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.BYTE,
                                Byte.MIN_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    }
                    break;
                }
                case 16:
                    if ("max".equals(name) || "pinf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.SHORT,
                                Short.MAX_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    } else if ("min".equals(name) || "ninf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.SHORT,
                                Short.MIN_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    }
                    break;
                case 32:
                    if ("max".equals(name) || "pinf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.INT,
                                Integer.MAX_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    } else if ("min".equals(name) || "ninf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.INT,
                                Integer.MIN_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    }
                    break;
                case 64:
                    if ("max".equals(name) || "pinf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.LONG,
                                Long.MAX_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    } else if ("min".equals(name) || "ninf".equals(name)) {
                        return new DefaultNNumberElement(
                                NElementType.LONG,
                                Long.MIN_VALUE,
                                info.numberLayout,
                                info.suffix,
                                image.toString(), null, null
                        );
                    }
                    break;
                case -1:
                    // BigInt doesn't have max/min in the same way, but we can support min=0 for unsigned?
                    // For signed BigInt, max/min are infinite.
                    break;
            }
        }
        return null;
    }

    private static class TSONNumberInfo {
        public boolean specialConst;      // e.g. "10" or "0x1A"
        public String baseValue;      // e.g. "10" or "0x1A"
        public String imaginaryValue; // e.g. "5" (if complex)
        public int bits = -2;     // e.g. 32, 64, -1 is big decimal, -2 is unknown yet
        public boolean unsignedNumber;    // true if 'u'
        public boolean floatingNumber;    // true if 'u'
        public String suffix;           // e.g. "ohm"
        public NNumberLayout numberLayout;           // e.g. "ohm"
        public boolean specialNumber;     // true if inf/nan
    }

    private boolean isImaginaryNumberChar(int c) {
        return c == 'i' || c == 'î';
    }

    private NElementTokenImpl readNormalNumber(StringBuilder image, int line, int col, long pos) {
        TSONNumberInfo info = new TSONNumberInfo();
        NMsg errorMessage = null;
        NElementType numberElementType = NElementType.INT;
        Number numberValue = 0;

        // --- 1. BASE DETECTION ---
        boolean isDecimal = true;
        NNumberLayout layout = NNumberLayout.DECIMAL;
        if (reader.peek() == '0') {
            int next = reader.peekAt(1);
            if (next == 'x' || next == 'b' || next == 'o') {
                isDecimal = false;
                image.append((char) reader.read()); // consume '0'
                char b = (char) reader.read();
                image.append(b); // consume 'x', 'b', or 'o'
                switch (b) {
                    case 'x':
                        layout = NNumberLayout.HEXADECIMAL;
                        break;
                    case 'b':
                        layout = NNumberLayout.BINARY;
                        break;
                    case 'o':
                        layout = NNumberLayout.OCTAL;
                        break;
                }
            }
        }

        // --- 2. REAL COMPONENT ---
        int realStart = image.length();
        if (readDigits(image, isDecimal)) {
            info.floatingNumber = true;
        }
        info.baseValue = image.substring(realStart);

        // --- 3. COMPLEX TRANSITION ---
        int p = reader.peek();
        if (isDecimal && (p == '+' || p == '-') && isImaginaryPartNext()) {
            char sign = (char) reader.read();
            image.append(sign);

            int imagStart = image.length();
            if (readDigits(image, true)) {
                info.floatingNumber = true;
            }
            info.imaginaryValue = image.substring(imagStart);

            if (isImaginaryNumberChar(reader.peek())) {
                image.append((char) reader.read());
            }
        } else if (isDecimal && isImaginaryNumberChar(p)) {
            // Pure imaginary case: 5i
            info.imaginaryValue = info.baseValue;
            info.baseValue = "0";
            image.append((char) reader.read());
        }

        // --- 4. TYPE SUFFIX (The Lock) ---
        p = reader.peek();
        if (p == 'u' || p == 's') {
            NamedSize ns = getSize(0, true);
            if (ns != null) {
                image.append(reader.read(ns.image.length()));
                info.unsignedNumber = ns.floatingNumber;
                info.floatingNumber = ns.floatingNumber;
                info.bits = ns.bits;
            }
        } else {
//            if (info.floatingNumber) {
//                info.bits = 64; // default to double
//            } else {
//                info.bits = 32; // default to int
//            }
        }

        // --- 5. UNIT ---
        info.suffix = consumeSuffix(image);

        if (info.imaginaryValue != null) {
            if (info.bits == -1) {
                numberElementType = NElementType.BIG_COMPLEX;
                try {
                    numberValue = (new NBigComplex(new BigDecimal(info.baseValue), new BigDecimal(info.imaginaryValue)));
                } catch (Exception ex) {
                    errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                    numberValue = (NBigComplex.ZERO);
                }
            } else if (info.bits <= 32) {
                numberElementType = NElementType.FLOAT_COMPLEX;
                try {
                    numberValue = (new NFloatComplex(Float.parseFloat(info.baseValue), Float.parseFloat(info.imaginaryValue)));
                } catch (Exception ex) {
                    errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                    numberValue = (NFloatComplex.ZERO);
                }
            } else {
                numberElementType = NElementType.DOUBLE_COMPLEX;
                try {
                    numberValue = (new NDoubleComplex(Double.parseDouble(info.baseValue), Double.parseDouble(info.imaginaryValue)));
                } catch (Exception ex) {
                    errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                    numberValue = (NDoubleComplex.ZERO);
                }
            }
        } else {
            if (info.unsignedNumber) {
                BigInteger bi;
                try {
                    bi = parseBigInteger(info.baseValue, layout);
                } catch (Exception ex) {
                    errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                    bi = BigInteger.ZERO;
                }
                switch (info.bits) {
                    case 8:
                        numberElementType = NElementType.UBYTE;
                        numberValue = (bi.shortValue());
                        break;
                    case 16:
                        numberElementType = NElementType.USHORT;
                        numberValue = (bi.intValue());
                        break;
                    case 32:
                        numberElementType = NElementType.UINT;
                        numberValue = (bi.longValue());
                        break;
                    case 64:
                    default:
                        numberElementType = NElementType.ULONG;
                        numberValue = (bi);
                        break;
                }
            } else {
                if (info.bits == -1) {
                    if (info.baseValue.contains(".") || info.baseValue.contains("e") || info.baseValue.contains("E")) {
                        numberElementType = NElementType.BIG_DECIMAL;
                        try {
                            numberValue = (new BigDecimal(info.baseValue));
                        } catch (Exception ex) {
                            errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                            numberValue = (new BigDecimal(0));
                        }
                    } else {
                        numberElementType = NElementType.BIG_INT;
                        try {
                            numberValue = (new BigInteger(info.baseValue));
                        } catch (Exception ex) {
                            errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                            numberValue = (BigInteger.ZERO);
                        }
                    }
                } else {
                    switch (info.bits) {
                        case 8: {
                            numberElementType = NElementType.BYTE;
                            try {
                                numberValue = (Byte.parseByte(info.baseValue));
                            } catch (Exception ex) {
                                errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                                numberValue = ((byte) 0);
                            }
                            break;
                        }
                        case 16: {
                            numberElementType = NElementType.SHORT;
                            try {
                                numberValue = (Short.parseShort(info.baseValue));
                            } catch (Exception ex) {
                                errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                                numberValue = ((short) 0);
                            }
                            break;
                        }
                        case 32:
                            numberElementType = NElementType.INT;
                            if (info.baseValue.contains(".") || info.baseValue.contains("e") || info.baseValue.contains("E")) {
                                try {
                                    numberValue = (Float.parseFloat(info.baseValue));
                                } catch (Exception ex) {
                                    errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                                    numberValue = (0.0f);
                                }

                            } else {
                                try {
                                    numberValue = (Integer.parseInt(info.baseValue));
                                } catch (Exception ex) {
                                    errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                                    numberValue = (0);
                                }
                            }
                            break;
                        case 64: {
                            numberElementType = NElementType.LONG;
                            if (info.baseValue.contains(".") || info.baseValue.contains("e") || info.baseValue.contains("E")) {
                                try {
                                    numberValue = (Double.parseDouble(info.baseValue));
                                } catch (Exception ex) {
                                    errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                                    numberValue = (0.0);
                                }
                            } else {
                                try {
                                    numberValue = (Long.parseLong(info.baseValue));
                                } catch (Exception ex) {
                                    errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                                    numberValue = (0L);
                                }
                            }
                            break;
                        }
                        case -2: // auto detect
                        default: {
                            if (info.baseValue.contains(".") || info.baseValue.contains("e") || info.baseValue.contains("E")) {
                                numberElementType = NElementType.DOUBLE;
                                try {
                                    numberValue = (Double.parseDouble(info.baseValue));
                                } catch (Exception ex) {
                                    try {
                                        numberValue = new BigDecimal(info.baseValue);
                                        numberElementType = NElementType.BIG_DECIMAL;
                                    } catch (Exception ex2) {
                                        errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                                        numberValue = (0.0);
                                    }
                                }
                            } else {
                                numberElementType = NElementType.INT;
                                try {
                                    numberValue = (Integer.parseInt(info.baseValue));
                                } catch (Exception ex) {
                                    try {
                                        numberValue = (Long.parseLong(info.baseValue));
                                        numberElementType = NElementType.LONG;
                                    } catch (Exception ex2) {
                                        try {
                                            numberValue = new BigInteger(info.baseValue);
                                            numberElementType = NElementType.BIG_INT;
                                        } catch (Exception ex3) {
                                            errorMessage = NMsg.ofC("%s", NExceptions.getErrorMessage(ex));
                                            numberValue = (0);
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return new NElementTokenImpl(image.toString(), NElementTokenType.NUMBER, "", 0, line, col, pos,
                new DefaultNNumberElement(
                        numberElementType, numberValue, layout, info.suffix,
                        image.toString(),
                        null, null
                )
                , errorMessage
        );
    }

    private BigInteger parseBigInteger(String value, NNumberLayout layout) {
        int radix = 10;
        switch (layout) {
            case HEXADECIMAL:
                radix = 16;
                break;
            case OCTAL:
                radix = 8;
                break;
            case BINARY:
                radix = 2;
                break;
        }
        return new BigInteger(value, radix);
    }

    private boolean isImaginaryPartNext() {
        int offset = 1; // Start peeking after the +/-
        int c = reader.peekAt(offset);

        // Skip digits, underscores, and dots
        while (Character.isDigit(c) || c == '.' || c == '_') {
            offset++;
            c = reader.peekAt(offset);
        }

        // If it ends in i or î, it's a complex literal!
        return c == 'i' || c == 'î';
    }


    private boolean readDigits(StringBuilder sb, boolean acceptFloating) {
        boolean wasFloating = false;
        while (true) {
            int c = reader.peek();
            if (Character.isDigit(c) || c == '_') {
                sb.append((char) reader.read());
            } else if ((acceptFloating && c == '.')) {
                sb.append((char) reader.read());
                wasFloating = true;
            } else if (acceptFloating && (c == 'e' || c == 'E')) {
                wasFloating = true;
                // Check if this is truly scientific notation (e followed by digit or +/-)
                int next = reader.peekAt(1);
                if (Character.isDigit(next) || next == '+' || next == '-') {
                    sb.append((char) reader.read()); // consume 'e'
                    int sign = reader.peek();
                    if (sign == '+' || sign == '-') {
                        sb.append((char) reader.read()); // consume sign
                    }
                    // Continue reading the exponent digits
                } else {
                    break; // 'e' might be the start of a unit or keyword
                }
            } else if (!acceptFloating && NHex.isHexDigit((char) c)) {
                sb.append((char) reader.read());
            } else {
                break;
            }
        }
        return wasFloating;
    }

    private String consumeSuffix(StringBuilder sb) {
        StringBuilder s = new StringBuilder();
        while (true) {
            int p = reader.peek();
            if (p == '%' || p == '_' || Character.isAlphabetic(p)) {
                char c = (char) reader.read();
                sb.append(c);
                s.append(c);
            } else {
                break;
            }
        }
        if (s.length() > 2 && s.charAt(0) == '_') {
            //drop first _ in unit
            s.deleteCharAt(0);
        }
        return s.toString();
    }


    private NElementTokenImpl continueReadTemporalOrNumber() {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        if (reader.canRead(10)
                && Character.isDigit(reader.peekAt(0))
                && Character.isDigit(reader.peekAt(1))
                && Character.isDigit(reader.peekAt(2))
                && Character.isDigit(reader.peekAt(3))
                && '-' == reader.peekAt(4)
                && Character.isDigit(reader.peekAt(5))
                && Character.isDigit(reader.peekAt(6))
                && '-' == reader.peekAt(7)
                && Character.isDigit(reader.peekAt(8))
                && Character.isDigit(reader.peekAt(9))
        ) {
            if (reader.canRead(16)
                    && ((reader.peekAt(10) == 'T')
                    && Character.isDigit(reader.peekAt(11))
                    && Character.isDigit(reader.peekAt(12))
                    && ':' == reader.peekAt(13)
                    && Character.isDigit(reader.peekAt(14))
                    && Character.isDigit(reader.peekAt(15))
            )) {
                if (reader.canRead(19)
                        && (reader.peekAt(16) == ':')
                        && Character.isDigit(reader.peekAt(17))
                        && Character.isDigit(reader.peekAt(18))
                ) {
                    int h = 19;
                    if (reader.canRead(h + 1) && reader.peekAt(h) == '.') {
                        h++;
                        while (reader.canRead(h + 1) && Character.isDigit(reader.peekAt(h))) {
                            h++;
                        }
                    }
                    if (reader.canRead(h + 1) && reader.peekAt(h) == 'Z') {
                        h++;
                        String str = reader.read(h);
                        Instant i = Instant.parse(str);
                        return new NElementTokenImpl(str, NElementTokenType.INSTANT, "", 0, line, column, pos,
                                new DefaultNPrimitiveElement(NElementType.INSTANT, i)

                                , null);
                    }
                    String str = reader.read(h);
                    LocalDateTime i = LocalDateTime.parse(str);
                    return new NElementTokenImpl(str, NElementTokenType.DATETIME, "", 0, line, column, pos,
                            //should include raw image as well
                            new DefaultNPrimitiveElement(NElementType.LOCAL_DATETIME, i)
                            , null);
                } else {
                    String str = reader.read(16);
                    LocalDateTime i = LocalDateTime.parse(str);
                    return new NElementTokenImpl(str, NElementTokenType.DATETIME, "", 0, line, column, pos,
                            new DefaultNPrimitiveElement(NElementType.LOCAL_DATETIME, i)

                            , null);
                }
            } else {
                String str = reader.read(10);
                LocalDate i = LocalDate.parse(str);
                return new NElementTokenImpl(str, NElementTokenType.DATE, "", 0, line, column, pos,
                        new DefaultNPrimitiveElement(NElementType.LOCAL_DATE, i)

                        , null);
            }
        } else if (reader.canRead(8)
                && Character.isDigit(reader.peekAt(0))
                && Character.isDigit(reader.peekAt(1))
                && ':' == reader.peekAt(2)
                && Character.isDigit(reader.peekAt(3))
                && Character.isDigit(reader.peekAt(4))
                && ':' == reader.peekAt(5)
                && Character.isDigit(reader.peekAt(6))
                && Character.isDigit(reader.peekAt(7))
        ) {
            int h = 8;
            if (reader.canRead(h + 1) && reader.peekAt(h) == '.') {
                h++;
                while (reader.canRead(h + 1) && Character.isDigit(reader.peekAt(h))) {
                    h++;
                }
            }
            String str = reader.read(h);
            LocalTime i = LocalTime.parse(str);
            return new NElementTokenImpl(str, NElementTokenType.TIME, "", 0, line, column, pos,
                    new DefaultNPrimitiveElement(NElementType.LOCAL_TIME, i)
                    , null);
        }
        return continueReadNumber();
    }

    private NElementTokenImpl continueReadRepeatedChar(int c, NElementTokenType tt) {
        StringBuilder image = new StringBuilder();
        int count = 1;
        while (true) {
            image.append(reader.read());
            int c2 = reader.peek();
            if (c2 == -1 || c2 != c) {
                break;
            }
            count++;
        }
        String s = image.toString();
        return new NElementTokenImpl(s, tt, String.valueOf(s.charAt(0)), count, reader.line(), reader.column(), reader.pos(), s, null);
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
            case '.':
            {
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
        return new NElementTokenImpl(image, NElementTokenType.OP, image, 0, reader.line(), reader.column(), reader.pos(), tt, null);
    }
}
