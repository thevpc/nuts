package net.thevpc.nuts.text;

import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

/**
 * NTextFormat interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTextFormat<T> extends NStringFormat<T> {
    /**
     * Creates a new instance of number.
     *
     * @return of number result
     */
    static NTextFormat<Number> ofNumber() {
        return of("number", Number.class, null);
    }

    /**
     * Creates a new instance of bytes.
     *
     * @param pattern pattern
     * @return of bytes result
     */
    static NTextFormat<Number> ofBytes(String pattern) {
        return of("bytes", Number.class, pattern);
    }

    /**
     * Creates a new instance of frequency.
     *
     * @param pattern pattern
     * @return of frequency result
     */
    static NTextFormat<Number> ofFrequency(String pattern) {
        return of("bytes", Number.class, pattern);
    }

    /**
     * Creates a new instance of distance.
     *
     * @param pattern pattern
     * @return of distance result
     */
    static NTextFormat<Number> ofDistance(String pattern) {
        return of("meters", Number.class, pattern);
    }

    /**
     * Creates a new instance of number.
     *
     * @param type type
     * @param pattern pattern
     * @return of number result
     */
    static NTextFormat<Number> ofNumber(String type, String pattern) {
        return of(type, Number.class, pattern);
    }

    /**
     * Creates a new instance of number.
     *
     * @param format format
     * @return of number result
     */
    static NTextFormat<Number> ofNumber(String format) {
        return of("number", Number.class, format);
    }

    /**
     * Creates a new instance of percent.
     *
     * @return of percent result
     */
    static NTextFormat<Number> ofPercent() {
        return of("number", Number.class, "00.00%");
    }

    /**
     * Creates a new instance.
     *
     * @param type type
     * @param expectedType expected type
     * @return of result
     */
    static <T> NTextFormat<T> of(String type, Class<T> expectedType) {
        return of(type, expectedType, null);
    }

    /**
     * Creates a new instance.
     *
     * @param type type
     * @param expectedType expected type
     * @param pattern pattern
     * @return of result
     */
    static <T> NTextFormat<T> of(String type, Class<T> expectedType, String pattern) {
        return get(type, expectedType, pattern).get();
    }

    /**
     * Returns the get.
     *
     * @param type type
     * @param expectedType expected type
     * @param pattern pattern
     * @return get result
     */
    static <T> NOptional<NTextFormat<T>> get(String type, Class<T> expectedType, String pattern) {
        NTextRPI texts = NTextRPI.of();
        NAssert.requireNamedNonNull(type, "type");
        NAssert.requireNamedNonNull(expectedType, "expectedType");
        return texts.createTextFormat(type, pattern, expectedType);
    }

    /**
     * Converts to text.
     *
     * @param object object
     * @return to text result
     */
    NText toText(T object);

    default String toString(T object) {
        return toText(object).filteredText();
    }
}
