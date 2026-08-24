package net.thevpc.nuts.util;

/**
 * NTypedStringifier interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTypedStringifier {
    <T> String toString(T value,Class<T> type);

    /**
     * From string.
     *
     * @param value value
     * @param type type
     * @return from string result
     */
    <T> T fromString(String value,Class<T> type);
}
