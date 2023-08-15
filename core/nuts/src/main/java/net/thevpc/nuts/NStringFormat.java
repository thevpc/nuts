package net.thevpc.nuts;

import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

public interface NStringFormat<T> {
    static <T> NOptional<NStringFormat<T>> of(NSession session, String type, Class<T> expectedType, String pattern) {
        NTexts texts = NTexts.of(session);
        NAssert.requireNonNull(type, "type", session);
        NAssert.requireNonNull(expectedType, "expectedType", session);
        NAssert.requireNonNull(pattern, "pattern", session);
        return texts.createStringFormat(type, pattern, expectedType);
    }

    String toString(T object, NSession session);
}
