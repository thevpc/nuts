package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.List;

public class NUtils {
    public static <T> T firstNonNull(T a, T b) {
        if (a != null) {
            return a;
        }
        if (b != null) {
            return b;
        }
        return null;
    }

    public static <T> T firstNonNull(T... values) {
        return firstNonNull(values == null ? null : Arrays.asList(values));
    }

    public static <T> T firstNonNull(List<T> values) {
        if (values != null) {
            for (T value : values) {
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

}
