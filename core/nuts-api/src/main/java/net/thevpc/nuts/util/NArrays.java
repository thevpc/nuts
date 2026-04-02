package net.thevpc.nuts.util;

import net.thevpc.nuts.math.NIndexSelectionStrategy;
import net.thevpc.nuts.text.NMsg;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class NArrays {

    public static <A> A[] reverse(A[] arr) {
        if (arr == null) return null;
        for (int i = 0; i < arr.length / 2; i++) {
            A temp = arr[i];
            arr[i] = arr[arr.length - 1 - i];
            arr[arr.length - 1 - i] = temp;
        }
        return arr;
    }

    public static <A> int indexOfBy(A[] a1, Predicate<A> b1) {
        for (int i = 0; i < a1.length; i++) {
            if (b1.test(a1[i])) {
                return i;
            }
        }
        return -1;
    }

    public static <A> int lastIndexOfBy(A[] a1, Predicate<A> b1) {
        for (int i = a1.length - 1; i >= 0; i--) {
            if (b1.test(a1[i])) {
                return i;
            }
        }
        return -1;
    }

    public static <A> int lastIndexOfBy(A[] a1, int from, Predicate<A> b1) {
        if (from < 0) {
            from = a1.length + from;
        }
        if (from < 0) {
            return -1;
        }
        if (from >= a1.length) {
            from = a1.length - 1;
        }
        for (int i = from; i >= 0; i--) {
            if (b1.test(a1[i])) {
                return i;
            }
        }
        return -1;
    }

    public static <A> int indexOfBy(A[] a1, int from, Predicate<A> b1) {
        for (int i = Math.max(0, from); i < a1.length; i++) {
            if (b1.test(a1[i])) {
                return i;
            }
        }
        return -1;
    }

    public static <A> A[] concat(A[] a1, A b1) {
        return append(a1, b1);
    }

    public static <A> A[] append(A[] a1, A b1) {
        A[] newArr = (A[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        newArr[a1.length] = b1;
        return newArr;
    }

    public static <A> A[] concat(Collection<A> a1, Collection<A> b1) {
        List<A> li = new ArrayList<>(a1);
        li.addAll(b1);
        return li.toArray((A[]) Array.newInstance(a1.getClass().getComponentType(), 0));
    }

    public static <A> A[] concat(A[] a1, Collection<A> b1) {
        List<A> li = new ArrayList<>(a1.length + b1.size());
        li.addAll(Arrays.asList(a1));
        li.addAll(b1);
        return li.toArray((A[]) Array.newInstance(a1.getClass().getComponentType(), 0));
    }

    public static <A> A[] concat(Collection<A> a1, A[] b1) {
        List<A> li = new ArrayList<>(a1.size() + b1.length);
        li.addAll(a1);
        li.addAll(Arrays.asList(b1));
        return li.toArray((A[]) Array.newInstance(a1.getClass().getComponentType(), 0));
    }

    public static <A> A[] concat(A[] a1, A[] b1) {
        A[] newArr = (A[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + b1.length);
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        System.arraycopy(b1, 0, newArr, a1.length, b1.length);
        return newArr;
    }

    public static <A> A[] concat(A b1, A[] a1) {
        return prepend(b1, a1);
    }

    public static <A> A[] prepend(A b1, A[] a1) {
        A[] newArr = (A[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 1, a1.length);
        newArr[0] = b1;
        return newArr;
    }

    /// /////////////////////////////////////////////////////////////////
    public static int[] concat(int[] a1, int b1) {
        return append(a1, b1);
    }

    public static int[] append(int[] a1, int b1) {
        int[] newArr = new int[a1.length + 1];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        newArr[a1.length] = b1;
        return newArr;
    }

    public static int[] concat(int[] a1, int[] b1) {
        int[] newArr = new int[a1.length + b1.length];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        System.arraycopy(b1, 0, newArr, a1.length, b1.length);
        return newArr;
    }

    public static int[] concat(int b1, int[] a1) {
        return prepend(b1, a1);
    }

    public static int[] prepend(int b1, int[] a1) {
        int[] newArr = (int[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 1, a1.length);
        newArr[0] = b1;
        return newArr;
    }

    /// /////////////////////////////////////////////////////////////////
    public static long[] concat(long[] a1, long b1) {
        return append(a1, b1);
    }

    public static long[] append(long[] a1, long b1) {
        long[] newArr = new long[a1.length + 1];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        newArr[a1.length] = b1;
        return newArr;
    }

    public static long[] concat(long[] a1, long[] b1) {
        long[] newArr = new long[a1.length + b1.length];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        System.arraycopy(b1, 0, newArr, a1.length, b1.length);
        return newArr;
    }

    public static long[] concat(long b1, long[] a1) {
        return prepend(b1, a1);
    }

    public static long[] prepend(long b1, long[] a1) {
        long[] newArr = (long[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 1, a1.length);
        newArr[0] = b1;
        return newArr;
    }

    /// /////////////////////////////////////////////////////////////////
    public static double[] concat(double[] a1, double b1) {
        return append(a1, b1);
    }

    public static double[] append(double[] a1, double b1) {
        double[] newArr = new double[a1.length + 1];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        newArr[a1.length] = b1;
        return newArr;
    }

    public static double[] concat(double[] a1, double[] b1) {
        double[] newArr = new double[a1.length + b1.length];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        System.arraycopy(b1, 0, newArr, a1.length, b1.length);
        return newArr;
    }

    public static double[] concat(double b1, double[] a1) {
        return prepend(b1, a1);
    }

    public static double[] prepend(double b1, double[] a1) {
        double[] newArr = (double[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 1, a1.length);
        newArr[0] = b1;
        return newArr;
    }

    /// /////////////////////////////////////////////////////////////////
    public static float[] concat(float[] a1, float b1) {
        return append(a1, b1);
    }

    public static float[] append(float[] a1, float b1) {
        float[] newArr = new float[a1.length + 1];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        newArr[a1.length] = b1;
        return newArr;
    }

    public static float[] concat(float[] a1, float[] b1) {
        float[] newArr = new float[a1.length + b1.length];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        System.arraycopy(b1, 0, newArr, a1.length, b1.length);
        return newArr;
    }

    public static float[] concat(float b1, float[] a1) {
        return prepend(b1, a1);
    }

    public static float[] prepend(float b1, float[] a1) {
        float[] newArr = (float[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 1, a1.length);
        newArr[0] = b1;
        return newArr;
    }

    /// /////////////////////////////////////////////////////////////////
    public static byte[] concat(byte[] a1, byte b1) {
        return append(a1, b1);
    }

    public static byte[] append(byte[] a1, byte b1) {
        byte[] newArr = new byte[a1.length + 1];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        newArr[a1.length] = b1;
        return newArr;
    }

    public static byte[] concat(byte[] a1, byte[] b1) {
        byte[] newArr = new byte[a1.length + b1.length];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        System.arraycopy(b1, 0, newArr, a1.length, b1.length);
        return newArr;
    }

    public static byte[] concat(byte b1, byte[] a1) {
        return prepend(b1, a1);
    }

    public static byte[] prepend(byte b1, byte[] a1) {
        byte[] newArr = (byte[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 1, a1.length);
        newArr[0] = b1;
        return newArr;
    }

    /// /////////////////////////////////////////////////////////////////
    public static boolean[] concat(boolean[] a1, boolean b1) {
        return append(a1, b1);
    }

    public static boolean[] append(boolean[] a1, boolean b1) {
        boolean[] newArr = new boolean[a1.length + 1];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        newArr[a1.length] = b1;
        return newArr;
    }

    public static boolean[] concat(boolean[] a1, boolean[] b1) {
        boolean[] newArr = new boolean[a1.length + b1.length];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        System.arraycopy(b1, 0, newArr, a1.length, b1.length);
        return newArr;
    }

    public static boolean[] concat(boolean b1, boolean[] a1) {
        return prepend(b1, a1);
    }

    public static boolean[] prepend(boolean b1, boolean[] a1) {
        boolean[] newArr = (boolean[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 1, a1.length);
        newArr[0] = b1;
        return newArr;
    }

    /// /////////////////////////////////////////////////////////////////
    public static char[] concat(char[] a1, char b1) {
        return append(a1, b1);
    }

    public static char[] append(char[] a1, char b1) {
        char[] newArr = new char[a1.length + 1];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        newArr[a1.length] = b1;
        return newArr;
    }

    public static char[] concat(char[] a1, char[] b1) {
        char[] newArr = new char[a1.length + b1.length];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        System.arraycopy(b1, 0, newArr, a1.length, b1.length);
        return newArr;
    }

    public static char[] concat(char b1, char[] a1) {
        return prepend(b1, a1);
    }

    public static char[] prepend(char b1, char[] a1) {
        char[] newArr = (char[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 1, a1.length);
        newArr[0] = b1;
        return newArr;
    }

    /// /////////////////////////////////////////////////////////////////
    public static short[] concat(short[] a1, short b1) {
        return append(a1, b1);
    }

    public static short[] append(short[] a1, short b1) {
        short[] newArr = new short[a1.length + 1];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        newArr[a1.length] = b1;
        return newArr;
    }

    public static short[] concat(short[] a1, short[] b1) {
        short[] newArr = new short[a1.length + b1.length];
        System.arraycopy(a1, 0, newArr, 0, a1.length);
        System.arraycopy(b1, 0, newArr, a1.length, b1.length);
        return newArr;
    }

    public static short[] concat(short b1, short[] a1) {
        return prepend(b1, a1);
    }

    public static short[] prepend(short b1, short[] a1) {
        short[] newArr = (short[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + 1);
        System.arraycopy(a1, 0, newArr, 1, a1.length);
        newArr[0] = b1;
        return newArr;
    }

    public static <T> T[] copyOf(T[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static byte[] copyOf(byte[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static boolean[] copyOf(boolean[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static short[] copyOf(short[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static int[] copyOf(int[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static char[] copyOf(char[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static long[] copyOf(long[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static float[] copyOf(float[] original) {
        return Arrays.copyOf(original, original.length);
    }

    public static double[] copyOf(double[] original) {
        return Arrays.copyOf(original, original.length);
    }


    public static String[] concat(String[]... arrays) {
        return concat(String.class, arrays);
    }

    public static double[] concat(double[]... arrays) {
        int count = 0;
        if (arrays != null) {
            for (double[] v : arrays) {
                if (v != null) {
                    count += v.length;
                }
            }
            double[] all = new double[count];
            int idx = 0;
            for (double[] v : arrays) {
                if (v != null) {
                    System.arraycopy(v, 0, all, idx, v.length);
                    idx += v.length;
                }
            }
            return all;
        }
        return new double[0];
    }

    public static long[] concat(long[]... arrays) {
        int count = 0;
        for (long[] v : arrays) {
            if (v != null) {
                count += v.length;
            }
        }
        long[] all = new long[count];
        int idx = 0;
        if (arrays != null) {
            for (long[] v : arrays) {
                if (v != null) {
                    System.arraycopy(v, 0, all, idx, v.length);
                    idx += v.length;
                }
            }
        }
        return all;
    }

    public static int[] concat(int[]... arrays) {
        int count = 0;
        for (int[] v : arrays) {
            if (v != null) {
                count += v.length;
            }
        }
        int[] all = new int[count];
        int idx = 0;
        if (arrays != null) {
            for (int[] v : arrays) {
                if (v != null) {
                    System.arraycopy(v, 0, all, idx, v.length);
                    idx += v.length;
                }
            }
        }
        return all;
    }

    public static <T> T[] concat(Class<T> cls, T[]... arrays) {
        int count = 0;
        for (T[] v : arrays) {
            if (v != null) {
                count += v.length;
            }
        }
        T[] all = (T[]) Array.newInstance(cls, count);
        int idx = 0;
        if (arrays != null) {
            for (T[] v : arrays) {
                if (v != null) {
                    System.arraycopy(v, 0, all, idx, v.length);
                    idx += v.length;
                }
            }
        }
        return all;
    }

    public static <T> T[] filterArray(Class<T> cls, T[] array, Predicate<T> t) {
        List<T> all = new ArrayList<>();
        for (T v : array) {
            if (t == null || t.test(v)) {
                all.add(v);
            }
        }
        return all.toArray((T[]) Array.newInstance(cls, all.size()));
    }

    public static <T> T[] removeHead(T[] arr, int count) {
        int nc = Math.max(arr.length - count, 0);
        T[] arr2 = (T[]) Array.newInstance(arr.getClass().getComponentType(), nc);
        if (nc > 0) {
            System.arraycopy(arr, count, arr2, 0, nc);
        }
        return arr2;
    }

    public static String[] subArray(String[] source, int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            beginIndex = 0;
        }
        if (endIndex > source.length) {
            endIndex = source.length;
        }
        if (beginIndex >= endIndex) {
            return new String[0];
        }
        String[] arr = new String[endIndex - beginIndex];
        System.arraycopy(source, beginIndex, arr, 0, endIndex - beginIndex);
        return arr;
    }

    public static <T> T[] subArray(T[] source, int begin, int end) {
        int b = Math.max(0, begin);
        int e = Math.min(source.length, end);
        if (b >= e) {
            return (T[]) Array.newInstance(source.getClass().getComponentType(), 0);
        }
        return Arrays.copyOfRange(source, b, e);
    }


    public static double[] linear(double min, double max, int count, DoublePredicate filter) {
        if (filter == null) {
            return linear(min, max, count);
        }
        double[] d = new double[count];
        if (count == 1) {
            d[0] = min;
        } else {
            double step = (max - min) / (count - 1);
            for (int i = 0; i < d.length; i++) {
                double v = min + i * step;
                if (filter.test(v)) {
                    d[i] = v;
                }
            }
        }
        return d;
    }

    // old times
    public static double[] linear(double min, double max, int count) {
        if (count <= 0) return new double[0];
        double[] d = new double[count];
        if (count == 1) {
            d[0] = min;
        } else {
            double step = (max - min) / (count - 1);
            for (int i = 0; i < d.length; i++) {
                d[i] = min + i * step;
            }
            d[count - 1] = max; // pin last value exactly, avoiding any floating-point drift
        }
        return d;
    }

    public static float[] linear(float min, float max, int count) {
        if (count <= 0) return new float[0];
        float[] d = new float[count];
        if (count == 1) {
            d[0] = min;
        } else {
            float step = (max - min) / (count - 1);
            for (int i = 0; i < d.length; i++) {
                d[i] = min + i * step;
            }
            d[count - 1] = max; // pin last value exactly, avoiding any floating-point drift
        }
        return d;
    }

    public static long[] linear(long min, long max, int count) {
        if (count <= 0) return new long[0];
        long[] d = new long[count];
        if (count == 1) {
            d[0] = min;
        } else {
            long step = (max - min) / (count - 1);
            for (int i = 0; i < d.length; i++) {
                d[i] = min + i * step;
            }
        }
        return d;
    }

    public static long[] range(long min, long max, long step) {
        int count = (int) Math.abs((max - min) / step) + 1;
        long[] d = new long[count];
        for (int i = 0; i < d.length; i++) {
            d[i] = min + i * step;
        }
        return d;
    }

    public static int rangeSize(double min, double max, double step) {
        if (step >= 0) {
            if (max < min) {
                return 0;
            }
            return (int) Math.abs((max - min) / step) + 1;
        } else {
            if (min < max) {
                return 0;
            }
            return (int) Math.abs((max - min) / step) + 1;
        }
    }

    public static double rangeAt(double min, double max, double step, int index) {
        if (step >= 0) {
            if (max < min) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
//            int count = (int) Math.abs((max - min) / step) + 1;
            return min + index * step;
        } else {
            if (min < max) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
//            int count = (int) Math.abs((max - min) / step) + 1;
            return min + index * step;
        }
    }

    public static double[] range(double min, double max) {
        return range(min, max, 1);
    }


    public static float[] range(float min, float max, float step) {
        if (max < min) {
            return new float[0];
        }
        int count = (int) Math.abs((max - min) / step) + 1;
        float[] d = new float[count];
        for (int i = 0; i < d.length; i++) {
            d[i] = min + i * step;
        }
        return d;
    }


    public static double[] unboxDoubles(List<Double> c) {
        if (c == null) return null;
        double[] r = new double[c.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = c.get(i);
        }
        return r;
    }

    public static int[] unboxInts(List<Integer> c) {
        if (c == null) return null;
        int[] r = new int[c.size()];
        for (int i = 0; i < r.length; i++) {
            r[i] = c.get(i);
        }
        return r;
    }

    public static double[] sample(double[] values, int count, NIndexSelectionStrategy sel) {
        switch (NAssert.requireNamedNonNull(sel, "selection")) {
            case BALANCED: {
                int[] ints = linear(0, values.length - 1, count);
                double[] xx = new double[ints.length];
                for (int i = 0; i < ints.length; i++) {
                    xx[i] = values[ints[i]];
                }
                return xx;
            }
            case FIRST: {
                double[] xx = new double[count];
                System.arraycopy(values, 0, xx, 0, count);
                return xx;
            }
            case LAST: {
                double[] xx = new double[count];
                System.arraycopy(values, values.length - count, xx, 0, count);
                return xx;
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unexpected selection type %s", sel));
    }

    public static int[] sample(int[] values, int count, NIndexSelectionStrategy sel) {
        switch (NAssert.requireNamedNonNull(sel, "selection")) {
            case BALANCED: {
                int[] ints = linear(0, values.length - 1, count);
                int[] xx = new int[ints.length];
                for (int i = 0; i < ints.length; i++) {
                    xx[i] = values[ints[i]];
                }
                return xx;
            }
            case FIRST: {
                int[] xx = new int[count];
                System.arraycopy(values, 0, xx, 0, count);
                return xx;
            }
            case LAST: {
                int[] xx = new int[count];
                System.arraycopy(values, values.length - count, xx, 0, count);
                return xx;
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unexpected selection type %s", sel));
    }

    public static double[] linear(double min, double max, int count, int maxCount, NIndexSelectionStrategy strategy) {
        if (count > maxCount) {
            throw new NIllegalArgumentException(NMsg.ofC("Count cannot exceed maxCount (Resolution limit)"));
        }
        return sample(linear(min, max, maxCount), count, strategy);
    }

    public static int[] linear(int min, int max, int count, int maxCount, NIndexSelectionStrategy strategy) {
        if (count > maxCount) {
            throw new NIllegalArgumentException(NMsg.ofC("Count cannot exceed maxCount (Resolution limit)"));
        }
        return sample(linear(min, max, maxCount), count, strategy);
    }

    public static Double[] box(double[] c) {
        if (c == null) return null;
        Double[] r = new Double[c.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = c[i];
        }
        return r;
    }

    public static Double[][] box(double[][] c) {
        if (c == null) return null;
        Double[][] r = new Double[c.length][];
        for (int i = 0; i < r.length; i++) {
            r[i] = box(c[i]);
        }
        return r;
    }

    public static Double[][][] box(double[][][] c) {
        if (c == null) return null;
        Double[][][] r = new Double[c.length][][];
        for (int i = 0; i < r.length; i++) {
            r[i] = box(c[i]);
        }
        return r;
    }

    public static Integer[] box(int[] c) {
        if (c == null) return null;
        Integer[] r = new Integer[c.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = c[i];
        }
        return r;
    }

    public static int[] unbox(Integer[] c) {
        if (c == null) return null;
        int[] r = new int[c.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = c[i];
        }
        return r;
    }

    public static Long[] box(long[] c) {
        if (c == null) return null;
        Long[] r = new Long[c.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = c[i];
        }
        return r;
    }

    public static long[] unbox(Long[] c) {
        if (c == null) return null;
        long[] r = new long[c.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = c[i];
        }
        return r;
    }

    public static double[] unbox(Double[] c) {
        if (c == null) return null;
        double[] r = new double[c.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = c[i];
        }
        return r;
    }

    public static Float[] box(float[] c) {
        if (c == null) return null;
        Float[] r = new Float[c.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = c[i];
        }
        return r;
    }

    public static float[] unbox(Float[] c) {
        if (c == null) return null;
        float[] r = new float[c.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = c[i];
        }
        return r;
    }

    /// ////


    public static double[] range(double min, double max, double step) {
        if (step == 0 || (step > 0 && max < min) || (step < 0 && min < max)) {
            return new double[0];
        }
        int count = (int) Math.abs((max - min) / step) + 1;
        double[] d = new double[count];
        for (int i = 0; i < d.length; i++) {
            double v = min + i * step;
            d[i] = (step >= 0) ? Math.min(v, max) : Math.max(v, max);
        }
        return d;
    }

    public static int[] range(int min, int max, int step) {
        if (max < min) {
            return new int[0];
        }
        int count = Math.abs((max - min) / step) + 1;
        int[] d = new int[count];
        for (int i = 0; i < d.length; i++) {
            d[i] = min + i * step;
        }
        return d;
    }

    public static int[] range(int min, int max, int step, IntPredicate filter) {
        if (filter == null) {
            return range(min, max, step);
        }
        if (max < min) {
            return new int[0];
        }
        int count = Math.abs((max - min) / step) + 1;
        int[] d = new int[count];
        int idx = 0;
        for (int i = 0; i < count; i++) {
            int v = min + i * step;
            if (filter.test(v)) {
                d[idx++] = v;
            }
        }
        return Arrays.copyOf(d, idx);
    }

    public static int[] linear(int min, int max, int count) {
        int[] d = new int[count];
        if (count == 1) {
            d[0] = min;
        } else {
            for (int i = 0; i < d.length; i++) {
                d[i] = min + i * (max - min) / (count - 1);
            }
        }
        return d;
    }
}
