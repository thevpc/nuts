package net.thevpc.nuts.util;

public interface NTypedStringifier {
    <T> String toString(T value,Class<T> type);

    <T> T fromString(String value,Class<T> type);
}
