package net.thevpc.nuts.text;

import net.thevpc.nuts.concurrent.NScoredCallable;

/**
 * NTextFormatProvider interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTextFormatProvider {
    /**
     * Types.
     *
     * @return types result
     */
    String[] types();
    /**
     * Resolve format.
     *
     * @param pattern pattern
     * @param expectedType expected type
     * @return resolve format result
     */
    <T> NScoredCallable<NTextFormat<T>> resolveFormat(String pattern, Class<T> expectedType);
}
