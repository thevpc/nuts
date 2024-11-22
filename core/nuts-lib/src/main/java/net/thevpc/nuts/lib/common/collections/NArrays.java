package net.thevpc.nuts.lib.common.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class NArrays {
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

    ////////////////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////////////////
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

    ////////////////////////////////////////////////////////////////////
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
        return (T[]) Arrays.copyOf(original, original.length);
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
}
