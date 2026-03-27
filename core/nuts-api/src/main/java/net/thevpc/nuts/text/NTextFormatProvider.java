package net.thevpc.nuts.text;

import net.thevpc.nuts.concurrent.NScoredCallable;

public interface NTextFormatProvider {
    String[] types();
    <T> NScoredCallable<NTextFormat<T>> resolveFormat(String pattern, Class<T> expectedType);
}
