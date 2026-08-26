package net.thevpc.nuts.util;

import net.thevpc.nuts.reflect.NReflectUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

final class NPairImpl<A extends T, B extends T, T> implements NPair<A, B, T> {
    private A a;
    private B b;

    /**
     * N pair impl.
     *
     * @param a a
     * @param b b
     * @return n pair impl result
     */
    public NPairImpl(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public A first() {
        return a;
    }

    @Override
    public B second() {
        return b;
    }

    @Override
    public T get(int index) {
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

    @Override
    public NPair<A, B, T> set(T newValue, int index) {
        switch (index) {
            case 0:
                return new NPairImpl<>((A) newValue, b);
            case 1:
                return new NPairImpl<>(a, (B) newValue);
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
    public NPair<A, B, T> first(A t) {
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
    public NPair<A, B, T> second(B t) {
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
    public Iterator<T> iterator() {
        return Arrays.asList(a, b).iterator();
    }

    @Override
    public List<T> toList() {
        return Arrays.asList(a, b);
    }

    @Override
    public T[] toArray() {
        Class ca = a == null ? null : a.getClass();
        Class cb = b == null ? null : b.getClass();
        if (ca == null && cb == null) {
            return (T[]) new Object[]{a, b};
        }
        T[] ts = (T[]) Array.newInstance(NReflectUtils.commonAncestor(ca, cb), 2);
        ts[0] = a;
        ts[1] = b;
        return ts;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NPairImpl<?, ?, ?> tuple = (NPairImpl<?, ?, ?>) o;
        return Objects.equals(a, tuple.a) && Objects.equals(b, tuple.b);
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
