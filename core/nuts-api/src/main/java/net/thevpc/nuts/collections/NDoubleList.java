package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

/**
 * NDoubleList interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NDoubleList {

    /**
     * Creates a new instance of of.
     *
     * @param initialSize initial size
     * @return of result
     */
    static NDoubleList of(int initialSize) {
        return  NUtilsRPI.of().createDoubleList(initialSize);
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NDoubleList of() {
        return  NUtilsRPI.of().createDoubleList();
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param offset offset
     * @param size size
     * @return of result
     */
    static NDoubleList of(double[] values, int offset, int size) {
        return  NUtilsRPI.of().createDoubleList(values,offset,size);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @return of result
     */
    static NDoubleList of(double[] values) {
        return NUtilsRPI.of().createDoubleList(values,0,values.length);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param size size
     * @return of result
     */
    static NDoubleList of(double[] values, int size) {
        return NUtilsRPI.of().createDoubleList(values,0,size);
    }


    /**
     * Adds add.
     *
     * @param index index
     * @param value value
     */
    void add(int index, double value);

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
    double remove(int index);

    /**
     * Adds the specified all.
     *
     * @param values values
     */
    void addAll(NDoubleList values);

    /**
     * Adds the specified all.
     *
     * @param values values
     */
    void addAll(double... values);

    /**
     * Insert all.
     *
     * @param offset offset
     * @param values values
     */
    void insertAll(int offset, double... values);

    /**
     * Sub list.
     *
     * @param offset offset
     * @param count count
     * @return sub list result
     */
    NDoubleList subList(int offset, int count);

    /**
     * Replace sub list.
     *
     * @param offset offset
     * @param count count
     * @param replacement replacement
     */
    void replaceSubList(int offset, int count, double... replacement);

    /**
     * Adds add.
     *
     * @param value value
     */
    void add(double value);

    /**
     * Trim to size.
     */
    void trimToSize();

    /**
     * Copy.
     *
     * @return copy result
     */
    NDoubleList copy();

    /**
     * Converts to array.
     *
     * @return to array result
     */
    double[] toArray();

    /**
     * Converts to long list.
     *
     * @return to long list result
     */
    NLongList toLongList();

    /**
     * Converts to double array.
     *
     * @return to double array result
     */
    Double[] toDoubleArray();

    /**
     * Converts to double list.
     *
     * @return to double list result
     */
    List<Double> toDoubleList();

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
    double get(int index);

    /**
     * Sets the set.
     *
     * @param index index
     * @param element element
     * @return set result
     */
    double set(int index, double element);

    /**
     * Index of.
     *
     * @param o o
     * @return index of result
     */
    int indexOf(double o);

    /**
     * Contains.
     *
     * @param o o
     * @return contains result
     */
    boolean contains(double o);

    /**
     * Index of.
     *
     * @param o o
     * @param from from
     * @return index of result
     */
    int indexOf(double o, int from);

    /**
     * Index of.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return index of result
     */
    int indexOf(double o, int from, int to);

    /**
     * Contains.
     *
     * @param o o
     * @param from from
     * @return contains result
     */
    boolean contains(double o, int from);

    /**
     * Contains.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return contains result
     */
    boolean contains(double o, int from, int to);

    /**
     * Last index of.
     *
     * @param o o
     * @return last index of result
     */
    int lastIndexOf(double o);

    /**
     * Last index of.
     *
     * @param o o
     * @param from from
     * @return last index of result
     */
    int lastIndexOf(double o, int from);

    /**
     * Last index of.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return last index of result
     */
    int lastIndexOf(double o, int from, int to);

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
