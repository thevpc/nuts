package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElementType;

import java.util.ArrayList;
import java.util.List;

/**
 * NStringLiteralFormatBuilder class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NStringLiteralFormatBuilder {
    private boolean skipBoundaries;
    private NSupportMode condition;
    private List<NCharEscapeSet> escapeSets = new ArrayList<>();
    private NElementType elementType;

    /**
     * Creates a new instance of of tson.
     *
     * @param elementType element type
     * @return of tson result
     */
    public static NStringLiteralFormatBuilder ofTson(NElementType elementType) {
        NStringLiteralFormatBuilder b = new NStringLiteralFormatBuilder();
        b.condition(NSupportMode.ALWAYS);
        b.elementType(elementType);
        switch (elementType) {
            case DOUBLE_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
                b.escapeSets.add(NCharEscapeSet.of(
                        NCharEscapeSet.Entry.always("\"", NCharEscape.REPEAT)
                ));
                break;
            case SINGLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
                b.escapeSets.add(NCharEscapeSet.of(
                        NCharEscapeSet.Entry.always("'", NCharEscape.REPEAT)
                ));
                break;
            case BACKTICK_STRING:
            case TRIPLE_BACKTICK_STRING:
                b.escapeSets.add(NCharEscapeSet.of(
                        NCharEscapeSet.Entry.always("'", NCharEscape.REPEAT)
                ));
                break;
        }
        return b;
    }

    /**
     * Creates a new instance of of java.
     *
     * @param elementType element type
     * @return of java result
     */
    public static NStringLiteralFormatBuilder ofJava(NElementType elementType) {
        NStringLiteralFormatBuilder b = new NStringLiteralFormatBuilder();
        b.condition(NSupportMode.ALWAYS);
        b.elementType(elementType);
        b.escapeSets.add(NCharEscapeSet.JAVA_WITH_SPACE);
        return b;
    }

    /**
     * Creates a new instance of of shell.
     *
     * @param elementType element type
     * @return of shell result
     */
    public static NStringLiteralFormatBuilder ofShell(NElementType elementType) {
        NStringLiteralFormatBuilder b = new NStringLiteralFormatBuilder();
        b.condition(NSupportMode.SUPPORTED);
        b.elementType(elementType);
        b.escapeSets.add(NCharEscapeSet.JAVA_WITH_SPACE);
        return b;
    }

    /**
     * Element type.
     *
     * @return element type result
     */
    public NElementType elementType() {
        return elementType;
    }

    /**
     * Element type.
     *
     * @param elementType element type
     * @return element type result
     */
    public NStringLiteralFormatBuilder elementType(NElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    /**
     * Condition.
     *
     * @return condition result
     */
    public NSupportMode condition() {
        return condition;
    }

    /**
     * Condition.
     *
     * @param condition condition
     * @return condition result
     */
    public NStringLiteralFormatBuilder condition(NSupportMode condition) {
        this.condition = condition;
        return this;
    }

    /**
     * Adds the specified extra escape chars.
     *
     * @param extraEscapeChars extra escape chars
     * @return add extra escape chars result
     */
    public NStringLiteralFormatBuilder addExtraEscapeChars(String extraEscapeChars) {
        this.escapeSets.add(NCharEscapeSet.of(
                NCharEscapeSet.Entry.always(extraEscapeChars,
                        NCharEscape.BACKSLASH)));
        return this;
    }

    /**
     * Checks if is skip boundaries.
     *
     * @return is skip boundaries result
     */
    public boolean isSkipBoundaries() {
        return skipBoundaries;
    }

    /**
     * Skip boundaries.
     *
     * @param skipBoundaries skip boundaries
     * @return skip boundaries result
     */
    public NStringLiteralFormatBuilder skipBoundaries(boolean skipBoundaries) {
        this.skipBoundaries = skipBoundaries;
        return this;
    }

    /**
     * Adds the specified escape set.
     *
     * @param escapeSet escape set
     */
    public void addEscapeSet(NCharEscapeSet escapeSet) {
        if (escapeSet != null) {
            this.escapeSets.add(escapeSet);
        }
    }

    /**
     * Build.
     *
     * @return build result
     */
    public NStringLiteralFormatBase build() {
        NSupportMode effectiveCondition = skipBoundaries ? NSupportMode.NEVER : condition;

        // Build the char-escape set: start from the standard set and append extras if any
        NCharEscapeSet escapeSet;
        if (escapeSets.isEmpty()) {
            escapeSet = NCharEscapeSet.JAVA_WITH_SPACE;
        } else {
            escapeSet = escapeSets.get(0);
            for (int i = 1; i < escapeSets.size(); i++) {
                escapeSet = NCharEscapeSet.combine(escapeSet, escapeSets.get(i));
            }
        }
        NStringLiteralFormatBase fmt;
        NElementType quoteType = NUtils.firstNonNull(this.elementType, NElementType.DOUBLE_QUOTED_STRING);
        switch (quoteType) {
            case LINE_STRING:
                fmt = NStringLiteralFormatBase.ofPrefix("¶ ", "\n", effectiveCondition, escapeSet);
                break;
            case BLOCK_STRING:
                fmt = NStringLiteralFormatBase.ofPrefix("¶¶ ", "\n", effectiveCondition, escapeSet);
                break;
            default:
                fmt = NStringLiteralFormatBase.ofEscapeChar(
                        quoteType, effectiveCondition, escapeSet, NCharEscape.BACKSLASH);
                break;
        }
        return fmt;
    }
}
