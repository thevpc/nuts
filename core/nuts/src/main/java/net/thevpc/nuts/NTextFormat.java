package net.thevpc.nuts;

import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;

public interface NTextFormat<T> {
    static <T> NOptional<NTextFormat<T>> of(NSession session, String type, Class<T> expectedType, String pattern) {
        NTexts texts = NTexts.of(session);
        NAssert.requireNonNull(type, "type", session);
        NAssert.requireNonNull(expectedType, "expectedType", session);
        NAssert.requireNonNull(pattern, "pattern", session);
        return texts.createTextFormat(type, expectedType, pattern);
    }

    NText toText(T object, NSession session);

    default String toString(T object, NSession session) {
        return toText(object, session).filteredText();
    }
}
