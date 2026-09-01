package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

/**
 * NLongList interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NLongList {
    /**
     * Creates a new instance of of.
     *
     * @param initialSize initial size
     * @return of result
     */
    static NLongList of(int initialSize) {
        return  NUtilsRPI.of().createLongList(initialSize);
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NLongList of() {
        return  NUtilsRPI.of().createLongList();
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param offset offset
     * @param size size
     * @return of result
     */
    static NLongList of(long[] values, int offset, int size) {
        return  NUtilsRPI.of().createLongList(values,offset,size);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @return of result
     */
    static NLongList of(long[] values) {
        return NUtilsRPI.of().createLongList(values,0,values.length);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param size size
     * @return of result
     */
    static NLongList of(long[] values, int size) {
        return NUtilsRPI.of().createLongList(values,0,size);
    }
    /**
     * Adds add.
     *
     * @param index index
     * @param value value
     */
    NLongList add(int index, long value);

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
    long remove(int index);

    /**
     * Adds the specified all.
     *
     * @param values values
     */
    NLongList addAll(NLongList values);

    /**
     * Adds the specified all.
     *
     * @param values values
     */
    NLongList addAll(long... values);

    /**
     * Insert all.
     *
     * @param offset offset
     * @param values values
     */
    NLongList insertAll(int offset, long... values);

    /**
     * Sub list.
     *
     * @param offset offset
     * @param count count
     * @return sub list result
     */
    NLongList subList(int offset, int count);

    /**
     * Replace sub list.
     *
     * @param offset offset
     * @param count count
     * @param replacement replacement
     */
    NLongList replaceSubList(int offset, int count, long... replacement);

    /**
     * Adds add.
     *
     * @param value value
     */
    NLongList add(long value);

    /**
     * Trim to size.
     */
    NLongList trimToSize();

    /**
     * Copy.
     *
     * @return copy result
     */
    NLongList copy();

    /**
     * Converts to array.
     *
     * @return to array result
     */
    long[] toArray();

    /**
     * Converts to double list.
     *
     * @return to double list result
     */
    NDoubleList toDoubleList();

    /**
     * Converts to int list.
     *
     * @return to int list result
     */
    NIntList toIntList();

    /**
     * Converts to long array.
     *
     * @return to long array result
     */
    Long[] toLongArray();

    /**
     * Converts to integer list.
     *
     * @return to integer list result
     */
    List<Long> toIntegerList();

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
    long get(int index);

    /**
     * Sets the set.
     *
     * @param index index
     * @param element element
     * @return set result
     */
    long set(int index, long element);

    /**
     * Index of.
     *
     * @param o o
     * @return index of result
     */
    int indexOf(long o);

    /**
     * Contains.
     *
     * @param o o
     * @return contains result
     */
    boolean contains(long o);

    /**
     * Index of.
     *
     * @param o o
     * @param from from
     * @return index of result
     */
    int indexOf(long o, int from);

    /**
     * Index of.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return index of result
     */
    int indexOf(long o, int from, int to);

    /**
     * Contains.
     *
     * @param o o
     * @param from from
     * @return contains result
     */
    boolean contains(long o, int from);

    /**
     * Contains.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return contains result
     */
    boolean contains(long o, int from, int to);

    /**
     * Last index of.
     *
     * @param o o
     * @return last index of result
     */
    int lastIndexOf(long o);

    /**
     * Last index of.
     *
     * @param o o
     * @param from from
     * @return last index of result
     */
    int lastIndexOf(long o, int from);

    /**
     * Last index of.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return last index of result
     */
    int lastIndexOf(long o, int from, int to);

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
    NLongList grow(int minCapacity);

    /**
     * Ensure size.
     *
     * @param size size
     */
    NLongList ensureSize(int size);
}
