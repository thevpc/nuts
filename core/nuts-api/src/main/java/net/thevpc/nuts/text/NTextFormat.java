package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

public interface NTextFormat<T> extends NStringFormat<T> {
    static <T> NOptional<NTextFormat<T>> of(String type, Class<T> expectedType, String pattern) {
        NTexts texts = NTexts.of();
        NAssert.requireNamedNonNull(type, "type");
        NAssert.requireNamedNonNull(expectedType, "expectedType");
        NAssert.requireNamedNonNull(pattern, "pattern");
        return texts.createTextFormat(type, pattern, expectedType);
    }

    NText toText(T object);

    default String toString(T object) {
        return toText(object).filteredText();
    }
}
