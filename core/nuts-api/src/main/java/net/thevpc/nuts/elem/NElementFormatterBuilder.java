package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

import java.util.function.Predicate;

public interface NElementFormatterBuilder {
    static NElementFormatterBuilder of() {
        return NElements.of().createElementFormatterBuilder();
    }

    NElementFormatterBuilder setIndent(int spaces);

    NElementFormatterBuilder setComplexityThreshold(int score);

    NElementFormatterBuilder setColumnLimit(int lineWidth);

    NElementFormatterBuilder removeComments();

    NElementFormatterBuilder removeWhitespaces();

    NElementFormatterBuilder removeSeparators();

    NElementFormatterBuilder removeNewlines();

    NElementFormatterBuilder removeAffixes(NElementType elementType, Predicate<NBoundAffix> affixPredicate);

    NElementFormatterBuilder addAction(NElementFormatterAction action);

    NElementFormatterBuilder addSpace(NElementType elementType, NAffixAnchor anchor);

    NElementFormatterBuilder addSpace(NElementType elementType, NAffixAnchor anchor, String space);

    NElementFormatterBuilder setSpaces(NElementType elementType, NAffixAnchor anchor, int count);

    NElementFormatterBuilder setSpace(NElementType elementType, NAffixAnchor anchor, String space);

    NElementFormatterBuilder setSpace(NElementType elementType, NAffixAnchor anchor);

    NElementFormatterBuilder removeSpace(NElementType elementType, NAffixAnchor anchor);

    NElementFormatterBuilder setTabs(NElementType elementType, NAffixAnchor anchor, int count);

    NElementFormatterBuilder setTab(NElementType elementType, NAffixAnchor anchor);

    NElementFormatterBuilder addNewline(NElementType elementType, NAffixAnchor anchor, NNewLineMode mode);

    NElementFormatterBuilder setNewline(NElementType elementType, NAffixAnchor anchor, NNewLineMode mode);

    NElementFormatterBuilder setNewline(NElementType elementType, NAffixAnchor anchor);

    NElementFormatterBuilder addNewline(NElementType elementType, NAffixAnchor anchor);

    int getIndent();

    int getComplexityThreshold();

    int getColumnLimit();

    NElementFormatter build();
}
