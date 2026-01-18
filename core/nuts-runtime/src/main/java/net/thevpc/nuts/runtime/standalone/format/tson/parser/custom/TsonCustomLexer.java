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

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementCommentType;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.elem.NNumberLayout;
import net.thevpc.nuts.elem.NOperatorSymbol;
import net.thevpc.nuts.elem.NPrimitiveElementBuilder;
import net.thevpc.nuts.io.NInputStreamProvider;
import net.thevpc.nuts.io.NPositionedCharReader;
import net.thevpc.nuts.io.NReaderProvider;
import net.thevpc.nuts.math.NBigComplex;
import net.thevpc.nuts.math.NDoubleComplex;
import net.thevpc.nuts.math.NFloatComplex;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNPrimitiveElement;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNStringElement;
import net.thevpc.nuts.runtime.standalone.elem.item.NElementCommentImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementToken;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenType;
import net.thevpc.nuts.util.NExceptions;

public class TsonCustomLexer {
    private NPositionedCharReader reader;

    public TsonCustomLexer(String reader) {
        this(new StringReader(reader));
    }

    public TsonCustomLexer(Reader reader) {
        this.reader = new NPositionedCharReader(reader);
    }

    public List<NElementToken> all() {
        List<NElementToken> ret = new ArrayList<>();
        while (true) {
            NElementToken u = next();
            if (u == null) {
                break;
            }
            ret.add(u);
        }
        return ret;
    }

