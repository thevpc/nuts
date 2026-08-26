package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * NIntPair class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public final class NIntPair implements NPair<Integer, Integer, Integer> {
    private final int a;
    private final int b;

    /**
     * Creates a new instance of of.
     *
     * @param a a
     * @param b b
     * @return of result
     */
    public static NIntPair of(int a, int b) {
        return new NIntPair(a, b);
    }

    /**
     * N int pair.
     *
     * @param a a
     * @param b b
     * @return n int pair result
     */
    public NIntPair(int a, int b) {
        this.a = a;
        this.b = b;
    }

    /**
     * First int.
     *
     * @return first int result
     */
    public int firstInt() {
        return a;
    }

    /**
     * Second int.
     *
     * @return second int result
     */
    public int secondInt() {
        return b;
    }

    @Override
    public Integer first() {
        return a;
    }

    @Override
    public Integer second() {
        return b;
    }

    @Override
    public Integer get(int index) {
        switch (index) {
            case 0:
                return a;
            case 1:
                return b;
        }
        /**
         * Array index out of bounds exception.
         *
         * @param index index
         * @return array index out of bounds exception result
         */
        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * Sets the set.
     *
     * @param newValue new value
     * @param index index
     * @return set result
     */
    public NIntPair set(int newValue, int index) {
        switch (index) {
            case 0:
                return new NIntPair(newValue, b);
            case 1:
                return new NIntPair(a, newValue);
        }
        /**
         * Array index out of bounds exception.
         *
         * @param index index
         * @return array index out of bounds exception result
         */
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public NPair<Integer, Integer, Integer> set(Integer newValue, int index) {
        if (newValue == null) {
            switch (index) {
                case 0:
                    return new NIntPair(newValue, b);
                case 1:
                    return new NIntPair(a, newValue);
            }
        } else {
            switch (index) {
                case 0:
                    return new NIntPair(newValue, b);
                case 1:
                    return new NIntPair(a, newValue);
            }
        }
        /**
         * Array index out of bounds exception.
         *
         * @param index index
         * @return array index out of bounds exception result
         */
        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * Sets the first.
     *
     * @param t t
     * @return set first result
     */
    public NIntPair setFirst(int t) {
        /**
         * Sets the set.
         *
         * @param t t
         * @param 0 0
         * @return set result
         */
        return set(t, 0);
    }

    /**
     * Sets the second.
     *
     * @param t t
     * @return set second result
     */
    public NIntPair setSecond(int t) {
        /**
         * Sets the set.
         *
         * @param t t
         * @param 0 0
         * @return set result
         */
        return set(t, 0);
    }

    @Override
    public NPair<Integer, Integer, Integer> first(Integer t) {
        /**
         * Sets the set.
         *
         * @param t t
         * @param 0 0
         * @return set result
         */
        return set(t, 0);
    }

    @Override
    public NPair<Integer, Integer, Integer> second(Integer t) {
        /**
         * Sets the set.
         *
         * @param t t
         * @param 1 1
         * @return set result
         */
        return set(t, 1);
    }

    @Override
    public Iterator<Integer> iterator() {
        return Arrays.asList(a, b).iterator();
    }

    @Override
    public List<Integer> toList() {
        return Arrays.asList(a, b);
    }

    @Override
    public Integer[] toArray() {
        return new Integer[]{a, b};
    }

    /**
     * Converts to int array.
     *
     * @return to int array result
     */
    public int[] toIntArray() {
        return new int[]{a, b};
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NIntPair tuple = (NIntPair) o;
      /**
       * Return.
       *
       * @param tuple.b tuple.b
       */
        return (a == tuple.a) && (b == tuple.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ')';
    }
}
