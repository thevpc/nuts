package net.thevpc.nuts.util;

import net.thevpc.nuts.elem.NElementType;

import java.util.ArrayList;
import java.util.List;

public class NStringLiteralFormatBuilder {
    private boolean skipBoundaries;
    private NSupportMode condition;
    private List<NCharEscapeSet> escapeSets = new ArrayList<>();
    private NElementType elementType;

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

    public static NStringLiteralFormatBuilder ofJava(NElementType elementType) {
        NStringLiteralFormatBuilder b = new NStringLiteralFormatBuilder();
        b.condition(NSupportMode.ALWAYS);
        b.elementType(elementType);
        b.escapeSets.add(NCharEscapeSet.JAVA_WITH_SPACE);
        return b;
    }

    public static NStringLiteralFormatBuilder ofShell(NElementType elementType) {
        NStringLiteralFormatBuilder b = new NStringLiteralFormatBuilder();
        b.condition(NSupportMode.SUPPORTED);
        b.elementType(elementType);
        b.escapeSets.add(NCharEscapeSet.JAVA_WITH_SPACE);
        return b;
    }

    public NElementType elementType() {
        return elementType;
    }

    public NStringLiteralFormatBuilder elementType(NElementType elementType) {
        this.elementType = elementType;
        return this;
    }

    public NSupportMode condition() {
        return condition;
    }

    public NStringLiteralFormatBuilder condition(NSupportMode condition) {
        this.condition = condition;
        return this;
    }

    public NStringLiteralFormatBuilder addExtraEscapeChars(String extraEscapeChars) {
        this.escapeSets.add(NCharEscapeSet.of(
                NCharEscapeSet.Entry.always(extraEscapeChars,
                        NCharEscape.BACKSLASH)));
        return this;
    }

    public boolean isSkipBoundaries() {
        return skipBoundaries;
    }

    public NStringLiteralFormatBuilder skipBoundaries(boolean skipBoundaries) {
        this.skipBoundaries = skipBoundaries;
        return this;
    }

    public void addEscapeSet(NCharEscapeSet escapeSet) {
        if (escapeSet != null) {
            this.escapeSets.add(escapeSet);
        }
    }

    public AbstractNStringLiteralFormat build() {
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
        AbstractNStringLiteralFormat fmt;
        NElementType quoteType = NUtils.firstNonNull(this.elementType, NElementType.DOUBLE_QUOTED_STRING);
        switch (quoteType) {
            case LINE_STRING:
                fmt = AbstractNStringLiteralFormat.ofPrefix("¶ ", "\n", effectiveCondition, escapeSet);
                break;
            case BLOCK_STRING:
                fmt = AbstractNStringLiteralFormat.ofPrefix("¶¶ ", "\n", effectiveCondition, escapeSet);
                break;
            default:
                fmt = AbstractNStringLiteralFormat.ofEscapeChar(
                        quoteType, effectiveCondition, escapeSet, NCharEscape.BACKSLASH);
                break;
        }
        return fmt;
    }
}
