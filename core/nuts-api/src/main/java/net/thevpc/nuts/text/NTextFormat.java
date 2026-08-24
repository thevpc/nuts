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
     * Creates a new instance of of number.
     *
     * @return of number result
     */
    static NTextFormat<Number> ofNumber() {
        /**
         * Creates a new instance of of.
         *
         * @param "number" "number"
         * @param Number.class number.class
         * @param null null
         * @return of result
         */
        return of("number", Number.class, null);
    }

    /**
     * Creates a new instance of of bytes.
     *
     * @param pattern pattern
     * @return of bytes result
     */
    static NTextFormat<Number> ofBytes(String pattern) {
        /**
         * Creates a new instance of of.
         *
         * @param "bytes" "bytes"
         * @param Number.class number.class
         * @param pattern pattern
         * @return of result
         */
        return of("bytes", Number.class, pattern);
    }

    /**
     * Creates a new instance of of frequency.
     *
     * @param pattern pattern
     * @return of frequency result
     */
    static NTextFormat<Number> ofFrequency(String pattern) {
        /**
         * Creates a new instance of of.
         *
         * @param "bytes" "bytes"
         * @param Number.class number.class
         * @param pattern pattern
         * @return of result
         */
        return of("bytes", Number.class, pattern);
    }

    /**
     * Creates a new instance of of distance.
     *
     * @param pattern pattern
     * @return of distance result
     */
    static NTextFormat<Number> ofDistance(String pattern) {
        /**
         * Creates a new instance of of.
         *
         * @param "meters" "meters"
         * @param Number.class number.class
         * @param pattern pattern
         * @return of result
         */
        return of("meters", Number.class, pattern);
    }

    /**
     * Creates a new instance of of number.
     *
     * @param type type
     * @param pattern pattern
     * @return of number result
     */
    static NTextFormat<Number> ofNumber(String type, String pattern) {
        /**
         * Creates a new instance of of.
         *
         * @param type type
         * @param Number.class number.class
         * @param pattern pattern
         * @return of result
         */
        return of(type, Number.class, pattern);
    }

    /**
     * Creates a new instance of of number.
     *
     * @param format format
     * @return of number result
     */
    static NTextFormat<Number> ofNumber(String format) {
        /**
         * Creates a new instance of of.
         *
         * @param "number" "number"
         * @param Number.class number.class
         * @param format format
         * @return of result
         */
        return of("number", Number.class, format);
    }

    /**
     * Creates a new instance of of percent.
     *
     * @return of percent result
     */
    static NTextFormat<Number> ofPercent() {
        /**
         * Creates a new instance of of.
         *
         * @param "number" "number"
         * @param Number.class number.class
         * @param "00.00%" "00.00%"
         * @return of result
         */
        return of("number", Number.class, "00.00%");
    }

    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @param expectedType expected type
     * @return of result
     */
    static <T> NTextFormat<T> of(String type, Class<T> expectedType) {
        /**
         * Creates a new instance of of.
         *
         * @param type type
         * @param expectedType expected type
         * @param null null
         * @return of result
         */
        return of(type, expectedType, null);
    }

    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @param expectedType expected type
     * @param pattern pattern
     * @return of result
     */
    static <T> NTextFormat<T> of(String type, Class<T> expectedType, String pattern) {
        /**
         * Returns the get.
         *
         * @param type type
         * @param expectedType expected type
         * @param pattern).get( pattern).get(
         * @return get result
         */
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
        /**
         * Converts to text.
         *
         * @param object).filteredText( object).filtered text(
         * @return to text result
         */
        return toText(object).filteredText();
    }
}
