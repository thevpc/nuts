package net.thevpc.nuts.runtime.standalone.tson;

class TsonApiUtils {
    public static <T extends Comparable<T>> int compareArrays(T[] a1,T[] a2) {
        for (int j = 0; j < Math.max(a1.length, a2.length); j++) {
            if (j >= a1.length) {
                return -1;
            }
            if (j >= a2.length) {
                return 1;
            }
            int i = a1[j].compareTo(a2[j]);
            if (i != 0) {
                return i;
            }
        }
        return 0;
    }

}
