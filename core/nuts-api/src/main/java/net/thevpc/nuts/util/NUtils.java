package net.thevpc.nuts.util;

import net.thevpc.nuts.math.NNumberUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * NUtils class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NUtils {

    /**
     * First non null.
     *
     * @param a a
     * @param b b
     * @return first non null result
     */
    public static <T> T firstNonNull(T a, T b) {
        if (a != null) {
            return a;
        }
        if (b != null) {
            return b;
        }
        return null;
    }

    /**
     * First non null lazy.
     *
     * @param first first
     * @param suppliers suppliers
     * @return first non null lazy result
     */
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

    /**
     * First matching lazy.
     *
     * @param filter filter
     * @param first first
     * @param suppliers suppliers
     * @return first matching lazy result
     */
    public static <T> T firstMatchingLazy(Predicate<? super T> filter, T first, Supplier<? extends T>... suppliers) {
        NAssert.requireNamedNonNull(filter, "filter");
        if (filter.test(first)) {
            return first;
        }
        /**
         * First matching lazy.
         *
         * @param filter filter
         * @param suppliers suppliers
         * @return first matching lazy result
         */
        return firstMatchingLazy(filter,suppliers);
    }

    /**
     * First matching lazy.
     *
     * @param filter filter
     * @param suppliers suppliers
     * @return first matching lazy result
     */
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

    /**
     * First non null.
     *
     * @param values values
     * @return first non null result
     */
    public static <T> T firstNonNull(T... values) {
        /**
         * First non null.
         *
         * @param Arrays.asList(values) arrays.as list(values)
         * @return first non null result
         */
        return firstNonNull(values == null ? null : Arrays.asList(values));
    }

    /**
     * First non null.
     *
     * @param values values
     * @return first non null result
     */
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

    /**
     * Compare objects.
     *
     * @param k1 k1
     * @param k2 k2
     * @param comparator comparator
     * @return compare objects result
     */
    public static int compareObjects(Object k1, Object k2, Comparator<?> comparator) {
        if (k1 == k2) {
            return 0;
        } else if (k1 == null) {
            return -1;
        } else if (k2 == null) {
            return 1;
        } else {
            if (comparator != null) {
              /**
               * Return.
               *
               * @param (Comparator)comparator).compare(k1 ( comparator)comparator).compare(k1
               * @param k2 k2
               */
                return ((Comparator)comparator).compare(k1, k2);
            }
          /**
           * Return.
           *
           * @param (Comparable)k1).compareTo(k2 ( comparable)k1).compare to(k2
           */
            return ((Comparable)k1).compareTo(k2);
        }
    }

    /**
     * Compare objects.
     *
     * @param a a
     * @param b b
     * @return compare objects result
     */
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
              /**
               * Return.
               *
               * @param a).compareTo(b a).compare to(b
               */
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
