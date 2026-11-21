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

final class TsonStringLexer {
    private final NPositionedCharReader reader;

    TsonStringLexer(NPositionedCharReader reader) {
        this.reader = reader;
    }

    public NElementTokenImpl readFencedString(int c, int line, int column, long pos) {
        char c0 = (char) c;
        int len = 0;
        while (reader.peekAt(len) == c0) {
            len++;
        }
        NElementTokenType tt;
        NElementType et;
        switch (c) {
            case '"': {
                tt = NElementTokenType.DOUBLE_QUOTED_STRING;
                et = NElementType.DOUBLE_QUOTED_STRING;
                break;
            }
            case '\'': {
                tt = NElementTokenType.SINGLE_QUOTED_STRING;
                et = NElementType.SINGLE_QUOTED_STRING;
                break;
            }
            default: {
                tt = NElementTokenType.BACKTICK_STRING;
                et = NElementType.BACKTICK_STRING;
                break;
            }
        }
        if (len == 2) {
            // A run of exactly 2 quote chars is an empty string (open, immediately
            // closed, empty content), not an N=2 fence.
            reader.read(2);
            String image = new String(new char[]{c0, c0});
            return new NElementTokenImpl(image, tt, image, 0, line, column, pos, "", null, 1);
        }
        if (len < 2) {
            return readFenced(c0, 1, tt, et, line, column, pos);
        }
        switch (c) {
            case '"': {
                return readFenced(c0, len, NElementTokenType.TRIPLE_DOUBLE_QUOTED_STRING, NElementType.TRIPLE_DOUBLE_QUOTED_STRING, line, column, pos);
            }
            case '\'': {
                return readFenced(c0, len, NElementTokenType.TRIPLE_SINGLE_QUOTED_STRING, NElementType.TRIPLE_SINGLE_QUOTED_STRING, line, column, pos);
            }
            default: {
                return readFenced(c0, len, NElementTokenType.TRIPLE_BACKTICK_STRING, NElementType.TRIPLE_BACKTICK_STRING, line, column, pos);
            }
        }
    }

    /**
     * Reads a fenced (quoted) string with fence length N using the generalized
     * P-run counting rule:
     * <pre>
     * P &lt; N : all P quotes are literal content, string stays open
     * P == N : the string terminates here
     * P &gt; N : P-1 literal quotes, 1 discarded (escape signal), string stays open
     * </pre>
     * Reaching EOF while the string is still open produces an error token
     * (unterminated fenced string); the reader is left at EOF and the token
     * image is the raw, undosed text.
     */
    public NElementTokenImpl readFenced(char c0, int N, NElementTokenType tokenType, NElementType elementType, int line, int column, long pos) {
        // consume the opening fence (N identical quote chars)
        for (int i = 0; i < N; i++) {
            int a = reader.read();
            if (a != c0) {
                throw new NUnexpectedException(NMsg.ofC("expected %s", String.valueOf(c0)));
            }
        }
        StringBuilder image = new StringBuilder();
        StringBuilder value = new StringBuilder();
        NMsg error = null;
        boolean closed = false;
        while (!closed) {
            int c = reader.peek();
            if (c == -1) {
                // EOF without closing fence
                error = NMsg.ofC("EOF without closing %s", String.valueOf(c0));
                break;
            }
            if (c == c0) {
                // count the maximal run P of delimiter characters at this position
                int run = 0;
                while (reader.peekAt(run) == c0) {
                    run++;
                }
                if (run == N) {
                    reader.read(N);
                    image.append(repeatChar(c0, N));
                    closed = true;
                } else if (run < N) {
                    reader.read(run);
                    value.append(repeatChar(c0, run));
                    image.append(repeatChar(c0, run));
                } else {
                    // run > N : P-1 literal quotes, 1 discarded, string stays open
                    reader.read(run);
                    value.append(repeatChar(c0, run - 1));
                    image.append(repeatChar(c0, run));
                }
            } else {
                int c2 = reader.read();
                value.append((char) c2);
                image.append((char) c2);
            }
        }
        // apply the boundary-space rule to the decoded value whether or not the
        // string was terminated, so that decoding followed by encoding is an
        // exact inverse in both cases
        trimBoundarySpaces(value, c0);
        return new NElementTokenImpl(
                repeatChar(c0, N) + image.toString(),
                tokenType,
                String.valueOf(c0),
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                value.toString(),
                error,
                N
        );
    }

