package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NNewLineMode;

public interface NElementFormatterBuilder {
    static NElementFormatterBuilder of() {
        return NElements.of().createElementFormatterBuilder();
    }

    NElementFormatterBuilder setContentType(NContentType contentType);

    NElementFormatterBuilder setStyle(NElementFormatterStyle style);

    NElementFormatterBuilder setIndent(int spaces);

    NElementFormatterBuilder setMaxWidth(int width);

    NElementFormatterBuilder setComplexityThreshold(int score);

    NElementFormatterBuilder setColumnLimit(int lineWidth);

    NElementFormatterBuilder clear(NElementType elementType, NAffixType affixType, NAffixAnchor anchor);

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

    NElementFormatterStyle getStyle();

    NContentType getContentType();

    int getIndent();

    int getMaxWidth();

    int getComplexityThreshold();

    int getColumnLimit();

    NElementFormatter build();
}
