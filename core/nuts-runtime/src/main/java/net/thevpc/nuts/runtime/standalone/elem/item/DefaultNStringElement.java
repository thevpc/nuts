package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementLineImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenType;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomLexer;
import net.thevpc.nuts.text.NLine;
import net.thevpc.nuts.text.NNewLineMode;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.elem.NElementUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultNStringElement extends DefaultNPrimitiveElement implements NStringElement {
    private String image;
    private List<NElementLine> lines;
    private List<NElementLine> lines0;
    private volatile boolean initialized;
    /**
     * fence length N of the quoted string (0 = unknown, derive from type:
     * 1 for simple quoted types, 3 for triple quoted types).
     */
    private int quotedFenceLength;
    /**
     * true when this fenced string was parsed from text that reached EOF while
     * the string was still open (no closing fence found).
     */
    private boolean quotedUnterminated;

    public int quotedFenceLength() {
        if (quotedFenceLength > 0) {
            return quotedFenceLength;
        }
        switch (type()) {
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_BACKTICK_STRING:
                return 3;
            default:
                return 1;
        }
    }

    public boolean quotedUnterminated() {
        return quotedUnterminated;
    }

    /**
     * Records the fence length N and the unterminated flag of this fenced
     * string (used when rebuilding parsed elements through builders).
     *
     * @param fenceLength fence length N (0 = derive from type)
     * @param unterminated true when the original text reached EOF while open
     * @return this
     */
    public DefaultNStringElement withQuotedFence(int fenceLength, boolean unterminated) {
        this.quotedFenceLength = fenceLength;
        this.quotedUnterminated = unterminated;
        return this;
    }

    public static DefaultNStringElement ofValue(NElementType type, String value) {
        String image = valueToImage(value, type);
        return new DefaultNStringElement(type, value, image, null, null, null, null);
    }

    public static DefaultNStringElement ofName(String value) {
        List<NElementLine> lines = Collections.singletonList(new NElementLineImpl("", "", "", value, "", "", null));
        return new DefaultNStringElement(NElementType.NAME, value, value, lines, null, null, null);
    }
    public static DefaultNStringElement ofName(String value,List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        List<NElementLine> lines = Collections.singletonList(new NElementLineImpl("", "", "", value, "", "", null));
        return new DefaultNStringElement(NElementType.NAME, value, value, lines, affixes, diagnostics, metadata);
    }

//    public static DefaultNStringElement ofImage(NElementType type, String image) {
//
//        return new DefaultNStringElement(type, image);
//    }

    public static DefaultNStringElement ofLines(NElementType type, String value,
                                                String image, List<NElementLine> lines,
                                                List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        if(image==null){
            StringBuilder sb=new StringBuilder();
            for(NElementLine l:lines){
                sb.append(l.toString());
            }
            image=sb.toString();
        }
        return new DefaultNStringElement(type, value, image, lines, affixes, diagnostics, metadata);
    }

    public static DefaultNStringElement ofNoLines(NElementType type, String value,
                                                  String image,
                                                  List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        return ofNoLines(type, value, image, 0, false, affixes, diagnostics, metadata);
    }

    public static DefaultNStringElement ofNoLines(NElementType type, String value,
                                                  String image, int fenceLength, boolean unterminated,
                                                  List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        if (image == null) {
            image = valueToImage(value, type, fenceLength);
        }
        DefaultNStringElement e = new DefaultNStringElement(type, value, image, null, affixes, diagnostics, metadata, unterminated);
        e.quotedFenceLength = fenceLength;
        e.quotedUnterminated = unterminated;
        return e;
    }

    public DefaultNStringElement(NElementType type, String value) {
        this(type, value, null, null, null, null, null);
    }

    public DefaultNStringElement(NElementType type, String value,
                                 String image, List<NElementLine> lines,
                                 List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        this(type, value, image, lines, affixes, diagnostics, metadata, false);
    }

    private DefaultNStringElement(NElementType type, String value,
                                  String image, List<NElementLine> lines,
                                  List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata,
                                  boolean skipImageSuffixChecks) {
        super(type, NAssert.requireNamedNonNull(value, "string value"), affixes, diagnostics, metadata);
        NAssert.requireNamedNonNull(value, "image value");
        switch (type) {
            case BLOCK_STRING: {
                NAssert.requireNamedTrue(image.startsWith("¶¶"), "block string");
//                char c = image.charAt(image.length() - 1);
//                NAssert.requireNamedTrue(c=='\n'||c=='\r', "block string");
                break;
            }
            case LINE_STRING: {
                NAssert.requireNamedTrue(image.startsWith("¶"), "block string");
//                char c = image.charAt(image.length() - 1);
//                NAssert.requireNamedTrue(c=='\n'||c=='\r', "block string");
                break;
            }
            case DOUBLE_QUOTED_STRING: {
                NAssert.requireNamedTrue(image.startsWith("\""), "double quoted string");
                if (!skipImageSuffixChecks) {
                    NAssert.requireNamedTrue(image.endsWith("\""), "double quoted string");
                }
                break;
            }
            case SINGLE_QUOTED_STRING: {
                NAssert.requireNamedTrue(image.startsWith("'"), "simple quoted string");
                if (!skipImageSuffixChecks) {
                    NAssert.requireNamedTrue(image.endsWith("'"), "simple quoted string");
                }
                break;
            }
            case BACKTICK_STRING: {
                NAssert.requireNamedTrue(image.startsWith("`"), "back quoted string");
                if (!skipImageSuffixChecks) {
                    NAssert.requireNamedTrue(image.endsWith("`"), "back quoted string");
                }
                break;
            }
            case TRIPLE_DOUBLE_QUOTED_STRING: {
                NAssert.requireNamedTrue(image.startsWith("\"\"\""), "triple double quoted string");
                if (!skipImageSuffixChecks) {
                    NAssert.requireNamedTrue(image.endsWith("\"\"\""), "triple double quoted string");
                }
                break;
            }
            case TRIPLE_SINGLE_QUOTED_STRING: {
                NAssert.requireNamedTrue(image.startsWith("'''"), "triple simple quoted string");
                if (!skipImageSuffixChecks) {
                    NAssert.requireNamedTrue(image.endsWith("'''"), "triple simple quoted string");
                }
                break;
            }
            case TRIPLE_BACKTICK_STRING: {
                NAssert.requireNamedTrue(image.startsWith("```"), "triple back quoted string");
                if (!skipImageSuffixChecks) {
                    NAssert.requireNamedTrue(image.endsWith("```"), "triple back quoted string");
                }
                break;
            }
        }
        this.image = image;
        this.lines0 = lines;
        if (NAssert.requireNamedNonNull(type,"type") == NElementType.NAME) {
            NAssert.requireNamedTrue(NElementUtils.isElementName(value), "valid name : " + value);
            if(this.lines==null) {
                this.lines = Collections.singletonList(new NElementLineImpl("", "", "", value, "", "", null));
            }
            if(this.image==null) {
                this.image = value;
            }
            this.initialized = true;
        }
    }


    private static String valueToImage(String any, NElementType type) {
        return valueToImage(any, type, 0);
    }

    private static String valueToImage(String any, NElementType type, int fenceLength) {
        switch (type) {
            case NAME: {
                return any;
            }
            case SINGLE_QUOTED_STRING: {
                return TsonCustomLexer.toFencedString(any, '\'', fenceLength > 0 ? fenceLength : 1, false);
            }
            case TRIPLE_SINGLE_QUOTED_STRING: {
                return TsonCustomLexer.toFencedString(any, '\'', fenceLength > 0 ? fenceLength : 3, false);
            }
            case DOUBLE_QUOTED_STRING: {
                return TsonCustomLexer.toFencedString(any, '"', fenceLength > 0 ? fenceLength : 1, false);
            }
            case TRIPLE_DOUBLE_QUOTED_STRING: {
                return TsonCustomLexer.toFencedString(any, '"', fenceLength > 0 ? fenceLength : 3, false);
            }
            case BACKTICK_STRING: {
                return TsonCustomLexer.toFencedString(any, '`', fenceLength > 0 ? fenceLength : 1, false);
            }
            case TRIPLE_BACKTICK_STRING: {
                return TsonCustomLexer.toFencedString(any, '`', fenceLength > 0 ? fenceLength : 3, false);
            }
            case LINE_STRING: {
                String[] all = any.split("\n");
                if(all.length==1 || (all.length==2 && all[all.length-1].isEmpty())){
                    return "¶ "+all[0]+'\n';
                }
                throw new NIllegalArgumentException(NMsg.ofC("line string could not include newlines"));
            }
            case BLOCK_STRING: {
                String[] all = any.split("\n");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < all.length; i++) {
                    sb.append("¶ ").append(all[i]).append("\n");
                }
                return sb.toString();
            }
        }
        return any;
    }

    private void _init() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    if(lines==null){
                        boolean checkLines = false;
                        String imageValue = image;
                        List<NElementLine> lines = lines0;
                        NElementType type = type();
                        if (imageValue != null) {
                            if (lines == null) {
                                lines = parseLinesFromImage(imageValue, type).lines;
                            } else {
                                checkLines = true;
                            }
                        } else {
                            if (lines == null) {
                                lines = parseLinesFromUser((String) value(), type);
                            }
                        }
                        if (checkLines) {
                            StringBuilder sb = new StringBuilder(imageValue.length());
                            for (NElementLine x : lines) {
                                sb.append(x.toString());
                            }
                            NAssert.requireNamedEquals(imageValue, sb.toString(), "string raw value");
                        }
                        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
                    }
                    initialized=true;
                }
            }
        }
    }

    public NNewLineMode newlineSuffix() {
        NElementType type = type();
        if (type != NElementType.BLOCK_STRING && type != NElementType.LINE_STRING) {
            return null;
        }
        return lines().get(lines().size() - 1).newline();
    }

    @Override
    public NStringElement withNewlineSuffix(NNewLineMode nNewLineMode) {
        NElementType type = type();
        if (type != NElementType.BLOCK_STRING && type != NElementType.LINE_STRING) {
            return this;
        }
        if (newlineSuffix() == nNewLineMode) {
            return this;
        }
        List<NElementLine> lines2 = new ArrayList<>(lines());
        NElementLine l = lines2.get(lines2.size() - 1);
        l = l.withNewline(nNewLineMode);
        lines2.set(lines2.size() - 1, l);
        return DefaultNStringElement.ofLines(type, NElementLineImpl.concatContent(lines2), NElementLineImpl.concatString(lines2), lines2, affixes(), diagnostics(), metadata());
    }


    @Override
    public List<NElementLine> lines() {
        _init();
        return lines;
    }

    private List<NElementLine> parseLinesFromUser(String userValue, NElementType type) {
        switch (type) {
            case BLOCK_STRING: {
                List<NLine> nLines = NLine.parseList(userValue);
                return nLines.stream().map(x -> new NElementLineImpl(
                        "", "¶¶", "", x.content(), "", "", x.newLine()
                )).collect(Collectors.toList());
            }
            case LINE_STRING: {
                List<NLine> nLines = NLine.parseList(userValue);
                if (nLines.isEmpty()) {
                    nLines.add(new NLine("", null));
                }
                if (nLines.size() > 1) {
                    throw new NIllegalArgumentException(NMsg.ofC("line string could not include newlines"));
                }
                return nLines.stream().map(x -> new NElementLineImpl(
                        "", "¶", "", x.content(), "", "", x.newLine()
                )).collect(Collectors.toList());
            }
            case SINGLE_QUOTED_STRING: {
                return parseLinesFromUserQuoted(userValue, "'");
            }
            case DOUBLE_QUOTED_STRING: {
                return parseLinesFromUserQuoted(userValue, "\"");
            }
            case BACKTICK_STRING: {
                return parseLinesFromUserQuoted(userValue, "`");
            }
            case TRIPLE_SINGLE_QUOTED_STRING: {
                return parseLinesFromUserQuoted(userValue, "'''");
            }
            case TRIPLE_DOUBLE_QUOTED_STRING: {
                return parseLinesFromUserQuoted(userValue, "\"\"\"");
            }
            case TRIPLE_BACKTICK_STRING: {
                return parseLinesFromUserQuoted(userValue, "```");
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid string : %s",
                NStringUtils.formatStringLiteral(userValue)
        ));
    }


    private TsonCustomLexer.LinesAndContent parseLinesFromImage(String image, NElementType type) {
        switch (type) {
            case BLOCK_STRING: {
                TsonCustomLexer le = new TsonCustomLexer(new StringReader(image));
                NElementTokenImpl t = le.readBlockString();
                expectNothing(le);
                return ((TsonCustomLexer.LinesAndContent) t.value());
            }
            case LINE_STRING: {
                TsonCustomLexer le = new TsonCustomLexer(new StringReader(image));
                NElementTokenImpl t = le.readLineString();
                TsonCustomLexer.LinesAndContent u = (TsonCustomLexer.LinesAndContent) t.value();
                expectNothing(le);
                return u;
            }
            case SINGLE_QUOTED_STRING: {
                return parseLinesFromImage(image, type, "'", NElementTokenType.SINGLE_QUOTED_STRING);
            }
            case DOUBLE_QUOTED_STRING: {
                return parseLinesFromImage(image, type, "\"", NElementTokenType.DOUBLE_QUOTED_STRING);
            }
            case BACKTICK_STRING: {
                return parseLinesFromImage(image, type, "`", NElementTokenType.BACKTICK_STRING);
            }
            case TRIPLE_SINGLE_QUOTED_STRING: {
                return parseLinesFromImage(image, type, "'''", NElementTokenType.TRIPLE_SINGLE_QUOTED_STRING);
            }
            case TRIPLE_DOUBLE_QUOTED_STRING: {
                return parseLinesFromImage(image, type, "\"\"\"", NElementTokenType.TRIPLE_DOUBLE_QUOTED_STRING);
            }
            case TRIPLE_BACKTICK_STRING: {
                return parseLinesFromImage(image, type, "```", NElementTokenType.TRIPLE_BACKTICK_STRING);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid string : %s",
                NStringUtils.formatStringLiteral(image)
        ));
    }

    private TsonCustomLexer.LinesAndContent parseLinesFromImage(String image, NElementType type, String quotes, NElementTokenType tokenType) {
        TsonCustomLexer le = new TsonCustomLexer(new StringReader(image));
        char c0 = quotes.charAt(0);
        // determine the actual fence length N from the raw image (it may be > 3)
        int n = 0;
        while (n < image.length() && image.charAt(n) == c0) {
            n++;
        }
        if (n <= 0) {
            n = quotes.length();
        }
        NElementTokenImpl t = le.readFenced(c0, n, tokenType, type, 0, 0, 0);
        expectNothing(le);
        char[] fenceChars = new char[n];
        Arrays.fill(fenceChars, c0);
        String fenceText = new String(fenceChars);
        List<NLine> nLines = NLine.parseList((String) t.value());
        List<NElementLine> result = new ArrayList<>();
        for (int i = 0; i < nLines.size(); i++) {
            NLine nLine = nLines.get(i);
            result.add(new NElementLineImpl(
                    "",
                    i == 0 ? fenceText : ""
                    , "", nLine.content(),
                    "",
                    (i == nLines.size() - 1) ? fenceText : "",
                    nLine.newLine()
            ));
        }
        return new TsonCustomLexer.LinesAndContent(result);
    }

    private List<NElementLine> parseLinesFromUserQuoted(String userValue, String quotes) {
        List<NElementLine> result = new ArrayList<>();
        List<NLine> nLines = NLine.parseList(userValue);
        char c0 = quotes.charAt(0);
        int fenceLength = quotes.length();
        for (int i = 0; i < nLines.size(); i++) {
            NLine nLine = nLines.get(i);
            String c = nLine.content();
            String escaped = TsonCustomLexer.encodeStringContent(c, c0, fenceLength);
            result.add(new NElementLineImpl(
                    "",
                    i == 0 ? quotes : ""
                    , "", escaped,
                    "",
                    (i == nLines.size() - 1) ? quotes : "",
                    nLine.newLine()
            ));
        }
        return result;
    }

    private void expectNothing(TsonCustomLexer le) {
        while (true) {
            NElementTokenImpl n = le.next();
            if (n == null) {
                return;
            }
            if (
                    n.type() == NElementTokenType.SPACE
                            || n.type() == NElementTokenType.NEWLINE
            ) {
                //just ignore
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("unexpected %s", n.image()));
            }
        }
    }

    public DefaultNStringElement(NElementType type, Character value) {
        this(type, value, null, null, null);
    }

    public DefaultNStringElement(NElementType type, Character value,
                                 List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics, NElementMetadata metadata) {
        super(type, value, affixes, diagnostics, metadata);
        if (type != NElementType.CHAR) {
            throw new NIllegalArgumentException(NMsg.ofC("expected character"));
        }
    }

    public NNewLineMode newLineSuffix() {
        return lines().get(lines().size() - 1).newline();
    }

    @Override
    public String stringValue() {
        String s = (String) value();
        return s == null ? "" : s;
    }

    @Override
    public String rawValue() {
        switch (type()){
            case NAME:
                return image;
            case SINGLE_QUOTED_STRING:
            case DOUBLE_QUOTED_STRING:
            case BACKTICK_STRING:
                if (quotedUnterminated) {
                    return image.substring(1);
                }
                return image.substring(1, image.length() - 1);
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_BACKTICK_STRING: {
                int n = quotedFenceLength();
                if (quotedUnterminated) {
                    return image.substring(n);
                }
                return image.substring(n, image.length() - n);
            }
            case LINE_STRING:{
                return image.substring(1);
            }
            case BLOCK_STRING:{
                return lines().stream().map(x -> x.toString()).collect(Collectors.joining());
            }
        }
        return image;
    }

    public String image() {
        return image;
    }

    @Override
    public NOptional<NStringElement> asString() {
        return NOptional.of(this);
    }

    @Override
    public String literalString() {
        return asLiteral().toStringLiteral();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNStringElement that = (DefaultNStringElement) o;
        return
                Objects.equals(value(), that.value())
                && Objects.equals(type(), that.type())
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value(), type());
    }
}
