package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

public interface NLongList {
    static NLongList of(int initialSize) {
        return  NUtilsRPI.of().createLongList(initialSize);
    }

    static NLongList of() {
        return  NUtilsRPI.of().createLongList();
    }

    static NLongList of(long[] values, int offset, int size) {
        return  NUtilsRPI.of().createLongList(values,offset,size);
    }

    static NLongList of(long[] values) {
        return NUtilsRPI.of().createLongList(values,0,values.length);
    }

    static NLongList of(long[] values, int size) {
        return NUtilsRPI.of().createLongList(values,0,size);
    }
    void add(int index, long value);

    int removeAll(int offset, int count);

    long remove(int index);

    void addAll(NLongList values);

    void addAll(long... values);

    void insertAll(int offset, long... values);

    NLongList subList(int offset, int count);

    void replaceSubList(int offset, int count, long... replacement);

    void add(long value);

    void trimToSize();

    NLongList copy();

    long[] toArray();

    NDoubleList toDoubleList();

    NIntList toIntList();

    Long[] toLongArray();

    List<Long> toIntegerList();

    int size();

    long get(int index);

    long set(int index, long element);

    int indexOf(long o);

    boolean contains(long o);

    int indexOf(long o, int from);

    int indexOf(long o, int from, int to);

    boolean contains(long o, int from);

    boolean contains(long o, int from, int to);

    int lastIndexOf(long o);

    int lastIndexOf(long o, int from);

    int lastIndexOf(long o, int from, int to);

    String toStringDebug();

    int committedSize();

    String format(String first, String sep, String last);

    void grow(int minCapacity);

    void ensureSize(int size);
}
