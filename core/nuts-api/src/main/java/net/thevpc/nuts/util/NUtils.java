package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

    public static <T> T firstNonNullLazy(T first, Supplier<T>... suppliers) {
        if (first != null) {
            return first;
        }
        if (suppliers != null) {
            for (Supplier<T> supplier : suppliers) {
                if (supplier != null) {
                    T v = supplier.get();
                    if (v != null) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    public static <T> T firstMatchingLazy(Predicate<? super T> filter, T first, Supplier<? extends T>... suppliers) {
        NAssert.requireNamedNonNull(filter, "filter");
        if (filter.test(first)) {
            return first;
        }
        return firstMatchingLazy(filter,suppliers);
    }

    public static <T> T firstMatchingLazy(Predicate<? super T> filter, Supplier<? extends T>... suppliers) {
        NAssert.requireNamedNonNull(filter, "filter");
        if (suppliers != null) {
            for (Supplier<? extends T> supplier : suppliers) {
                if (supplier != null) {
                    T v = supplier.get();
                    if (filter.test(v)) {
                        return v;
                    }
                }
            }
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

    public static int compareObjects(Object a, Object b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        if (a instanceof Number && b instanceof Number) {
            return NNumberUtils.compareNumbers((Number) a, (Number) b);
        }
        if (a instanceof Comparable && b instanceof Comparable) {
            try {
                return ((Comparable) a).compareTo(b);
            } catch (ClassCastException e) {
                // If types are incompatible, fall back to hashCode
            }
        }

        int ha = a.hashCode();
        int hb = b.hashCode();
        return Integer.compare(ha, hb);
    }

}
