package net.thevpc.nuts.runtime.standalone.text.parser.v2;

import net.thevpc.nuts.runtime.standalone.collections.DefaultNCharQueue;
import net.thevpc.nuts.runtime.standalone.text.AbstractNTextNodeParser;
import net.thevpc.nuts.runtime.standalone.text.NTextNodeCollector;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextCode;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.util.NMatchType;
import net.thevpc.nuts.util.NStringMatchResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * MTF (Markdown/Text Format) parser. MTF is a <b>hybrid</b> format supporting both the
 * {@code NTF} (nuts text format, see {@code ntf-help.ntf}) syntax and a subset of the
 * {@code markdown} syntax.
 *
 * <p>Supported NTF concepts (see {@code ntf-help.ntf} for the complete reference):
 * <ul>
 *     <li>{@code ##Text##}, {@code ###Text###}, ... primary styles (level 1..15).
 *     {@code #Text#} is plain text as documented in {@code ntf-help.ntf}</li>
 *     <li>{@code ##:style:Text##} simple styles</li>
 *     <li>{@code ##{style:Text##:other:Text2##}##} composite styles</li>
 *     <li>{@code #) Text}, {@code ##) Text}, ... titles</li>
 *     <li>{@code ```Text```} verbatim text and {@code ```lang code```} formatted code</li>
 *     <li>{@code \} escape sequences and the {@code \u001E} 'silent' separator</li>
 * </ul>
 *
 * <p>Supported markdown concepts:
 * <ul>
 *     <li>{@code *italic*}</li>
 *     <li>{@code **bold**} and {@code ***bold italic***}</li>
 *     <li>{@code `inline code`}</li>
 *     <li>{@code [[lang: source]]} embedded sources, parsed as an {@link NTextCode}</li>
 *     <li>{@code \*}, {@code \`} and {@code \[} escaped characters</li>
 * </ul>
 *
 * <p>Markdown spans never span multiple lines and require a non blank content. Anything
 * that cannot be parsed is kept as plain text so that highlighting a document never
 * loses any content.
 *
 * <p>This parser is incremental: {@link #offer(String)} may be called with arbitrary
 * chunks and {@link #read()} returns {@code null} whenever more input is required to
 * decide about the pending characters.
 *
 * @author thevpc
 */
public class MTFParser extends AbstractNTextNodeParser {

    /**
     * NTF 'silent' separator. It is used as a separator when required but never displayed.
     */
    private static final char SILENT = '\u001E';

    /**
     * Result of a lookahead scan that did not find any delimiter.
     */
    private static final int SCAN_NOT_FOUND = -1;

    /**
     * Result of a lookahead scan that ran out of input before being able to decide.
     */
    private static final int SCAN_INCOMPLETE = -2;

    /**
     * Extra characters accepted in the {@code lang} part of an embedded source.
     */
    private static final String SOURCE_LANG_CHARS = "!#+-_.@";

    /**
     * Matches an NTF title delimiter such as {@code #)} or {@code ###)}.
     */
    private static final String TITLE_PATTERN = "#+[)]";

    /**
     * Matches the opening delimiter of an NTF primary style such as {@code ##}.
     */
    private static final String OPEN_STYLE_PATTERN = "##+";

    private static final String[] SHARPS = {
            "",
            "#",
            "##",
            "###",
            "####",
            "#####",
            "######",
            "#######",
            "########",
            "#########",
            "##########",
            "###########",
            "############",
            "#############",
            "##############",
            "###############"
    };

    private enum StepEnum {
        TEXT,
        CODE,
        TITLE,
        SIMPLE_STYLE,
        COMPOSITE_STYLE,
    }

    private static class Embedded {
        final StepEnum mode;
        final NTextStyles style;
        final int level;
        final List<NText> children = new ArrayList<>();

        Embedded(StepEnum mode, NTextStyles style, int level) {
            this.mode = mode;
            this.style = style;
            this.level = level;
        }
    }

    private final StringBuilder buffer = new StringBuilder();
    private final DefaultNCharQueue q = new DefaultNCharQueue();
    private final Stack<Embedded> stackedStyles = new Stack<>();
    /**
     * Top level nodes already parsed but not yet returned to the caller.
     */
    private final Deque<NText> pendingOut = new ArrayDeque<>();
    private boolean wasNewLine = true;
    /**
     * true when the blank that follows a title delimiter has not been consumed yet
     * because it was not part of the current input chunk.
     */
    private boolean skipTitleBlank;
    /**
     * true when the offered input is known to be the whole input
     */
    private boolean fully;
    /**
     * Index of the ':' separating lang and content of the last scanned embedded source.
     */
    private int sourceColonIndex;
    /**
     * Index of the first content character of the last scanned embedded source.
     */
    private int sourceContentIndex;

    public MTFParser() {
        super();
    }

    @Override
    public void offer(char c) {
        synchronized (q) {
            q.write(c);
        }
    }

    public void eof(boolean c) {
        synchronized (q) {
            q.eof(c);
        }
    }

    @Override
    public void offer(String c) {
        synchronized (q) {
            q.write(c);
        }
    }

    @Override
    public void offer(char[] c) {
        synchronized (q) {
            q.write(c);
        }
    }

    @Override
    public void offer(char[] c, int offset, int len) {
        synchronized (q) {
            q.write(c, offset, len);
        }
    }

    @Override
    public void reset() {
        synchronized (q) {
            q.clear();
            q.eof(false);
            stackedStyles.clear();
            pendingOut.clear();
            buffer.setLength(0);
            wasNewLine = true;
            skipTitleBlank = false;
        }
    }

    // -------------------------------------------------------------------------
    // public api
    // -------------------------------------------------------------------------

    @Override
    public NText read() {
        return readNode(false);
    }

    /**
     * Parses all the remaining input as a single text. Never returns {@code null},
     * an empty text is returned when there is nothing left to parse.
     *
     * @return the whole remaining parsed text
     */
    @Override
    public NText readFully() {
        NTextNodeCollector collector = new NTextNodeCollector();
        while (true) {
            NText n = readNode(true);
            if (n == null) {
                break;
            }
            collector.accept(n);
        }
        return collector.getRootOrEmpty();
    }

    @Override
    public long parseRemaining(Consumer<NText> visitor) {
        long count = 0;
        while (true) {
            NText n = readNode(true);
            if (n == null) {
                break;
            }
            count++;
            visitor.accept(n);
        }
        return count;
    }

    @Override
    public long parseIncremental(char[] buf, int off, int len, Consumer<NText> visitor) {
        offer(buf, off, len);
        long count = 0;
        while (true) {
            NText n = readNode(false);
            if (n == null) {
                break;
            }
            count++;
            visitor.accept(n);
        }
        return count;
    }

    @Override
    public boolean isIncomplete() {
        return q.length() > 0 || buffer.length() > 0 || !stackedStyles.isEmpty();
    }

    // -------------------------------------------------------------------------
    // main parsing loop
    // -------------------------------------------------------------------------

    /**
     * Parses until a top level node is available or until more input is required.
     *
     * @param fully true if the offered input is the whole input
     * @return next top level node or null if more input is required
     */
    private NText readNode(boolean fully) {
        synchronized (q) {
            this.fully = fully;
            loop:
            while (q.hasNext()) {
                if (!pendingOut.isEmpty()) {
                    return pendingOut.poll();
                }
                Embedded embedded = stackedStyles.isEmpty() ? null : stackedStyles.peek();
                StepEnum mode = embedded == null ? StepEnum.TEXT : embedded.mode;
                switch (q.peek()) {
                    case '}': {
                        wasNewLine = false;
                        if (mode != StepEnum.COMPOSITE_STYLE) {
                            buffer.append(q.read());
                            break;
                        }
                        NStringMatchResult n1 = q.peekString("}##", fully);
                        if (n1.mode() == NMatchType.FULL_MATCH) {
                            q.read(3);
                            closeCompositeStyle();
                        } else if (n1.mode() == NMatchType.NO_MATCH) {
                            buffer.append(q.read());
                        } else {
                            break loop;
                        }
                        break;
                    }
                    case '#': {
                        if (mode == StepEnum.CODE) {
                            wasNewLine = false;
                            buffer.append(q.read());
                        } else if (!readSharp(embedded, mode)) {
                            break loop;
                        }
                        break;
                    }
                    case '\\': {
                        if (!readBackSlash(mode)) {
                            break loop;
                        }
                        break;
                    }
                    case SILENT: {
                        wasNewLine = false;
                        if (mode == StepEnum.CODE) {
                            //verbatim text keeps every character
                            buffer.append(q.read());
                        } else {
                            //silent separator, never displayed
                            q.read();
                        }
                        break;
                    }
                    case '\r':
                    case '\n': {
                        if (!readNewLine(mode)) {
                            break loop;
                        }
                        break;
                    }
                    case '`': {
                        if (!readBackQuote(mode)) {
                            break loop;
                        }
                        break;
                    }
                    case '*': {
                        if (mode == StepEnum.CODE) {
                            wasNewLine = false;
                            buffer.append(q.read());
                        } else if (!readEmphasis()) {
                            break loop;
                        }
                        break;
                    }
                    case '[': {
                        if (mode == StepEnum.CODE) {
                            wasNewLine = false;
                            buffer.append(q.read());
                        } else if (!readSource(mode == StepEnum.TITLE)) {
                            break loop;
                        }
                        break;
                    }
                    default: {
                        wasNewLine = false;
                        if (skipTitleBlank) {
                            skipTitleBlank = false;
                            if (mode == StepEnum.TITLE && q.peek() == ' ') {
                                //ignore the leading space of the title
                                q.read();
                                break;
                            }
                        }
                        buffer.append(q.read());
                    }
                }
            }

            if (fully) {
                //close all the unterminated contexts, flushing their content
                while (!stackedStyles.isEmpty()) {
                    closeCurrentContext();
                    if (!pendingOut.isEmpty()) {
                        return pendingOut.poll();
                    }
                }
            }
            if (stackedStyles.isEmpty()) {
                //at top level the buffered text is definitely plain text
                consumeBuffer();
            }
            return pendingOut.isEmpty() ? null : pendingOut.poll();
        }
    }

    /**
     * Reads the NTF primary/composite styles and titles.
     *
     * <p>{@link NMultiPattern} is not used here because it selects the "best" match and
     * therefore ignores the patterns that may still complete with more input. As the
     * parser must support incremental input, any pending pattern suspends the parsing.
     *
     * @return false if more input is required
     */
    private boolean readSharp(Embedded embedded, StepEnum mode) {
        final int level = (embedded == null ? 0 : embedded.level) + 1;
        final boolean canClose = level < SHARPS.length && mode == StepEnum.SIMPLE_STYLE;
        final boolean canTitle = wasNewLine
                && (mode == StepEnum.TEXT || mode == StepEnum.COMPOSITE_STYLE);
        final boolean canOpen = mode != StepEnum.SIMPLE_STYLE;

        final String simplePattern = "##:(?<n>[!a-zA-Z0-9_,(')/%+-]+)[: ]";
        final String compositePattern = "##\\{(?<n>[!a-zA-Z0-9_,(')/%+-]+)[: ]";
        final String closePattern = SHARPS[level] + "($|[^#])";

        final NStringMatchResult rSimple = q.peekPattern(simplePattern, fully);
        final NStringMatchResult rComposite = q.peekPattern(compositePattern, fully);
        final NStringMatchResult rTitle = canTitle
                ? q.peekPattern(TITLE_PATTERN, fully)
                : NStringMatchResult.ofNoMatch();
        final NStringMatchResult rClose = canClose
                ? q.peekPattern(closePattern, fully)
                : NStringMatchResult.ofNoMatch();
        final NStringMatchResult rOpen = canOpen
                ? q.peekPattern(OPEN_STYLE_PATTERN, fully)
                : NStringMatchResult.ofNoMatch();

        if (isPending(rSimple) || isPending(rComposite) || isPending(rTitle)
                || isPending(rClose) || isPending(rOpen)) {
            //more input may turn one of these into a valid match
            return false;
        }

        if (rSimple.mode() == NMatchType.FULL_MATCH) {
            wasNewLine = false;
            String all = rSimple.get();
            NTextStyles styles = NTextStyles.parse(rSimple.get("n")).orNull();
            q.read(rSimple.count());
            consumeBuffer();
            if (styles == null) {
                //not a valid style, keep the raw sequence as plain text
                buffer.append(all);
            } else {
                pushSimpleStyle(styles, 1);
            }
            return true;
        }
        if (rComposite.mode() == NMatchType.FULL_MATCH) {
            wasNewLine = false;
            String all = rComposite.get();
            NTextStyles styles = NTextStyles.parse(rComposite.get("n")).orNull();
            q.read(rComposite.count());
            consumeBuffer();
            if (styles == null) {
                buffer.append(all);
            } else {
                pushCompositeStyle(styles);
            }
            return true;
        }
        if (rTitle.mode() == NMatchType.FULL_MATCH) {
            wasNewLine = false;
            String all = rTitle.get();
            q.read(rTitle.count());
            if (q.hasNext()) {
                skipTitleBlank = false;
                if (q.peek() == ' ') {
                    //ignore the leading space of the title
                    q.read();
                }
            } else {
                //the leading space may be part of a next chunk
                skipTitleBlank = true;
            }
            consumeBuffer();
            pushTitle(all.length() - 1);
            return true;
        }
        if (rClose.mode() == NMatchType.FULL_MATCH) {
            wasNewLine = false;
            q.read(SHARPS[level].length());
            closeSimpleStyle();
            return true;
        }
        if (rOpen.mode() == NMatchType.FULL_MATCH) {
            int count = rOpen.count();
            q.read(count);
            wasNewLine = false;
            consumeBuffer();
            pushSimpleStyle(count - 1);
            return true;
        }
        wasNewLine = false;
        buffer.append(q.read());
        return true;
    }

    /**
     * @param r some pattern result
     * @return true if more input is required to know whether the pattern matches
     */
    private static boolean isPending(NStringMatchResult r) {
        return r.mode() == NMatchType.MATCH || r.mode() == NMatchType.PARTIAL_MATCH;
    }

    /**
     * reads the NTF and markdown escape sequences.
     *
     * @param mode current mode
     * @return false if more input is required
     */
    private boolean readBackSlash(StepEnum mode) {
        wasNewLine = false;
        if (!canPeek(1)) {
            if (!eofReached()) {
                return false;
            }
            buffer.append(q.read());
            return true;
        }
        char escaped = q.peekAt(1);
        if (mode == StepEnum.CODE) {
            if (escaped == '\\') {
                q.read(2);
                buffer.append('\\');
                return true;
            }
            if (escaped == '`') {
                int ticks = tickRun(1);
                if (ticks >= 3) {
                    //escaped code fence
                    q.read(1 + ticks);
                    appendRepeat('`', ticks);
                    return true;
                }
            }
            buffer.append(q.read(2));
            return true;
        }
        switch (escaped) {
            case '\\':
            case '#':
            case '`':
            case '*':
            case '[':
            case ']': {
                q.read(2);
                buffer.append(escaped);
                return true;
            }
            case SILENT: {
                //escaped silent separator is ignored
                q.read(2);
                return true;
            }
            default: {
                //not an escape sequence, keep the raw characters
                buffer.append(q.read(2));
                return true;
            }
        }
    }

    /**
     * reads a new line, closing the current title if any.
     *
     * @param mode current mode
     * @return false if more input is required
     */
    private boolean readNewLine(StepEnum mode) {
        String s = q.readNewLine(fully);
        if (s == null) {
            //an incomplete \r sequence, more input is required
            return false;
        }
        if (mode == StepEnum.CODE) {
            wasNewLine = false;
            buffer.append(s);
            return true;
        }
        if (mode == StepEnum.TITLE) {
            //titles are single lines, the new line is implicit in NTextTitle
            wasNewLine = true;
            closeTitle();
            return true;
        }
        wasNewLine = true;
        buffer.append(s);
        return true;
    }

    /**
     * reads an NTF code block or a markdown inline code span.
     *
     * @param mode current mode
     * @return false if more input is required
     */
    private boolean readBackQuote(StepEnum mode) {
        NStringMatchResult n = q.peekString("```", fully);
        if (n.mode() == NMatchType.FULL_MATCH) {
            wasNewLine = false;
            q.read(3);
            if (mode == StepEnum.CODE) {
                closeCode();
            } else {
                consumeBuffer();
                pushCode();
            }
            return true;
        }
        if (n.mode() == NMatchType.NO_MATCH) {
            if (mode == StepEnum.CODE) {
                //verbatim text
                wasNewLine = false;
                buffer.append(q.read());
                return true;
            }
            return readInlineCode();
        }
        //more input is required to know whether this is a code fence
        return false;
    }

    /**
     * reads a markdown {@code `inline code`} span.
     *
     * @return false if more input is required
     */
    private boolean readInlineCode() {
        int close = findInlineCodeEnd(1);
        if (close == SCAN_INCOMPLETE) {
            return false;
        }
        wasNewLine = false;
        if (close == SCAN_NOT_FOUND) {
            buffer.append(q.read());
            return true;
        }
        //drop the surrounding back quotes
        String code = q.read(close + 1).substring(1, close);
        consumeBuffer();
        push(NText.ofCode("`", "", ":", "`", code));
        return true;
    }

    /**
     * reads a markdown {@code *italic*}, {@code **bold**} or {@code ***bold italic***}
     * span. Any unmatched delimiter is kept as plain text.
     *
     * @return false if more input is required
     */
    private boolean readEmphasis() {
        int count = starRun(0);
        if (count >= q.length() && !eofReached()) {
            //more stars may follow, the meaning of this sequence is unknown yet
            return false;
        }
        if (count == 1 && wasNewLine && canPeek(1) && Character.isWhitespace(q.peekAt(1))) {
            //bullet list item, not an italic span
            wasNewLine = false;
            buffer.append(q.read());
            return true;
        }
        if (startsBlankSpan(count)) {
            //an opening delimiter can not be followed by a blank
            wasNewLine = false;
            buffer.append(q.read());
            return true;
        }
        int delimiterCount = Math.min(count, 3);
        int close = scanEmphasisEnd(delimiterCount, delimiterCount);
        if (close == SCAN_INCOMPLETE) {
            return false;
        }
        if (close == SCAN_NOT_FOUND) {
            //not an emphasis span, keep the raw text
            wasNewLine = false;
            buffer.append(q.read());
            return true;
        }
        String raw = q.read(close + delimiterCount);
        wasNewLine = false;
        consumeBuffer();
        push(NText.ofStyled(parseFragment(raw.substring(delimiterCount, close)), emphasisStyle(delimiterCount)));
        return true;
    }

    /**
     * reads an embedded source of the form {@code [[lang: source]]}.
     *
     * @param sameLine true if the source can not span multiple lines
     * @return false if more input is required
     */
    private boolean readSource(boolean sameLine) {
        if (!canPeek(1)) {
            if (!eofReached()) {
                //the second bracket may still come
                return false;
            }
            wasNewLine = false;
            buffer.append(q.read());
            return true;
        }
        if (q.peekAt(1) != '[') {
            wasNewLine = false;
            buffer.append(q.read());
            return true;
        }
        int contentStart = scanSourceHeader(2);
        if (contentStart == SCAN_INCOMPLETE) {
            return false;
        }
        if (contentStart == SCAN_NOT_FOUND) {
            wasNewLine = false;
            buffer.append(q.read());
            return true;
        }
        int end = scanSourceEnd(contentStart, sameLine);
        if (end == SCAN_INCOMPLETE) {
            return false;
        }
        if (end == SCAN_NOT_FOUND) {
            wasNewLine = false;
            buffer.append(q.read());
            return true;
        }
        String raw = q.read(end + 2);
        wasNewLine = false;
        consumeBuffer();
        push(NText.ofCodeOrCommand(
                raw.substring(2, sourceColonIndex),
                unescapeSource(raw.substring(sourceContentIndex, end)),
                ":"
        ));
        return true;
    }

    // -------------------------------------------------------------------------
    // lookahead
    // -------------------------------------------------------------------------

    /**
     * scans the closing delimiter of an emphasis span. Emphasis spans never span
     * multiple lines. Runs of delimiters of another length are skipped so that
     * {@code *a **b** c*} is read as a single italic span.
     *
     * @param from     index of the first character of the span content
     * @param expected length of the expected closing delimiter
     * @return index of the closing delimiter, {@link #SCAN_NOT_FOUND} or {@link #SCAN_INCOMPLETE}
     */
    private int scanEmphasisEnd(int from, int expected) {
        int i = from;
        while (true) {
            if (i >= q.length()) {
                return eofReached() ? SCAN_NOT_FOUND : SCAN_INCOMPLETE;
            }
            char c = q.peekAt(i);
            if (c == '\n' || c == '\r') {
                return SCAN_NOT_FOUND;
            }
            switch (c) {
                case '\\': {
                    if (!canPeek(i + 1)) {
                        return eofReached() ? SCAN_NOT_FOUND : SCAN_INCOMPLETE;
                    }
                    i += 2;
                    break;
                }
                case '`': {
                    int ticks = tickRun(i);
                    int end = findInlineCodeEnd(i + ticks);
                    if (end == SCAN_INCOMPLETE) {
                        return SCAN_INCOMPLETE;
                    }
                    i = end == SCAN_NOT_FOUND ? i + ticks : end + 1;
                    break;
                }
                case '[': {
                    i = skipSource(i);
                    break;
                }
                case '*': {
                    int stars = starRun(i);
                    if (stars == expected && isClosingDelimiter(i, expected)) {
                        return i;
                    }
                    //another delimiter run, skip it entirely
                    i += stars;
                    break;
                }
                default: {
                    i++;
                }
            }
        }
    }

    /**
     * A closing delimiter must follow a non blank character so that an opening
     * delimiter followed by a blank one is never closed by the very same run.
     *
     * @param i     index of a candidate closing delimiter
     * @param count length of the delimiter
     * @return true if this delimiter can close a span
     */
    private boolean isClosingDelimiter(int i, int count) {
        return i > 0 && !Character.isWhitespace(q.peekAt(i - 1));
    }

    /**
     * @param i index of the opening back quote
     * @return index of the closing back quote, {@link #SCAN_NOT_FOUND} or {@link #SCAN_INCOMPLETE}
     */
    private int findInlineCodeEnd(int i) {
        int length = q.length();
        while (true) {
            if (i >= length) {
                return eofReached() ? SCAN_NOT_FOUND : SCAN_INCOMPLETE;
            }
            char c = q.peekAt(i);
            if (c == '\n' || c == '\r') {
                return SCAN_NOT_FOUND;
            }
            if (c == '`') {
                return i;
            }
            i++;
        }
    }

    /**
     * scans the header of an embedded source. Expected form is {@code lang:} optionally
     * followed by a single space.
     *
     * @param from index of the first character of the lang
     * @return index of the first content character, {@link #SCAN_NOT_FOUND} or {@link #SCAN_INCOMPLETE}
     */
    private int scanSourceHeader(int from) {
        int length = q.length();
        int i = from;
        while (true) {
            if (i >= length) {
                return eofReached() ? SCAN_NOT_FOUND : SCAN_INCOMPLETE;
            }
            char c = q.peekAt(i);
            if (c == ':') {
                if (i == from) {
                    //an empty lang is not an embedded source
                    return SCAN_NOT_FOUND;
                }
                sourceColonIndex = i;
                i++;
                if (i < length && (q.peekAt(i) == ' ' || q.peekAt(i) == '\t')) {
                    i++;
                }
                sourceContentIndex = i;
                return i;
            }
            if (Character.isLetterOrDigit(c) || SOURCE_LANG_CHARS.indexOf(c) >= 0) {
                i++;
            } else {
                return SCAN_NOT_FOUND;
            }
        }
    }

    /**
     * scans the end of an embedded source. Expected form is {@code ]]}. A {@code \]} is
     * an escaped closing delimiter.
     *
     * @param from     index of the first character of the content
     * @param sameLine true if the content can not span multiple lines
     * @return index of the first {@code ]} of the closing {@code ]]},
     * {@link #SCAN_NOT_FOUND} or {@link #SCAN_INCOMPLETE}
     */
    private int scanSourceEnd(int from, boolean sameLine) {
        int length = q.length();
        int i = from;
        while (true) {
            if (i >= length) {
                return eofReached() ? SCAN_NOT_FOUND : SCAN_INCOMPLETE;
            }
            char c = q.peekAt(i);
            if (c == '\n' || c == '\r') {
                if (sameLine) {
                    return SCAN_NOT_FOUND;
                }
                i++;
            } else if (c == '\\') {
                if (!canPeek(i + 1)) {
                    return eofReached() ? SCAN_NOT_FOUND : SCAN_INCOMPLETE;
                }
                i += 2;
            } else if (c == ']') {
                if (!canPeek(i + 1)) {
                    return eofReached() ? SCAN_NOT_FOUND : SCAN_INCOMPLETE;
                }
                if (q.peekAt(i + 1) == ']') {
                    return i;
                }
                i++;
            } else {
                i++;
            }
        }
    }

    /**
     * skips an embedded source found while scanning for a closing delimiter.
     *
     * @param i index of the opening [
     * @return index of the first character following the embedded source
     */
    private int skipSource(int i) {
        if (!canPeek(i + 1) || q.peekAt(i + 1) != '[') {
            return i + 1;
        }
        int contentStart = scanSourceHeader(i + 2);
        if (contentStart == SCAN_INCOMPLETE || contentStart == SCAN_NOT_FOUND) {
            return i + 1;
        }
        int end = scanSourceEnd(contentStart, false);
        if (end == SCAN_INCOMPLETE || end == SCAN_NOT_FOUND) {
            return i + 1;
        }
        return end + 2;
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private StepEnum currentMode() {
        return stackedStyles.isEmpty() ? StepEnum.TEXT : stackedStyles.peek().mode;
    }

    private boolean eofReached() {
        return fully || q.isEOF();
    }

    private boolean canPeek(int index) {
        return index >= 0 && index < q.length();
    }

    /**
     * @param i index of the first back quote
     * @return the number of consecutive back quotes starting at {@code i}
     */
    private int tickRun(int i) {
        int length = q.length();
        int count = 0;
        while (i + count < length && q.peekAt(i + count) == '`') {
            count++;
        }
        return count;
    }

    /**
     * @param i index of the first star
     * @return the number of consecutive stars starting at {@code i}
     */
    private int starRun(int i) {
        int length = q.length();
        int count = 0;
        while (i + count < length && q.peekAt(i + count) == '*') {
            count++;
        }
        return count;
    }

    /**
     * @param from index just after an opening delimiter
     * @return true if the span would have an empty or blank content
     */
    private boolean startsBlankSpan(int from) {
        if (from >= q.length()) {
            //nothing follows the delimiter
            return true;
        }
        return Character.isWhitespace(q.peekAt(from));
    }

    private static NTextStyles emphasisStyle(int count) {
        switch (count) {
            case 1:
                return NTextStyles.of(NTextStyle.italic());
            case 2:
                return NTextStyles.of(NTextStyle.bold());
            default:
                return NTextStyles.of(NTextStyle.bold(), NTextStyle.italic());
        }
    }

    /**
     * removes the escape sequences of an embedded source content.
     */
    private static String unescapeSource(String value) {
        if (value.indexOf('\\') < 0) {
            return value;
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char n = value.charAt(i + 1);
                if (n == '\\' || n == ']') {
                    sb.append(n);
                    i++;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * parses a standalone fragment such as the content of an emphasis span.
     */
    private static NText parseFragment(String text) {
        MTFParser parser = new MTFParser();
        parser.offer(text);
        parser.eof(true);
        return parser.readFully();
    }

    private void consumeBuffer() {
        String s = consumeBufferString();
        if (s != null) {
            push(NText.ofPlain(s));
        }
    }

    private void appendRepeat(char c, int count) {
        for (int i = 0; i < count; i++) {
            buffer.append(c);
        }
    }

    private String consumeBufferString() {
        if (buffer.length() == 0) {
            return null;
        }
        String s = buffer.toString();
        buffer.setLength(0);
        return s;
    }

    /**
     * pushes a node either to the current context or to the output queue.
     */
    private void push(NText node) {
        if (node == null) {
            return;
        }
        if (!stackedStyles.isEmpty()) {
            stackedStyles.peek().children.add(node);
        } else {
            pendingOut.add(node);
        }
    }

    private void pushCode() {
        stackedStyles.push(new Embedded(StepEnum.CODE, null, 0));
    }

    private void pushTitle(int level) {
        stackedStyles.push(new Embedded(StepEnum.TITLE, null, level));
    }

    private void pushSimpleStyle(int level) {
        NTextStyles style = NTextStyles.parse("p" + level).orNull();
        if (style == null) {
            style = NTextStyles.parse("p").orNull();
        }
        if (style == null) {
            style = NTextStyles.PLAIN;
        }
        pushSimpleStyle(style, level);
    }

    private void pushSimpleStyle(NTextStyles style, int level) {
        stackedStyles.push(new Embedded(StepEnum.SIMPLE_STYLE, style, level));
    }

    private void pushCompositeStyle(NTextStyles style) {
        stackedStyles.push(new Embedded(StepEnum.COMPOSITE_STYLE, style, 0));
    }

    /**
     * closes the current context whatever it is. Used to flush the unterminated
     * contexts at the end of the input.
     */
    private void closeCurrentContext() {
        switch (currentMode()) {
            case CODE:
                closeCode();
                break;
            case TITLE:
                closeTitle();
                break;
            case COMPOSITE_STYLE:
                closeCompositeStyle();
                break;
            default:
                closeSimpleStyle();
        }
    }

    private void closeCode() {
        consumeBuffer();
        Embedded embedded = stackedStyles.pop();
        push(NText.ofCodeOrCommand(
                NText.ofList(embedded.children).simplify().filteredText()
        ).simplify());
    }

    private void closeTitle() {
        consumeBuffer();
        Embedded embedded = stackedStyles.pop();
        push(NText.ofTitle(NText.ofList(embedded.children).simplify(), embedded.level));
    }

    private void closeSimpleStyle() {
        consumeBuffer();
        Embedded embedded = stackedStyles.pop();
        wasNewLine = false;
        push(NText.ofStyled(NText.ofList(embedded.children).simplify(), embedded.style).simplify());
    }

    private void closeCompositeStyle() {
        consumeBuffer();
        Embedded embedded = stackedStyles.pop();
        push(NText.ofStyled(NText.ofList(embedded.children).simplify(), embedded.style).simplify());
    }
}
