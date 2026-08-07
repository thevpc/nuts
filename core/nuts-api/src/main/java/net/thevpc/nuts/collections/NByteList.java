package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

public interface NByteList {

    static NByteList of(int initialSize) {
        return  NUtilsRPI.of().createByteList(initialSize);
    }

    static NByteList of() {
        return  NUtilsRPI.of().createByteList();
    }

    static NByteList of(byte[] values, int offset, int size) {
        return  NUtilsRPI.of().createByteList(values,offset,size);
    }

    static NByteList of(byte[] values) {
        return NUtilsRPI.of().createByteList(values,0,values.length);
    }

    static NByteList of(byte[] values, int size) {
        return NUtilsRPI.of().createByteList(values,0,size);
    }

    void add(int index, byte value);

    int removeAll(int offset, int count);

    byte remove(int index);

    void addAll(NByteList values);

    void addAll(byte... values);

    void insertAll(int offset, byte... values);

    NByteList subList(int offset, int count);

    void replaceSubList(int offset, int count, byte... replacement);

    void add(byte value);

    void trimToSize();

    NByteList copy();

    byte[] toArray();

    NDoubleList toDoubleList();

    NLongList toLongList();

    NIntList toIntList();

    Byte[] toByteArray();

    List<Byte> toByteList();

    int size();

    int get(int index);

    int set(int index, byte element);

    int indexOf(byte o);

    boolean contains(byte o);

    int indexOf(byte o, int from);

    int indexOf(byte o, int from, int to);

    boolean contains(byte o, int from);

    boolean contains(byte o, int from, int to);

    int lastIndexOf(byte o);

    int lastIndexOf(byte o, int from);

    int lastIndexOf(byte o, int from, int to);

    String toStringDebug();

    int committedSize();

    String format(String first, String sep, String last);

    void grow(int minCapacity);

    void ensureSize(int size);
}