    public NElementTokenImpl readQuoted(char c0, NElementTokenType elementTokenType, NElementType elementType) {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        return readFenced(c0, 1, elementTokenType, elementType, line, column, pos);
    }

    public NElementTokenImpl readTripleQuoted(char c0, NElementTokenType tokenType, NElementType elementType) {
        int line = reader.line();
        int column = reader.column();
        long pos = reader.pos();
        return readFenced(c0, 3, tokenType, elementType, line, column, pos);
    }

    /**
     * Boundary space trimming (U+0020 only, start/end of content only): at the
     * very start of content, if the maximal leading run of spaces is followed
     * by the delimiter character, exactly one space is discarded (sacrificial)
     * and the remaining ones are literal. The mirror rule applies at the very
     * end of content. Spaces adjacent to non-delimiter characters are never
     * trimmed.
     */
    private static void trimBoundarySpaces(StringBuilder value, char c0) {
        int len = value.length();
        // leading
        int lead = 0;
        while (lead < len && value.charAt(lead) == ' ') {
            lead++;
        }
        if (lead > 0 && lead < len && value.charAt(lead) == c0) {
            value.deleteCharAt(0);
        }
        // trailing
        int vlen = value.length();
        int trail = 0;
        while (trail < vlen && value.charAt(vlen - 1 - trail) == ' ') {
            trail++;
        }
        if (trail > 0 && trail < vlen && value.charAt(vlen - 1 - trail) == c0) {
            value.deleteCharAt(vlen - 1);
        }
    }

    private static String repeatChar(char c, int n) {
        if (n <= 0) {
            return "";
        }
        char[] buf = new char[n];
        Arrays.fill(buf, c);
        return new String(buf);
    }

