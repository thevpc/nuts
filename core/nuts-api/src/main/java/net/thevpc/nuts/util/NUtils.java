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

    public static int compareObjects(Object a,Object b){
        if (a == null && b == null) {
            return 0;
        }
        if (a == null ) {
            return -1;
        }
        if (b == null ) {
            return 1;
        }
        if(a instanceof  Number && b instanceof Number){
            return NNumberUtils.compareNumbers((Number)a,(Number)b);
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
