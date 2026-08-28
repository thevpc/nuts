package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;

import java.util.List;

/**
 * NDoubleList interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NFloatList {

    /**
     * Creates a new instance of of.
     *
     * @param initialSize initial size
     * @return of result
     */
    static NFloatList of(int initialSize) {
        return  NUtilsRPI.of().createFloatList(initialSize);
    }

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NFloatList of() {
        return  NUtilsRPI.of().createFloatList();
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param offset offset
     * @param size size
     * @return of result
     */
    static NFloatList of(float[] values, int offset, int size) {
        return  NUtilsRPI.of().createFloatList(values,offset,size);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @return of result
     */
    static NFloatList of(float[] values) {
        return NUtilsRPI.of().createFloatList(values,0,values.length);
    }

    /**
     * Creates a new instance of of.
     *
     * @param values values
     * @param size size
     * @return of result
     */
    static NFloatList of(float[] values, int size) {
        return NUtilsRPI.of().createFloatList(values,0,size);
    }


    /**
     * Adds add.
     *
     * @param index index
     * @param value value
     */
    NFloatList add(int index, float value);

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
    float remove(int index);

    /**
     * Adds the specified all.
     *
     * @param values values
     */
    NFloatList addAll(NFloatList values);

    /**
     * Adds the specified all.
     *
     * @param values values
     */
    NFloatList addAll(float... values);

    /**
     * Insert all.
     *
     * @param offset offset
     * @param values values
     */
    NFloatList insertAll(int offset, float... values);

    /**
     * Sub list.
     *
     * @param offset offset
     * @param count count
     * @return sub list result
     */
    NFloatList subList(int offset, int count);

    /**
     * Replace sub list.
     *
     * @param offset offset
     * @param count count
     * @param replacement replacement
     */
    NFloatList replaceSubList(int offset, int count, float... replacement);

    /**
     * Adds add.
     *
     * @param value value
     */
    NFloatList add(float value);

    /**
     * Trim to size.
     */
    NFloatList trimToSize();

    /**
     * Copy.
     *
     * @return copy result
     */
    NFloatList copy();

    /**
     * Converts to array.
     *
     * @return to array result
     */
    float[] toArray();

    /**
     * Converts to long list.
     *
     * @return to long list result
     */
    NLongList toLongList();
    NDoubleList toDoubleList();

    /**
     * Converts to float array.
     *
     * @return to float array result
     */
    Float[] toFloatArray();

    /**
     * Converts to float list.
     *
     * @return to float list result
     */
    List<Float> toFloatList();

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
    float get(int index);

    /**
     * Sets the set.
     *
     * @param index index
     * @param element element
     * @return set result
     */
    float set(int index, float element);

    /**
     * Index of.
     *
     * @param o o
     * @return index of result
     */
    int indexOf(float o);

    /**
     * Contains.
     *
     * @param o o
     * @return contains result
     */
    boolean contains(float o);

    /**
     * Index of.
     *
     * @param o o
     * @param from from
     * @return index of result
     */
    int indexOf(float o, int from);

    /**
     * Index of.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return index of result
     */
    int indexOf(float o, int from, int to);

    /**
     * Contains.
     *
     * @param o o
     * @param from from
     * @return contains result
     */
    boolean contains(float o, int from);

    /**
     * Contains.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return contains result
     */
    boolean contains(float o, int from, int to);

    /**
     * Last index of.
     *
     * @param o o
     * @return last index of result
     */
    int lastIndexOf(float o);

    /**
     * Last index of.
     *
     * @param o o
     * @param from from
     * @return last index of result
     */
    int lastIndexOf(float o, int from);

    /**
     * Last index of.
     *
     * @param o o
     * @param from from
     * @param to to
     * @return last index of result
     */
    int lastIndexOf(float o, int from, int to);

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
    NFloatList grow(int minCapacity);

    /**
     * Ensure size.
     *
     * @param size size
     */
    NFloatList ensureSize(int size);
}
