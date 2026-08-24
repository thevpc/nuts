package net.thevpc.nuts.text;

import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;

/**
 * NStringFormat interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NStringFormat<T> {
    /**
     * Creates a new instance of of.
     *
     * @param type type
     * @param expectedType expected type
     * @param pattern pattern
     * @return of result
     */
    static <T> NOptional<NStringFormat<T>> of(String type, Class<T> expectedType, String pattern) {
        NTextRPI texts = NTextRPI.of();
        NAssert.requireNamedNonNull(type, "type");
        NAssert.requireNamedNonNull(expectedType, "expectedType");
        NAssert.requireNamedNonNull(pattern, "pattern");
        return texts.createStringFormat(type, pattern, expectedType);
    }

    String toString(T object);
}