    public NElementToken next() {
        StringBuilder currentError = new StringBuilder();
        int errline = 0;
        int errcolumn = 0;
        long errpos = 0;
        while (true) {
            int c = reader.peek();
            if (c == -1) {
                if (currentError.length() > 0) {
                    return new NElementToken(
                            currentError.toString(),
                            NElementTokenType.UNKNOWN,
                            "",
                            0,
                            errline, errcolumn, errpos,
                            currentError.length(), "unexpected token"
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
                case '[':
                    return asChar(c, NElementTokenType.LBRACK);
                case ']':
                    return asChar(c, NElementTokenType.RBRACK);
                case '(':
                    return asChar(c, NElementTokenType.LPAREN);
                case ')':
                    return asChar(c, NElementTokenType.RPAREN);
                case '#':
                    return continueReadRepeatedChar(c, NElementTokenType.ORDERED_LIST);
                case '.': {
                    int c1 = reader.peekChar(1);
                    if (c1 != -1) {
                        if (Character.isDigit(c1)) {
                            return continueReadNumber();
                        }
                        if (c1 != '¶' && Character.isAlphabetic(c1)) {
                            return continueReadIdentifier();
                        }
                    }
                    StringBuilder image = new StringBuilder();
                    int count = 0;
                    while (reader.peek() == '.') {
                        image.append((char) reader.read());
                        count++;
                        if (count >= 10) break; // safety
                    }
                    String s = image.toString();
                    return new NElementToken(s, NElementTokenType.UNORDERED_LIST, String.valueOf(s.charAt(0)), count, line, column, pos, s, null);
                }
                case '¶': {
                    int c2 = reader.peek();
                    if (c2 == '¶') {
                        return continueReadUserMultiLine();
                    }
                    return continueReadUserSingleLine();
                }
                case ';': {
                    int c2 = reader.peek();
                    if (c2 == ';') {
                        reader.read(2);
                        return new NElementToken(";;", NElementTokenType.SEMICOLON2, ";;", 0, line, column, pos, c, null);
                    }
                    return asChar(c, NElementTokenType.SEMICOLON);
                }
                case ',':
                    return asChar(c, NElementTokenType.COMMA);
                case ':': {
                    int n = reader.peekChar(1);
                    switch (n) {
                        case ':': {
                            reader.read(2);
                            return new NElementToken("::", NElementTokenType.OP, "::", 0, line, column, pos, NOperatorSymbol.COLON2, null);
                        }
                        case '=': {
                            n = reader.peekChar(2);
                            if (n == '=') {
                                reader.read(3);
                                return new NElementToken(":==", NElementTokenType.OP, ":==", 0, line, column, pos, NOperatorSymbol.COLON_EQ2, null);
                            }
                            reader.read(2);
                            return new NElementToken(":=", NElementTokenType.OP, ":=", 0, line, column, pos, NOperatorSymbol.COLON_EQ, null);
                        }
                        default: {
                            return asChar(c, NElementTokenType.COLON);
                        }
                    }
                }
                case '/': {
                    int n = reader.peekChar(1);
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
                    int n = reader.peekChar(1);
                    switch (n) {
                        case '\\': {
                            reader.read(2);
                            return new NElementToken("\\", NElementTokenType.BACKSLASH2, "\\", 0, line, column, pos, c, null);
                        }
                        default: {
                            return asChar(c, NElementTokenType.BACKSLASH);
                        }
                    }
                }
                case '?':
                    return asChar123(c, NOperatorSymbol.INTERROGATION, NOperatorSymbol.INTERROGATION2, NOperatorSymbol.INTERROGATION3);
                case '!': {
                    return asChar123Eq(c, NOperatorSymbol.NOT, NOperatorSymbol.NOT2, NOperatorSymbol.NOT3, NOperatorSymbol.NOT_EQ, NOperatorSymbol.NOT_EQ2);
                }
                case '~': {
                    return asChar123Eq(c, NOperatorSymbol.TILDE, NOperatorSymbol.TILDE2, NOperatorSymbol.TILDE3, NOperatorSymbol.TILDE_EQ, NOperatorSymbol.TILDE_EQ2);
                }
                case '%':
                    return asChar123(c, NOperatorSymbol.REM, NOperatorSymbol.REM2, NOperatorSymbol.REM3);
                case '&':
                    return asChar123(c, NOperatorSymbol.AND, NOperatorSymbol.AND2, NOperatorSymbol.AND3);
                case '^': {
                    return continueReadStream(c, line, column, pos);
                }
                case '@': {
                    int n = reader.peekChar(1);
                    if (n == c) {
                        int n2 = reader.peekChar(2);
                        char cc = (char) c;
                        if (n2 == c) {
                            reader.read(3);
                            String i = new String(new char[]{cc, cc, cc});
                            return new NElementToken(i, NElementTokenType.OP, i, 0, line, column, pos, NOperatorSymbol.AT3, null);
                        } else {
                            reader.read(2);
                            String i = new String(new char[]{cc, cc});
                            return new NElementToken(i, NElementTokenType.OP, i, 0, line, column, pos, NOperatorSymbol.AT2, null);
                        }
                    }
                    return asChar(c, NElementTokenType.AT);
                }
                case '<': {
                    int c1 = reader.peekChar(1);
                    if (c1 == '<') {
                        if (reader.peekChar(2) == '<') {
                            reader.read(3);
                            return new NElementToken("<<<", NElementTokenType.OP, "<<<", 0, line, column, pos, NOperatorSymbol.LT3, null);
                        }
                        reader.read(2);
                        return new NElementToken("<<", NElementTokenType.OP, "<<", 0, line, column, pos, NOperatorSymbol.LT2, null);
                    } else if (c1 == '>') {
                        reader.read(2);
                        return new NElementToken("<>", NElementTokenType.OP, "<>", 0, line, column, pos, NOperatorSymbol.LT_GT, null);
                    } else if (c1 == '=') {
                        int c2 = reader.peekChar(2);
                        if (c2 == '=') {
                            reader.read(3);
                            return new NElementToken("<==", NElementTokenType.OP, "<<<", 0, line, column, pos, NOperatorSymbol.LT_EQ2, null);
                        }
                        reader.read(2);
                        return new NElementToken("<=", NElementTokenType.OP, "<<", 0, line, column, pos, NOperatorSymbol.LTE, null);
                    } else if (c1 == '-') {
                        int c2 = reader.peekChar(2);
                        if (c2 == '-') {
                            reader.read(3); // <--x
                            return new NElementToken("<--", NElementTokenType.OP, "<<<", 0, line, column, pos, NOperatorSymbol.LT_MINUS2, null);
                        } else if (Character.isDigit(c2)) {
                            //<-3
                            reader.read(); // <--x
                            return new NElementToken("<", NElementTokenType.OP, "<", 0, line, column, pos, NOperatorSymbol.LT, null);
                        }
                        reader.read(2);
                        return new NElementToken("<=", NElementTokenType.OP, "<<", 0, line, column, pos, NOperatorSymbol.LT2, null);
                    }
                    reader.read();
                    return new NElementToken("<", NElementTokenType.OP, "&", 0, line, column, pos, NOperatorSymbol.LT, null);
                }

                case '-': {
                    int c1 = reader.peekChar(1);
                    if (Character.isDigit(c1)) {
                        return continueReadNumber();
                    }
                    return asChar123Eq(c, NOperatorSymbol.MINUS, NOperatorSymbol.MINUS2, NOperatorSymbol.MINUS3, NOperatorSymbol.MINUS_EQ, NOperatorSymbol.MINUS_EQ2);
                }

                case '+': {
                    int c1 = reader.peekChar(1);
                    if (Character.isDigit(c1)) {
                        return continueReadNumber();
                    }
                    return asChar123Eq(c, NOperatorSymbol.PLUS, NOperatorSymbol.PLUS2, NOperatorSymbol.PLUS3, NOperatorSymbol.PLUS_EQ, NOperatorSymbol.PLUS_EQ2);
                }

                case '*': {
                    return asChar123Eq(c, NOperatorSymbol.MUL, NOperatorSymbol.MUL2, NOperatorSymbol.MUL3, NOperatorSymbol.MUL_EQ, NOperatorSymbol.MUL_EQ2);
                }

                case '=': {
                    int c1 = reader.peekChar(1);
                    if (c1 == '=') {
                        int c2 = reader.peekChar(2);
                        if (c2 == '=') {
                            reader.read(3);
                            return new NElementToken("===", NElementTokenType.OP, "===", 0, line, column, pos, NOperatorSymbol.EQ3, null);
                        } else if (c2 == '>') {
                            reader.read(3);
                            return new NElementToken("==>", NElementTokenType.OP, "==>", 0, line, column, pos, NOperatorSymbol.EQ2_GT, null);
                        }
                        reader.read(2);
                        return new NElementToken("==", NElementTokenType.OP, "==", 0, line, column, pos, NOperatorSymbol.EQ2, null);
                    } else if (c1 == '>') {
                        reader.read(2);
                        return new NElementToken("=>", NElementTokenType.OP, "=>", 0, line, column, pos, NOperatorSymbol.EQ_GT, null);
                    }
                    reader.read();
                    return new NElementToken("=", NElementTokenType.OP, "=", 0, line, column, pos, NOperatorSymbol.EQ, null);
                }
                case '>': {
                    int c1 = reader.peekChar(1);
                    if (c1 == '>') {
                        if (reader.peekChar(2) == '>') {
                            reader.read(3);
                            return new NElementToken(">>>", NElementTokenType.OP, ">>>", 0, line, column, pos, NOperatorSymbol.GT3, null);
                        }
                        reader.read(2);
                        return new NElementToken(">>", NElementTokenType.OP, ">>", 0, line, column, pos, NOperatorSymbol.GT2, null);
                    } else if (c1 == '=') {
                        reader.read(2);
                        return new NElementToken(">=", NElementTokenType.OP, ">=", 0, line, column, pos, NOperatorSymbol.GTE, null);
                    }
                    reader.read();
                    return new NElementToken(">", NElementTokenType.OP, ">", 0, line, column, pos, NOperatorSymbol.GT, null);
                }
                case '"': {
                    if (reader.canRead(2) && reader.peekChar(1) == c && reader.peekChar(2) == c) {
                        reader.read(); // consume first "
                        reader.read(); // second
                        reader.read(); // third
                        return continueReadDoubleQuoted3();
                    }
                    reader.read(); // third
                    return continueReadDoubleQuoted1();
                }
                case '\'': {
                    if (reader.canRead(2) && reader.peekChar(1) == c && reader.peekChar(2) == c) {
                        reader.read(); // consume first "
                        reader.read(); // second
                        reader.read(); // third
                        return continueReadSingleQuoted3();
                    }
                    reader.read(); // third
                    return continueReadSingleQuoted1();
                }
                case '`': {
                    if (reader.canRead(2) && reader.peekChar(1) == c && reader.peekChar(2) == c) {
                        reader.read(); // consume first "
                        reader.read(); // second
                        reader.read(); // third
                        return continueReadBacktick3();
                    }
                    reader.read(); // third
                    return continueReadBacktick1();
                }
                case ' ': {
                    StringBuilder sb = new StringBuilder();
                    while (reader.peek() == ' ') {
                        sb.append((char) reader.read());
                    }
                    return new NElementToken(sb.toString(), NElementTokenType.WHITESPACE, " ", 0, line, column, pos, c, null);
                }
                case '\t': {
                    StringBuilder sb = new StringBuilder();
                    while (reader.peek() == '\t') {
                        sb.append((char) reader.read());
                    }
                    return new NElementToken(sb.toString(), NElementTokenType.WHITESPACE, "\t", 0, line, column, pos, c, null);
                }
                case '\n': {
                    reader.read();
                    return new NElementToken("\n", NElementTokenType.WHITESPACE, "\n", 0, line, column, pos, c, null);
                }
                case '\r': {
                    reader.read();
                    if (reader.peek() == '\n') {
                        reader.read();
                        return new NElementToken("\r\n", NElementTokenType.WHITESPACE, "\r\n", 0, line, column, pos, c, null);
                    }
                    return new NElementToken("\r", NElementTokenType.WHITESPACE, "\r", 0, line, column, pos, c, null);
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
                        return new NElementToken(sb.toString(), NElementTokenType.WHITESPACE, String.valueOf(c0), 0, line, column, pos, c, null);
                    }
                    if (Character.isAlphabetic(c)) {
                        return continueReadIdentifier();
                    }
                    break;
                }
            }
            if (currentError.length() == 0) {
                errline = reader.line();
                errcolumn = reader.column();
                errpos = reader.pos();
            }
            currentError.append((char) c);
        }
    }


    private NElementToken continueReadStream(int c, int line, int column, long pos) {
        StringBuilder image = new StringBuilder();
        image.append((char) c);
        int count = 1;
        while (reader.peek() == '^' && count < 3) {
            image.append((char) reader.read());
            count++;
        }

        // Peek for identifier
        int p = reader.peek();
        String id = "";
        int idLen = 0;
        if (Character.isAlphabetic(p)) {
            while (true) {
                int next = reader.peekChar(idLen);
                if (Character.isLetterOrDigit(next)) {
                    idLen++;
                } else {
                    break;
                }
            }
        }

        int nextAfterId = reader.peekChar(idLen);
        if (nextAfterId == '[' || nextAfterId == '{') {
            // It's a stream! Consume identifier
            if (idLen > 0) {
                id = reader.read(idLen);
                image.append(id);
            }
            if (nextAfterId == '[') {
                image.append((char) reader.read()); // consume '['
                StringBuilder content = new StringBuilder();
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
                return new NElementToken(image.toString(), NElementTokenType.UNKNOWN, "stream-binary", 0, line, column, pos,
                        NElement.ofBinaryStream(provider), null);
            } else {
                image.append((char) reader.read()); // consume '{'
                StringBuilder content = new StringBuilder();
                String endMarker = "^" + id + "}";
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
                return new NElementToken(image.toString(), NElementTokenType.UNKNOWN, "stream-text", 0, line, column, pos,
                        NElement.ofCharStream(provider)
                        , null);
            }
        }

        // Not a stream, just the operator
        NOperatorSymbol type = NOperatorSymbol.HAT;
        if (count == 2) type = NOperatorSymbol.HAT2;
        if (count == 3) type = NOperatorSymbol.HAT3;
        return new NElementToken(image.toString(), NElementTokenType.OP, image.toString(), 0, line, column, pos, type, null);
    }

    private NElementToken continueReadLineComments() {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        StringBuilder image = new StringBuilder("//");
        reader.read(2); // consume //
        while (true) {
            int c = reader.peek();
            if (c == -1 || c == '\n' || c == '\r') {
                break;
            }
            image.append((char) reader.read());
        }
        return new NElementToken(image.toString(), NElementTokenType.LINE_COMMENT, "//", 0, line, column, pos,
                new NElementCommentImpl(NElementCommentType.SINGLE_LINE,image.toString())
                , null);
    }

    private NElementToken continueReadBlockComments() {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        StringBuilder image = new StringBuilder("/*");
        StringBuilder value = new StringBuilder("");
        reader.read(2); // consume /*
        while (true) {
            int c = reader.peek();
            if (c == -1) {
                break;
            }
            if (c == '*' && reader.peekChar(1) == '/') {
                image.append("*/");
                reader.read(2);
                break;
            }
            c = (char) reader.read();
            value.append((char)c);
            image.append((char)c);
        }
        return new NElementToken(image.toString(), NElementTokenType.BLOCK_COMMENT, "/*", 0, line, column, pos,
                new NElementCommentImpl(NElementCommentType.MULTI_LINE,image.toString(),value.toString())
                , null);
    }

    private NElementToken asChar123Eq(int c, NOperatorSymbol t1, NOperatorSymbol t2, NOperatorSymbol t3, NOperatorSymbol eq1, NOperatorSymbol eq2) {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        int n = reader.peekChar(1);
        if (n == c) {
            int n2 = reader.peekChar(2);
            char cc = (char) c;
            if (n2 == c) {
                reader.read(3);
                String i = new String(new char[]{cc, cc, cc});
                return new NElementToken(i, NElementTokenType.OP, i, 0, line, column, pos, t3, null);
            } else {
                reader.read(2);
                String i = new String(new char[]{cc, cc});
                return new NElementToken(i, NElementTokenType.OP, i, 0, line, column, pos, t2, null);
            }
        } else if (n == '=') {
            int n2 = reader.peekChar(2);
            char cc = (char) c;
            if (n2 == '=') {
                reader.read(3);
                String i = new String(new char[]{cc, cc, cc});
                return new NElementToken(i, NElementTokenType.OP, i, 0, line, column, pos, eq2, null);
            } else {
                reader.read(2);
                String i = new String(new char[]{cc, cc});
                return new NElementToken(i, NElementTokenType.OP, i, 0, line, column, pos, eq1, null);
            }
        } else {
            return asChar(c, t1);
        }
    }

    private NElementToken asChar123(int c, NOperatorSymbol t1, NOperatorSymbol t2, NOperatorSymbol t3) {
        int n = reader.peekChar(1);
        if (n == c) {
            int line = reader.line();
            int column = reader.column();
            long pos = reader.pos();
            int n2 = reader.peekChar(2);
            char cc = (char) c;
            if (n2 == c) {
                reader.read(3);
                String i = new String(new char[]{cc, cc, cc});
                return new NElementToken(i, NElementTokenType.OP, i, 0, line, column, pos, t3, null);
            } else {
                reader.read(2);
                String i = new String(new char[]{cc, cc});
                return new NElementToken(i, NElementTokenType.OP, i, 0, line, column, pos, t2, null);
            }
        }
        return asChar(c, t1);
    }



    private NElementToken continueReadDoubleQuoted1() {
        StringBuilder image = new StringBuilder("\"");
        StringBuilder value = new StringBuilder();

        while (true) {
            int c = reader.peek();
            if (c == -1) {
                // EOF without closing "
                break;
            }
            if (c == '"') {
                // Found closing "
                reader.read(); // consume "
                image.append('"');
                break;
            }
            if (c == '\\') {
                reader.read(); // consume \
                image.append('\\');

                // Peek next char
                int c2 = reader.peek();
                if (c2 == '"') {
                    // Escaped quote: \"
                    reader.read(); // consume "
                    image.append('"');
                    value.append('"'); // unescaped in value
                } else {
                    // Literal backslash + char
                    if (c2 != -1) {
                        char ch = (char) reader.read();
                        image.append(ch);
                        value.append('\\').append(ch);
                    } else {
                        // \ at EOF
                        value.append('\\');
                    }
                }
            } else {
                // Regular char
                char ch = (char) reader.read();
                image.append(ch);
                value.append(ch);
            }
        }

        return new NElementToken(
                image.toString(),
                NElementTokenType.DOUBLE_QUOTED_STRING,
                "\"",
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        NElementType.DOUBLE_QUOTED_STRING,
                        value.toString(),
                        image.toString()
                ), null
        );
    }

