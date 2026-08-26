package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.List;
import java.util.Objects;

/**
 * NArgCompletePosition class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NArgCompletePosition {
    private final int wordIndex;
    private final int wordOffset;
    private final int lineCursor;

    /**
     * Creates a new instance of of.
     *
     * @param str str
     * @return of result
     */
    public static NOptional<NArgCompletePosition> of(String str) {
        if (NBlankable.isBlank(str)) {
            return NOptional.ofNamedEmpty("complete");
        }
        str = NStringUtils.strip(str);
        List<String> r = NStringUtils.split(str.substring(1), ",", true, false);
        if (r.size() > 3) {
            return NOptional.ofEmpty(NMsg.ofC("invalid complete : '%s'", str));
        }
        final Integer z1 = -1;
        Integer index = r.size() < 1 ? z1 : NBlankable.isBlank(r.get(0)) ? z1 : NLiteral.ofInt(r.get(0)).orNull();
        Integer offset = r.size() < 2 ? z1 : NBlankable.isBlank(r.get(1)) ? z1 : NLiteral.ofInt(r.get(1)).orNull();
        Integer cursor = r.size() < 3 ? z1 : NBlankable.isBlank(r.get(2)) ? z1 : NLiteral.ofInt(r.get(2)).orNull();
        if (index != null && offset != null && cursor != null) {
            return NOptional.of(of(index, offset, cursor));
        }
        return NOptional.ofError(NMsg.ofC("invalid complete : '%s'", str));
    }

    /**
     * Creates a new instance of of.
     *
     * @param wordIndex word index
     * @param wordOffset word offset
     * @param lineCursor line cursor
     * @return of result
     */
    public static NArgCompletePosition of(int wordIndex, int wordOffset, int lineCursor) {
        return new NArgCompletePosition(wordIndex, wordOffset, lineCursor);
    }

    /**
     * Creates a new instance of of.
     *
     * @param wordIndex word index
     * @param wordOffset word offset
     * @return of result
     */
    public static NArgCompletePosition of(int wordIndex, int wordOffset) {
        return new NArgCompletePosition(wordIndex, wordOffset, -1);
    }

    /**
     * N arg complete position.
     *
     * @param wordIndex word index
     * @param wordOffset word offset
     * @param lineCursor line cursor
     * @return n arg complete position result
     */
    public NArgCompletePosition(int wordIndex, int wordOffset, int lineCursor) {
        this.wordIndex = Math.max(wordIndex, -1);
        this.wordOffset = Math.max(wordOffset, -1);
        this.lineCursor = Math.max(lineCursor, -1);
    }

    /**
     * Word index.
     *
     * @return word index result
     */
    public int wordIndex() {
        return wordIndex;
    }

    /**
     * Word offset.
     *
     * @return word offset result
     */
    public int wordOffset() {
        return wordOffset;
    }

    /**
     * Line cursor.
     *
     * @return line cursor result
     */
    public int lineCursor() {
        return lineCursor;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NArgCompletePosition pos = (NArgCompletePosition) o;
        return wordIndex == pos.wordIndex && wordOffset == pos.wordOffset && lineCursor == pos.lineCursor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(wordIndex, wordOffset, lineCursor);
    }

    @Override
    public String toString() {
        String a = wordIndex < 0 ? String.valueOf(wordIndex) : "";
        String b = wordOffset < 0 ? String.valueOf(wordOffset) : "";
        String c = lineCursor < 0 ? String.valueOf(lineCursor) : "";
        StringBuilder sb = new StringBuilder();
        sb.append(a);
        if (!b.isEmpty() || !c.isEmpty()) {
            sb.append(",");
            sb.append(b);
            if (!c.isEmpty()) {
                sb.append(",");
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