    /**
     * Serializes a string value using fence length N (encoding section 2.1 +
     * boundary space trimming): internal delimiter runs of length k &lt; N are
     * written as-is, runs of length k &gt;= N are written as k+1 quotes, and a
     * single sacrificial space is added at the very start/end of content when
     * the content starts/ends with a delimiter character (so the literal quote
     * cannot merge into the fence). When {@code unterminated} is true, no
     * closing fence is written.
     */
    public static String toFencedString(String value, char c, int N, boolean unterminated) {
        if (!unterminated && value.indexOf(c) < 0) {
            // common case: no delimiter chars inside, so no escaping or
            // boundary-space logic is needed
            StringBuilder sb = new StringBuilder(value.length() + Math.max(0, N) * 2);
            for (int i = 0; i < N; i++) {
                sb.append(c);
            }
            sb.append(value);
            for (int i = 0; i < N; i++) {
                sb.append(c);
            }
            return sb.toString();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(repeatChar(c, N));
        if (hasQuoteBoundary(value, c, true)) {
            sb.append(' ');
        }
        sb.append(encodeStringContent(value, c, N));
        if (hasQuoteBoundary(value, c, false)) {
            sb.append(' ');
        }
        if (!unterminated) {
            sb.append(repeatChar(c, N));
        }
        return sb.toString();
    }

    /**
     * Encodes the interior of a fenced string (encoding section 2.1): internal
     * maximal delimiter runs of length k are written as k quotes when
     * k &lt; N, and as k+1 quotes when k &gt;= N (the extra quote prevents an
     * internal run from being mistaken for the closing fence). Every other
     * character is written verbatim.
     */
    public static String encodeStringContent(String value, char c, int N) {
        StringBuilder sb = new StringBuilder();
        int len = value.length();
        int i = 0;
        while (i < len) {
            char ch = value.charAt(i);
            if (ch == c) {
                int run = 0;
                while (i + run < len && value.charAt(i + run) == c) {
                    run++;
                }
                sb.append(repeatChar(c, run < N ? run : run + 1));
                i += run;
            } else {
                sb.append(ch);
                i++;
            }
        }
        return sb.toString();
    }

    private static boolean hasQuoteBoundary(String value, char c, boolean leading) {
        int i = leading ? 0 : value.length() - 1;
        int step = leading ? 1 : -1;
        int len = value.length();
        while (i >= 0 && i < len && value.charAt(i) == ' ') {
            i += step;
        }
        return i >= 0 && i < len && value.charAt(i) == c;
    }

    public NElementTokenImpl readLineString() {
        StringBuilder image = new StringBuilder();
        StringBuilder content = new StringBuilder();

        // Consume the initial '¶'
        image.append('¶');
        int c = reader.read();// consume '¶'
        NAssert.requireNamedEquals('¶', (char) c, "paragraph start");
        String line = reader.readLine();
        image.append(line);
        content.append(line);

        NNewLineMode newLine = reader.readNewLine();
        if (newLine != null) {
            image.append(newLine.value());
        }
        return new NElementTokenImpl(
                image.toString(),           // e.g., "¶ hello\n"
                NElementTokenType.LINE_STRING,
                "¶",
                0,
                reader.line(),
                reader.column(),
                reader.pos(),
                new TsonCustomLexer.LinesAndContent(
                        Collections.singletonList(new NElementLineImpl(
                                "",
                                "¶",
                                "",
                                content.toString(),
                                "",
                                "",
                                newLine
                        ))
                )
                , null
        );
    }

    private static class NewLineThenSpaces {
        NNewLineMode newLine;
        String spaces;
        String suffix;

        public NewLineThenSpaces(NNewLineMode newLine, String spaces, String suffix) {
            this.newLine = newLine;
            this.spaces = spaces;
            this.suffix = suffix;
        }
    }

    private NewLineThenSpaces newLineThenSpaceThen(String a) {
        int i = 0;
        NNewLineMode newLine;
        char[] cc = a.toCharArray();
        if (reader.peekAt(i) == '\r') {
            if (reader.peekAt(i + 1) == '\n') {
                i += 2;
                newLine = NNewLineMode.CRLF;
            } else {
                i++;
                newLine = NNewLineMode.CR;
            }
        } else if (reader.peekAt(i) == '\n') {
            newLine = NNewLineMode.LF;
            i++;
        } else {
            return null;
        }
        while (true) {
            int c = reader.peekAt(i);
            if (c == -1 || c == '\n' || c == '\r') {
                return null;
            }
            if (c == ' ' || c == '\t') {
                i++;
            } else {
                if (reader.canRead(cc.length)) {
                    for (int j = 0; j < cc.length; j++) {
                        if (cc[j] != reader.peekAt(i)) {
                            return null;
                        }
                        i++;
                    }
                    //skip newline
                    int nll = newLine.value().length();
                    reader.read(nll);
                    int remaining = i - nll - cc.length;
                    String spaces = "";
                    if (remaining > 0) {
                        spaces = reader.read(remaining);
                    }
                    String suffix = reader.read(cc.length);
                    return new NewLineThenSpaces(newLine, spaces, suffix);
                } else {
                    return null;
                }
            }
        }
    }


    public NElementTokenImpl readBlockString() {
        return readMultiLineString("¶¶", NElementTokenType.BLOCK_STRING);
    }

    public NElementTokenImpl readMultiLineString(String marker, NElementTokenType tokenType) {
        int line0 = reader.line();
        int column0 = reader.column();
        long pos0 = reader.pos();
        StringBuilder image = new StringBuilder();
        image.append(marker);
        String s = reader.read(2);// consume /*
        NAssert.requireNamedEquals(s, marker, "prefix");
        List<NElementLine> elems = new ArrayList<>();
        String nextPrefix = null;
        String nextMarker = marker;
        while (true) {
            // Read rest of current line
            String line = reader.readLine();
            image.append(line);
            NewLineThenSpaces cont = newLineThenSpaceThen(marker);
            if (cont != null) {
                image.append(cont.newLine.value());
                image.append(cont.spaces);
                image.append(cont.suffix);
                elems.add(new NElementLineImpl(
                        nextPrefix, nextMarker, "", line, "", "", cont.newLine
                ));
                nextMarker = marker;
                nextPrefix = cont.spaces;
            } else {
                NNewLineMode nl = reader.readNewLine();
                elems.add(new NElementLineImpl(
                        nextPrefix, nextMarker, "", line, "", "", nl
                ));
                if (nl != null) {
                    image.append(nl.value());
                }
                break;
            }
        }
        return new NElementTokenImpl(
                image.toString(),           // ← exact source
                tokenType,
                marker,
                0,
                line0,
                column0,
                pos0,
                new TsonCustomLexer.LinesAndContent(elems)
                // ← clean value
                , null
        );
    }

}