    private NElementToken continueReadDoubleQuoted3() {
        StringBuilder image = new StringBuilder("\"\"\"");
        StringBuilder value = new StringBuilder();

        while (true) {
            // Check for \"""
            if (reader.canRead(4) &&
                    reader.peekChar(0) == '\\' &&
                    reader.peekChar(1) == '"' &&
                    reader.peekChar(2) == '"' &&
                    reader.peekChar(3) == '"') {

                reader.read();
                reader.read();
                reader.read();
                reader.read();
                image.append("\\\"\"\"");
                value.append("\"\"\"");
                continue;
            }

            // Check for """
            if (reader.canRead(3) &&
                    reader.peekChar(0) == '"' &&
                    reader.peekChar(1) == '"' &&
                    reader.peekChar(2) == '"') {

                reader.read();
                reader.read();
                reader.read();
                image.append("\"\"\"");
                break;
            }

            // Regular char
            int c = reader.peek();
            if (c == -1) break;
            char ch = (char) reader.read();
            image.append(ch);
            value.append(ch);
        }

        return new NElementToken(
                image.toString(),
                NElementTokenType.TRIPLE_DOUBLE_QUOTED_STRING,
                "\"\"\"",
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        NElementType.TRIPLE_DOUBLE_QUOTED_STRING,
                        value.toString(),
                        image.toString()
                ), null
        );
    }

    private NElementToken continueReadSingleQuoted1() {
        StringBuilder image = new StringBuilder("'");
        StringBuilder value = new StringBuilder();

        while (true) {
            int c = reader.peek();
            if (c == -1) {
                // EOF without closing "
                break;
            }
            if (c == '\'') {
                // Found closing "
                reader.read(); // consume "
                image.append('\'');
                break;
            }
            if (c == '\\') {
                reader.read(); // consume \
                image.append('\\');

                // Peek next char
                int c2 = reader.peek();
                if (c2 == '\'') {
                    // Escaped quote: \"
                    reader.read(); // consume "
                    image.append('\'');
                    value.append('\''); // unescaped in value
                } else {
                    // Literal backslash + char
                    if (c2 != -1) {
                        char ch = (char) reader.read();
                        image.append(ch);
                        value.append('\\').append(ch);
                    } else {
                        // \ at EOF
                        value.append('\\');
                    }
                }
            } else {
                // Regular char
                char ch = (char) reader.read();
                image.append(ch);
                value.append(ch);
            }
        }

        return new NElementToken(
                image.toString(),
                NElementTokenType.SINGLE_QUOTED_STRING,
                "'",
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        NElementType.SINGLE_QUOTED_STRING,
                        value.toString(),
                        image.toString()
                ), null
        );
    }

    private NElementToken continueReadSingleQuoted3() {
        StringBuilder image = new StringBuilder("'''");
        StringBuilder value = new StringBuilder();

        while (true) {
            // Check for \"""
            if (reader.canRead(4) &&
                    reader.peekChar(0) == '\\' &&
                    reader.peekChar(1) == '\'' &&
                    reader.peekChar(2) == '\'' &&
                    reader.peekChar(3) == '\'') {

                reader.read();
                reader.read();
                reader.read();
                reader.read();
                image.append("\\'");
                value.append("'");
                continue;
            }

            // Check for """
            if (reader.canRead(3) &&
                    reader.peekChar(0) == '\'' &&
                    reader.peekChar(1) == '\'' &&
                    reader.peekChar(2) == '\'') {

                reader.read();
                reader.read();
                reader.read();
                image.append("'''");
                break;
            }

            // Regular char
            int c = reader.peek();
            if (c == -1) break;
            char ch = (char) reader.read();
            image.append(ch);
            value.append(ch);
        }

        return new NElementToken(
                image.toString(),
                NElementTokenType.TRIPLE_SINGLE_QUOTED_STRING,
                "'''",
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        NElementType.TRIPLE_SINGLE_QUOTED_STRING,
                        value.toString(),
                        image.toString()
                ), null
        );
    }

    private NElementToken continueReadBacktick1() {
        StringBuilder image = new StringBuilder("`");
        StringBuilder value = new StringBuilder();

        while (true) {
            int c = reader.peek();
            if (c == -1) {
                // EOF without closing "
                break;
            }
            if (c == '`') {
                // Found closing "
                reader.read(); // consume "
                image.append('`');
                break;
            }
            if (c == '\\') {
                reader.read(); // consume \
                image.append('\\');

                // Peek next char
                int c2 = reader.peek();
                if (c2 == '`') {
                    // Escaped quote: \"
                    reader.read(); // consume "
                    image.append('`');
                    value.append('`'); // unescaped in value
                } else {
                    // Literal backslash + char
                    if (c2 != -1) {
                        char ch = (char) reader.read();
                        image.append(ch);
                        value.append('\\').append(ch);
                    } else {
                        // \ at EOF
                        value.append('\\');
                    }
                }
            } else {
                // Regular char
                char ch = (char) reader.read();
                image.append(ch);
                value.append(ch);
            }
        }

        return new NElementToken(
                image.toString(),
                NElementTokenType.BACKTICK_STR,
                "`",
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        NElementType.BACKTICK_STRING,
                        value.toString(),
                        image.toString()
                ), null
        );
    }

    private NElementToken continueReadBacktick3() {
        StringBuilder image = new StringBuilder("```");
        StringBuilder value = new StringBuilder();

        while (true) {
            // Check for \```
            if (reader.canRead(4) &&
                    reader.peekChar(0) == '\\' &&
                    reader.peekChar(1) == '`' &&
                    reader.peekChar(2) == '`' &&
                    reader.peekChar(3) == '`') {

                reader.read();
                reader.read();
                reader.read();
                reader.read();
                image.append("\\'");
                value.append("'");
                continue;
            }

            // Check for """
            if (reader.canRead(3) &&
                    reader.peekChar(0) == '`' &&
                    reader.peekChar(1) == '`' &&
                    reader.peekChar(2) == '`') {

                reader.read();
                reader.read();
                reader.read();
                image.append("```");
                break;
            }

            // Regular char
            int c = reader.peek();
            if (c == -1) break;
            char ch = (char) reader.read();
            image.append(ch);
            value.append(ch);
        }

        return new NElementToken(
                image.toString(),
                NElementTokenType.TRIPLE_BACKTICK_STRING,
                "```",
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new DefaultNStringElement(
                        NElementType.TRIPLE_BACKTICK_STRING,
                        value.toString(),
                        image.toString()
                ), null
        );
    }

    private NElementToken continueReadUserSingleLine() {
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

        return new NElementToken(
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

    private NElementToken continueReadUserMultiLine() {
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
            if (reader.peekChar(0) != '¶' || reader.peekChar(1) != '¶') {
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

        return new NElementToken(
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


    private NElementToken continueReadIdentifier() {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        StringBuilder image = new StringBuilder();
        while (true) {
            int c = reader.peek();
            if (c == -1) {
                break;
            }
            if (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '$' || c == 'ω' || c == 'π') {
                image.append((char) reader.read());
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
        return new NElementToken(s, type, "", 0, line, column, pos, value, null);
    }

    private NElementToken continueReadNumber() {
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
            int next = reader.peekChar(1);
            String constantName = getConstantName(1);
            if (constantName != null) {
                // image already has the sign +/-
                image.append((char) reader.read()); // '0'
                image.append(reader.read(constantName.length()));
                return readSpecialLiteralConst(constantName,image, line,column,pos);
            }
            if (next == 's' || next == 'u') {
                return readSpecialLiteral(image, line, column, pos);
            }
        }

        // 3. Normal Number Path (Decimal, Hex, Complex)
        return readNormalNumber(image, line, column, pos);
    }



    private String getConstantName(int offset) {
        if (reader.peekChar(offset) == 'm' || reader.peekChar(offset) == 'M') {
            if ((reader.peekChar(offset + 1) == 'a' || reader.peekChar(offset + 1) == 'A') && (reader.peekChar(offset + 2) == 'x' || reader.peekChar(offset + 2) == 'X')) return "max";
            if ((reader.peekChar(offset + 1) == 'i' || reader.peekChar(offset + 1) == 'I') && (reader.peekChar(offset + 2) == 'n' || reader.peekChar(offset + 2) == 'N')) return "min";
        }
        if ((reader.peekChar(offset) == 'p' || reader.peekChar(offset) == 'P') && (reader.peekChar(offset + 1) == 'i' || reader.peekChar(offset + 1) == 'I') && (reader.peekChar(offset + 2) == 'n' || reader.peekChar(offset + 2) == 'N') && (reader.peekChar(offset + 3) == 'f' || reader.peekChar(offset + 3) == 'F')) return "pinf";
        if ((reader.peekChar(offset) == 'n' || reader.peekChar(offset) == 'N')) {
            if ((reader.peekChar(offset + 1) == 'i' || reader.peekChar(offset + 1) == 'I') && (reader.peekChar(offset + 2) == 'n' || reader.peekChar(offset + 2) == 'N') && (reader.peekChar(offset + 3) == 'f' || reader.peekChar(offset + 3) == 'F')) return "ninf";
            if ((reader.peekChar(offset + 1) == 'a' || reader.peekChar(offset + 1) == 'A') && (reader.peekChar(offset + 2) == 'n' || reader.peekChar(offset + 2) == 'N')) return "nan";
        }
        return null;
    }

    private NElementToken readSpecialLiteral(StringBuilder image, int line, int col, long pos) {
        TSONNumberInfo info = new TSONNumberInfo();
        info.specialNumber = true;

        // image already has the sign +/-
        image.append((char) reader.read()); // '0'

        // Capture Keyword (max, min, pinf, ninf, nan)

        // Optional underscore
        if (reader.peek() == '_') {
            image.append((char) reader.read());
        }

        // Capture Type (s8, s16, s32, s64, sN, u8, u16, u32, u64, f32, f64)
        int p = reader.peek();
        if (p == 'u' || p == 's') {
            char typeChar = (char) reader.read();
            image.append(typeChar);
            info.unsignedNumber = (typeChar == 'u');
            info.floatingNumber = (typeChar == 'f');

            if (reader.peek() == 'N') {
                image.append((char) reader.read());
                info.bitWidth = -1; // BigNum
            } else {
                StringBuilder widthBuf = new StringBuilder();
                while (true) {
                    int nextChar = reader.peek();
                    if (nextChar >= '0' && nextChar <= '9') {
                        char digit = (char) reader.read();
                        widthBuf.append(digit);
                        image.append(digit);
                    } else {
                        break;
                    }
                }
                if (widthBuf.length() > 0) {
                    info.bitWidth = Integer.parseInt(widthBuf.toString());
                }
            }
        }

        // Capture Suffix
        int suffixStart = image.length();
        consumeUnit(image);
        info.suffix = image.substring(suffixStart);

        NPrimitiveElementBuilder pb = NElement.ofPrimitiveBuilder();
        pb.numberLayout(NNumberLayout.BINARY);
        if (info.suffix != null && !info.suffix.isEmpty()) {
            pb.numberSuffix(info.suffix);
        }

        if (info.baseValue == null) {
            // This was the old 0s32 style (no constant name)
            // We'll treat it as 0
            if (info.unsignedNumber) {
                switch (info.bitWidth) {
                    case 8: pb.setShort((short) 0); break;
                    case 16: pb.setInt(0); break;
                    case 32: pb.setLong(0L); break;
                    case 64: pb.setBigInt(BigInteger.ZERO); break;
                }
            } else {
                switch (info.bitWidth) {
                    case 8: pb.setByte((byte) 0); break;
                    case 16: pb.setShort((short) 0); break;
                    case 32: pb.setInt(0); break;
                    case 64: pb.setLong(0L); break;
                    case -1: pb.setBigInt(BigInteger.ZERO); break;
                }
            }
        } else {
            applyConstantValue(pb, info);
        }

        return new NElementToken(image.toString(), NElementTokenType.NUMBER,
                image.toString(), 0, line, col, pos, pb.build(), null);
    }
    private NElementToken readSpecialLiteralConst(String constName,StringBuilder image, int line, int col, long pos) {
        TSONNumberInfo info = new TSONNumberInfo();
        info.specialNumber = true;
        info.specialConst = true;
        // Capture Keyword (max, min, pinf, ninf, nan)
        info.baseValue = constName.toLowerCase();
        // Optional underscore
        if (reader.peek() == '_') {
            image.append((char) reader.read());
        }

        // Capture Type (s8, s16, s32, s64, sN, u8, u16, u32, u64, f32, f64)
        int p = reader.peek();
        if (p == 'u' || p == 's' || p == 'f') {
            char typeChar = (char) reader.read();
            image.append(typeChar);
            info.unsignedNumber = (typeChar == 'u');
            info.floatingNumber = (typeChar == 'f');

            if (reader.peek() == 'N') {
                image.append((char) reader.read());
                info.bitWidth = -1; // BigNum
            } else {
                StringBuilder widthBuf = new StringBuilder();
                while (true) {
                    int nextChar = reader.peek();
                    if (nextChar >= '0' && nextChar <= '9') {
                        char digit = (char) reader.read();
                        widthBuf.append(digit);
                        image.append(digit);
                    } else {
                        break;
                    }
                }
                if (widthBuf.length() > 0) {
                    info.bitWidth = Integer.parseInt(widthBuf.toString());
                }
            }
        }

        // Capture Suffix
        int suffixStart = image.length();
        consumeUnit(image);
        info.suffix = image.substring(suffixStart);

        NPrimitiveElementBuilder pb = NElement.ofPrimitiveBuilder();
        pb.numberLayout(NNumberLayout.DECIMAL);
        if (info.suffix != null && !info.suffix.isEmpty()) {
            pb.numberSuffix(info.suffix);
        }
        applyConstantValue(pb, info);
        return new NElementToken(image.toString(), NElementTokenType.NUMBER,
                image.toString(), 0, line, col, pos, pb.build(), null);
    }

    private void applyConstantValue(NPrimitiveElementBuilder pb, TSONNumberInfo info) {
        String name = info.baseValue;
        if (info.floatingNumber) {
            if (info.bitWidth == 32) {
                switch (name) {
                    case "max": pb.setFloat(Float.MAX_VALUE); break;
                    case "min": pb.setFloat(Float.MIN_VALUE); break;
                    case "pinf": pb.setFloat(Float.POSITIVE_INFINITY); break;
                    case "ninf": pb.setFloat(Float.NEGATIVE_INFINITY); break;
                    case "nan": pb.setFloat(Float.NaN); break;
                }
            } else {
                switch (name) {
                    case "max": pb.setDouble(Double.MAX_VALUE); break;
                    case "min": pb.setDouble(Double.MIN_VALUE); break;
                    case "pinf": pb.setDouble(Double.POSITIVE_INFINITY); break;
                    case "ninf": pb.setDouble(Double.NEGATIVE_INFINITY); break;
                    case "nan": pb.setDouble(Double.NaN); break;
                }
            }
            return;
        }

        if (info.unsignedNumber) {
            switch (info.bitWidth) {
                case 8:
                    if ("max".equals(name) || "pinf".equals(name)) pb.setShort((short) 255);
                    else if ("min".equals(name) || "ninf".equals(name)) pb.setShort((short) 0);
                    break;
                case 16:
                    if ("max".equals(name) || "pinf".equals(name)) pb.setInt(65535);
                    else if ("min".equals(name) || "ninf".equals(name)) pb.setInt(0);
                    break;
                case 32:
                    if ("max".equals(name) || "pinf".equals(name)) pb.setLong(4294967295L);
                    else if ("min".equals(name) || "ninf".equals(name)) pb.setLong(0L);
                    break;
                case 64:
                    if ("max".equals(name) || "pinf".equals(name)) pb.setBigInt(new BigInteger("18446744073709551615"));
                    else if ("min".equals(name) || "ninf".equals(name)) pb.setBigInt(BigInteger.ZERO);
                    break;
            }
        } else {
            switch (info.bitWidth) {
                case -2: // no type defined
                {
                    switch (name) {
                        case "max": {
                            pb.setInt(Integer.MAX_VALUE);
                            break;
                        }
                        case "min": {
                            pb.setInt(Integer.MIN_VALUE);
                            break;
                        }
                        case "nan": {
                            pb.setDouble(Double.NaN);
                            break;
                        }
                        case "pinf": {
                            pb.setDouble(Double.POSITIVE_INFINITY);
                            break;
                        }
                        case "ninf": {
                            pb.setDouble(Double.NEGATIVE_INFINITY);
                            break;
                        }
                    }
                    break;
                }
                case 8:
                    if ("max".equals(name) || "pinf".equals(name)) pb.setByte(Byte.MAX_VALUE);
                    else if ("min".equals(name) || "ninf".equals(name)) pb.setByte(Byte.MIN_VALUE);
                    break;
                case 16:
                    if ("max".equals(name) || "pinf".equals(name)) pb.setShort(Short.MAX_VALUE);
                    else if ("min".equals(name) || "ninf".equals(name)) pb.setShort(Short.MIN_VALUE);
                    break;
                case 32:
                    if ("max".equals(name) || "pinf".equals(name)) pb.setInt(Integer.MAX_VALUE);
                    else if ("min".equals(name) || "ninf".equals(name)) pb.setInt(Integer.MIN_VALUE);
                    break;
                case 64:
                    if ("max".equals(name) || "pinf".equals(name)) pb.setLong(Long.MAX_VALUE);
                    else if ("min".equals(name) || "ninf".equals(name)) pb.setLong(Long.MIN_VALUE);
                    break;
                case -1:
                    // BigInt doesn't have max/min in the same way, but we can support min=0 for unsigned?
                    // For signed BigInt, max/min are infinite.
                    break;
            }
        }
    }

    private static class TSONNumberInfo {
        public boolean specialConst;      // e.g. "10" or "0x1A"
        public String baseValue;      // e.g. "10" or "0x1A"
        public String imaginaryValue; // e.g. "5" (if complex)
        public int bitWidth = -2;     // e.g. 32, 64, -1 is big decimal, -2 is unknown yet
        public boolean unsignedNumber;    // true if 'u'
        public boolean floatingNumber;    // true if 'u'
        public String suffix;           // e.g. "ohm"
        public boolean specialNumber;     // true if inf/nan
    }

    private boolean isI(int c) {
        return c == 'i' || c == 'î';
    }

    private NElementToken readNormalNumber(StringBuilder image, int line, int col, long pos) {
        TSONNumberInfo info = new TSONNumberInfo();
        String errorMessage = null;

        // --- 1. BASE DETECTION ---
        boolean isDecimal = true;
        NNumberLayout layout = NNumberLayout.DECIMAL;
        if (reader.peek() == '0') {
            int next = reader.peekChar(1);
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

            if (isI(reader.peek())) {
                image.append((char) reader.read());
            }
        } else if (isDecimal && isI(p)) {
            // Pure imaginary case: 5i
            info.imaginaryValue = info.baseValue;
            info.baseValue = "0";
            image.append((char) reader.read());
        }

        // --- 4. TYPE SUFFIX (The Lock) ---
        p = reader.peek();
        if (p == 'u' || p == 's') {
            info.unsignedNumber = (p == 'u');
            image.append((char) reader.read());

            StringBuilder widthBuf = new StringBuilder();
            while (Character.isDigit(reader.peek())) {
                char digit = (char) reader.read();
                widthBuf.append(digit);
                image.append(digit);
            }
            if (widthBuf.length() > 0) {
                try {
                    info.bitWidth = Integer.parseInt(widthBuf.toString());
                } catch (Exception ex) {
                    errorMessage = NExceptions.getErrorMessage(ex);
                    info.bitWidth = 32;
                }
            }
        } else if (p == 'n' || p == 'N') {
            image.append((char) reader.read());
            info.bitWidth = -1; // Marker for BigNum
        } else {
            if (info.floatingNumber) {
                info.bitWidth = 64; // default to double
            } else {
                info.bitWidth = 32; // default to int
            }
        }

        // --- 5. UNIT ---
        int suffixStart = image.length();
        consumeUnit(image);
        info.suffix = image.substring(suffixStart);

        NPrimitiveElementBuilder pb = NElement.ofPrimitiveBuilder();
        pb.numberLayout(layout);
        if (info.suffix != null) {
            pb.numberSuffix(info.suffix);
        }

        if (info.imaginaryValue != null) {
            if (info.bitWidth == -1) {
                try {
                    pb.setBigComplex(new NBigComplex(new BigDecimal(info.baseValue), new BigDecimal(info.imaginaryValue)));
                } catch (Exception ex) {
                    errorMessage = NExceptions.getErrorMessage(ex);
                    pb.setBigComplex(NBigComplex.ZERO);
                }
            } else if (info.bitWidth <= 32) {
                try {
                    pb.setFloatComplex(new NFloatComplex(Float.parseFloat(info.baseValue), Float.parseFloat(info.imaginaryValue)));
                } catch (Exception ex) {
                    errorMessage = NExceptions.getErrorMessage(ex);
                    pb.setFloatComplex(NFloatComplex.ZERO);
                }
            } else {
                try {
                    pb.setDoubleComplex(new NDoubleComplex(Double.parseDouble(info.baseValue), Double.parseDouble(info.imaginaryValue)));
                } catch (Exception ex) {
                    errorMessage = NExceptions.getErrorMessage(ex);
                    pb.setDoubleComplex(NDoubleComplex.ZERO);
                }
            }
        } else {
            if (info.unsignedNumber) {
                BigInteger bi;
                try {
                    bi = parseBigInteger(info.baseValue, layout);
                } catch (Exception ex) {
                    errorMessage = NExceptions.getErrorMessage(ex);
                    bi = BigInteger.ZERO;
                }
                switch (info.bitWidth) {
                    case 8:
                        pb.setShort(bi.shortValue());
                        break;
                    case 16:
                        pb.setInt(bi.intValue());
                        break;
                    case 32:
                        pb.setLong(bi.longValue());
                        break;
                    case 64:
                    default:
                        pb.setBigInt(bi);
                        break;
                }
            } else {
                if (info.bitWidth == -1) {
                    if (info.baseValue.contains(".") || info.baseValue.contains("e") || info.baseValue.contains("E")) {
                        try {
                            pb.setBigDecimal(new BigDecimal(info.baseValue));
                        } catch (Exception ex) {
                            errorMessage = NExceptions.getErrorMessage(ex);
                            pb.setBigDecimal(new BigDecimal(0));
                        }
                    } else {
                        try {
                            pb.setBigInt(new BigInteger(info.baseValue));
                        } catch (Exception ex) {
                            errorMessage = NExceptions.getErrorMessage(ex);
                            pb.setBigInt(BigInteger.ZERO);
                        }
                    }
                } else {
                    switch (info.bitWidth) {
                        case 8: {
                            try {
                                pb.setByte(Byte.parseByte(info.baseValue));
                            } catch (Exception ex) {
                                errorMessage = NExceptions.getErrorMessage(ex);
                                pb.setByte((byte) 0);
                            }
                            break;
                        }
                        case 16: {
                            try {
                                pb.setShort(Short.parseShort(info.baseValue));
                            } catch (Exception ex) {
                                errorMessage = NExceptions.getErrorMessage(ex);
                                pb.setShort((short) 0);
                            }
                            break;
                        }
                        case 32:
                            if (info.baseValue.contains(".") || info.baseValue.contains("e") || info.baseValue.contains("E")) {
                                try {
                                    pb.setFloat(Float.parseFloat(info.baseValue));
                                } catch (Exception ex) {
                                    errorMessage = NExceptions.getErrorMessage(ex);
                                    pb.setFloat(0.0f);
                                }

                            } else {
                                try {
                                    pb.setInt(Integer.parseInt(info.baseValue));
                                } catch (Exception ex) {
                                    errorMessage = NExceptions.getErrorMessage(ex);
                                    pb.setInt(0);
                                }
                            }
                            break;
                        case 64:
                        default:
                            if (info.baseValue.contains(".") || info.baseValue.contains("e") || info.baseValue.contains("E")) {
                                try {
                                    pb.setDouble(Double.parseDouble(info.baseValue));
                                } catch (Exception ex) {
                                    errorMessage = NExceptions.getErrorMessage(ex);
                                    pb.setDouble(0.0);
                                }
                            } else {
                                try {
                                    pb.setLong(Long.parseLong(info.baseValue));
                                } catch (Exception ex) {
                                    errorMessage = NExceptions.getErrorMessage(ex);
                                    pb.setLong(0L);
                                }
                            }
                            break;
                    }
                }
            }
        }
        return new NElementToken(image.toString(), NElementTokenType.NUMBER, "", 0, line, col, pos, pb.build(), errorMessage);
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
        int c = reader.peekChar(offset);

        // Skip digits, underscores, and dots
        while (Character.isDigit(c) || c == '.' || c == '_') {
            offset++;
            c = reader.peekChar(offset);
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
                int next = reader.peekChar(1);
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
            } else if (!acceptFloating && isHexDigit(c)) {
                sb.append((char) reader.read());
            } else {
                break;
            }
        }
        return wasFloating;
    }

    private void consumeUnit(StringBuilder sb) {
        while (true) {
            int p = reader.peek();
            if (p == '%' || p == '_' || Character.isAlphabetic(p)) {
                sb.append((char) reader.read());
            } else {
                break;
            }
        }
    }

    public static boolean isHexDigit(int current) {
        return current >= '0' && current <= '9'
                || current >= 'a' && current <= 'f'
                || current >= 'A' && current <= 'F';
    }


    private NElementToken continueReadTemporalOrNumber() {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        if (reader.canRead(10)
                && Character.isDigit(reader.peekChar(0))
                && Character.isDigit(reader.peekChar(1))
                && Character.isDigit(reader.peekChar(2))
                && Character.isDigit(reader.peekChar(3))
                && '-' == reader.peekChar(4)
                && Character.isDigit(reader.peekChar(5))
                && Character.isDigit(reader.peekChar(6))
                && '-' == reader.peekChar(7)
                && Character.isDigit(reader.peekChar(8))
                && Character.isDigit(reader.peekChar(9))
        ) {
            if (reader.canRead(16)
                    && ((reader.peekChar(10) == ' ' || reader.peekChar(10) == 'T')
                    && Character.isDigit(reader.peekChar(11))
                    && Character.isDigit(reader.peekChar(12))
                    && ':' == reader.peekChar(13)
                    && Character.isDigit(reader.peekChar(14))
                    && Character.isDigit(reader.peekChar(15))
            )) {
                if (reader.canRead(19)
                        && (reader.peekChar(16) == ':')
                        && Character.isDigit(reader.peekChar(17))
                        && Character.isDigit(reader.peekChar(18))
                ) {
                    int h = 19;
                    if (reader.canRead(h + 1) && reader.peekChar(h) == '.') {
                        h++;
                        while (reader.canRead(h + 1) && Character.isDigit(reader.peekChar(h))) {
                            h++;
                        }
                    }
                    if (reader.canRead(h + 1) && reader.peekChar(h) == 'Z') {
                        h++;
                        String str = reader.read(h);
                        Instant i = Instant.parse(str);
                        return new NElementToken(str, NElementTokenType.INSTANT, "", 0, line, column, pos,
                                new DefaultNPrimitiveElement(NElementType.INSTANT, i, null, null)

                                , null);
                    }
                    String str = reader.read(h);
                    LocalDateTime i = LocalDateTime.parse(str.replace(' ', 'T'));
                    return new NElementToken(str, NElementTokenType.DATETIME, "", 0, line, column, pos,
                            //should include raw image as well
                            new DefaultNPrimitiveElement(NElementType.LOCAL_DATETIME, i, null, null)
                            , null);
                } else {
                    String str = reader.read(16);
                    LocalDateTime i = LocalDateTime.parse(str.replace(' ', 'T'));
                    return new NElementToken(str, NElementTokenType.DATETIME, "", 0, line, column, pos,
                            new DefaultNPrimitiveElement(NElementType.LOCAL_DATETIME, i, null, null)

                            , null);
                }
            } else {
                String str = reader.read(10);
                LocalDate i = LocalDate.parse(str);
                return new NElementToken(str, NElementTokenType.DATE, "", 0, line, column, pos,
                        new DefaultNPrimitiveElement(NElementType.LOCAL_DATE, i, null, null)

                        , null);
            }
        } else if (reader.canRead(8)
                && Character.isDigit(reader.peekChar(0))
                && Character.isDigit(reader.peekChar(1))
                && ':' == reader.peekChar(2)
                && Character.isDigit(reader.peekChar(3))
                && Character.isDigit(reader.peekChar(4))
                && ':' == reader.peekChar(5)
                && Character.isDigit(reader.peekChar(6))
                && Character.isDigit(reader.peekChar(7))
        ) {
            int h = 8;
            if (reader.canRead(h + 1) && reader.peekChar(h) == '.') {
                h++;
                while (reader.canRead(h + 1) && Character.isDigit(reader.peekChar(h))) {
                    h++;
                }
            }
            String str = reader.read(h);
            LocalTime i = LocalTime.parse(str);
            return new NElementToken(str, NElementTokenType.TIME, "", 0, line, column, pos,
                    new DefaultNPrimitiveElement(NElementType.LOCAL_TIME, i, null, null)
                    , null);
        }
        return continueReadNumber();
    }

    private NElementToken continueReadRepeatedChar(int c, NElementTokenType tt) {
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
        return new NElementToken(s, tt, String.valueOf(s.charAt(0)), count, reader.line(), reader.column(), reader.pos(), s, null);
    }

    private NElementToken asChar(int c, NElementTokenType tt) {
        String image = String.valueOf((char) c);
        reader.read();
        return new NElementToken(image, tt, image, 0, reader.line(), reader.column(), reader.pos(), c, null);
    }

    private NElementToken asChar(int c, NOperatorSymbol tt) {
        String image = String.valueOf((char) c);
        reader.read();
        return new NElementToken(image, NElementTokenType.OP, image, 0, reader.line(), reader.column(), reader.pos(), tt, null);
    }
}
