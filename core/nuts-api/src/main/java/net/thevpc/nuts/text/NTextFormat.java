package net.thevpc.nuts.text;

import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

public interface NTextFormat<T> extends NStringFormat<T> {
    static NTextFormat<Number> ofNumber() {
        return of("number", Number.class, null);
    }

    static NTextFormat<Number> ofBytes(String pattern) {
        return of("bytes", Number.class, pattern);
    }

    static NTextFormat<Number> ofFrequency(String pattern) {
        return of("bytes", Number.class, pattern);
    }

    static NTextFormat<Number> ofDistance(String pattern) {
        return of("meters", Number.class, pattern);
    }

    static NTextFormat<Number> ofNumber(String type, String pattern) {
        return of(type, Number.class, pattern);
    }

    static NTextFormat<Number> ofNumber(String format) {
        return of("number", Number.class, format);
    }

    static NTextFormat<Number> ofPercent() {
        return of("percent", Number.class, "00.00%");
    }

    static <T> NTextFormat<T> of(String type, Class<T> expectedType) {
        return of(type, expectedType, null);
    }

    static <T> NTextFormat<T> of(String type, Class<T> expectedType, String pattern) {
        return get(type, expectedType, pattern).get();
    }

    static <T> NOptional<NTextFormat<T>> get(String type, Class<T> expectedType, String pattern) {
        NTexts texts = NTexts.of();
        NAssert.requireNamedNonNull(type, "type");
        NAssert.requireNamedNonNull(expectedType, "expectedType");
        return texts.createTextFormat(type, pattern, expectedType);
    }

    NText toText(T object);

    default String toString(T object) {
        return toText(object).filteredText();
    }
}
