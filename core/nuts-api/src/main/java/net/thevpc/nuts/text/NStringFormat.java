package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

public interface NStringFormat<T> {
    static <T> NOptional<NStringFormat<T>> of(String type, Class<T> expectedType, String pattern) {
        NTexts texts = NTexts.of();
        NAssert.requireNonNull(type, "type");
        NAssert.requireNonNull(expectedType, "expectedType");
        NAssert.requireNonNull(pattern, "pattern");
        return texts.createStringFormat(type, pattern, expectedType);
    }

    String toString(T object);
}
