package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

public interface NDoubleList {

    static NDoubleList of(int initialSize) {
        return  NUtilsRPI.of().createDoubleList(initialSize);
    }

    static NDoubleList of() {
        return  NUtilsRPI.of().createDoubleList();
    }

    static NDoubleList of(double[] values, int offset, int size) {
        return  NUtilsRPI.of().createDoubleList(values,offset,size);
    }

    static NDoubleList of(double[] values) {
        return NUtilsRPI.of().createDoubleList(values,0,values.length);
    }

    static NDoubleList of(double[] values, int size) {
        return NUtilsRPI.of().createDoubleList(values,0,size);
    }


    void add(int index, double value);

    int removeAll(int offset, int count);

    double remove(int index);

    void addAll(NDoubleList values);

    void addAll(double... values);

    void insertAll(int offset, double... values);

    NDoubleList subList(int offset, int count);

    void replaceSubList(int offset, int count, double... replacement);

    void add(double value);

    void trimToSize();

    NDoubleList copy();

    double[] toArray();

    NLongList toLongList();

    Double[] toDoubleArray();

    List<Double> toDoubleList();

    int size();

    double get(int index);

    double set(int index, double element);

    int indexOf(double o);

    boolean contains(double o);

    int indexOf(double o, int from);

    int indexOf(double o, int from, int to);

    boolean contains(double o, int from);

    boolean contains(double o, int from, int to);

    int lastIndexOf(double o);

    int lastIndexOf(double o, int from);

    int lastIndexOf(double o, int from, int to);

    String toStringDebug();

    int committedSize();

    String format(String first, String sep, String last);

    void grow(int minCapacity);

    void ensureSize(int size);
}
