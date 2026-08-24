package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

/**
 * NByteList interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NByteList {

    /**
     * Creates a new instance of of.
     *
     * @param initialSize initial size
     * @return of result
     */
    static NByteList of(int initialSize) {
        return  NUtilsRPI.of().createByteList(initialSize);
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NByteList of() {
        return  NUtilsRPI.of().createByteList();
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param offset offset
     * @param size size
     * @return of result
     */
    static NByteList of(byte[] values, int offset, int size) {
        return  NUtilsRPI.of().createByteList(values,offset,size);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @return of result
     */
    static NByteList of(byte[] values) {
        return NUtilsRPI.of().createByteList(values,0,values.length);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param size size
     * @return of result
     */
    static NByteList of(byte[] values, int size) {
        return NUtilsRPI.of().createByteList(values,0,size);
    }

    /**
     * Adds add.
     *
     * @param index index
     * @param value value
     */
    void add(int index, byte value);

    /**
     * Removes the specified all.
     *
     * @param offset offset
     * @param count count
     * @return remove all result
     */
    int removeAll(int offset, int count);

    /**
     * Removes remove.
     *
     * @param index index
     * @return remove result
     */
    byte remove(int index);

    /**
     * Adds the specified all.
     *
     * @param values values
     */
    void addAll(NByteList values);

    /**
     * Adds the specified all.
     *
     * @param values values
     */
    void addAll(byte... values);

    /**
     * Insert all.
     *
     * @param offset offset
     * @param values values
     */
    void insertAll(int offset, byte... values);

    /**
     * Sub list.
     *
     * @param offset offset
     * @param count count
     * @return sub list result
     */
    NByteList subList(int offset, int count);

    /**
     * Replace sub list.
     *
     * @param offset offset
     * @param count count
     * @param replacement replacement
     */
    void replaceSubList(int offset, int count, byte... replacement);

    /**
     * Adds add.
     *
     * @param value value
     */
    void add(byte value);

    /**
     * Trim to size.
     */
    void trimToSize();

    /**
     * Copy.
     *
     * @return copy result
     */
    NByteList copy();

    /**
     * Converts to array.
     *
     * @return to array result
     */
    byte[] toArray();

    /**
     * Converts to double list.
     *
     * @return to double list result
     */
    NDoubleList toDoubleList();

    /**
     * Converts to long list.
     *
     * @return to long list result
     */
    NLongList toLongList();

    /**
     * Converts to int list.
     *
     * @return to int list result
     */
    NIntList toIntList();

    /**
     * Converts to byte array.
     *
     * @return to byte array result
     */
    Byte[] toByteArray();

    /**
     * Converts to byte list.
     *
     * @return to byte list result
     */
    List<Byte> toByteList();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Returns the get.
     *
     * @param index index
     * @return get result
     */
    int get(int index);

    /**
     * Sets the set.
     *
     * @param index index
     * @param element element
     * @return set result
     */
    int set(int index, byte element);

    /**
     * Index of.
     *
     * @param o o
     * @return index of result
     */
    int indexOf(byte o);

    /**
     * Contains.
     *
     * @param o o
     * @return contains result
     */
    boolean contains(byte o);

    /**
     * Index of.
     *
     * @param o o
     * @param from from
     * @return index of result
     */
    int indexOf(byte o, int from);

    /**
     * Index of.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return index of result
     */
    int indexOf(byte o, int from, int to);

    /**
     * Contains.
     *
     * @param o o
     * @param from from
     * @return contains result
     */
    boolean contains(byte o, int from);

    /**
     * Contains.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return contains result
     */
    boolean contains(byte o, int from, int to);

    /**
     * Last index of.
     *
     * @param o o
     * @return last index of result
     */
    int lastIndexOf(byte o);

    /**
     * Last index of.
     *
     * @param o o
     * @param from from
     * @return last index of result
     */
    int lastIndexOf(byte o, int from);

    /**
     * Last index of.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return last index of result
     */
    int lastIndexOf(byte o, int from, int to);

    /**
     * Converts to string debug.
     *
     * @return to string debug result
     */
    String toStringDebug();

    /**
     * Committed size.
     *
     * @return committed size result
     */
    int committedSize();

    /**
     * Format.
     *
     * @param first first
     * @param sep sep
     * @param last last
     * @return format result
     */
    String format(String first, String sep, String last);

    /**
     * Grow.
     *
     * @param minCapacity min capacity
     */
    void grow(int minCapacity);

    /**
     * Ensure size.
     *
     * @param size size
     */
    void ensureSize(int size);
}
