package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

public interface NIntList {

    static NIntList of(int initialSize) {
        return  NUtilsRPI.of().createIntList(initialSize);
    }

    static NIntList of() {
        return  NUtilsRPI.of().createIntList();
    }

    static NIntList of(int[] values, int offset, int size) {
        return  NUtilsRPI.of().createIntList(values,offset,size);
    }

    static NIntList of(int[] values) {
        return NUtilsRPI.of().createIntList(values,0,values.length);
    }

    static NIntList of(int[] values, int size) {
        return NUtilsRPI.of().createIntList(values,0,size);
    }

    void add(int index, int value);

    int removeAll(int offset, int count);

    int remove(int index);

    void addAll(NIntList values);

    void addAll(int... values);

    void insertAll(int offset, int... values);

    NIntList subList(int offset, int count);

    void replaceSubList(int offset, int count, int... replacement);

    void add(int value);

    void trimToSize();

    NIntList copy();

    int[] toArray();

    NDoubleList toDoubleList();

    NLongList toLongList();

    Integer[] toIntegerArray();

    List<Integer> toIntegerList();

    int size();

    int get(int index);

    int set(int index, int element);

    int indexOf(int o);

    boolean contains(int o);

    int indexOf(int o, int from);

    int indexOf(int o, int from, int to);

    boolean contains(int o, int from);

    boolean contains(int o, int from, int to);

    int lastIndexOf(int o);

    int lastIndexOf(int o, int from);

    int lastIndexOf(int o, int from, int to);

    String toStringDebug();

    int committedSize();

    String format(String first, String sep, String last);

    void grow(int minCapacity);

    void ensureSize(int size);
}
