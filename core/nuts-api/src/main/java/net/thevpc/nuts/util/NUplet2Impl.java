package net.thevpc.nuts.util;

import net.thevpc.nuts.reflect.NReflectUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

final class NUplet2Impl<A extends T, B extends T, T> implements NUplet2<A, B, T> {
    private A a;
    private B b;

    public NUplet2Impl(A a, B b) {
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
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public NUplet2<A, B, T> set(T newValue, int index) {
        switch (index) {
            case 0:
                return new NUplet2Impl<>((A) newValue, b);
            case 1:
                return new NUplet2Impl<>(a, (B) newValue);
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public NUplet2<A, B, T> setFirst(A t) {
        return set(t, 0);
    }

    @Override
    public NUplet2<A, B, T> setSecond(B t) {
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
        NUplet2Impl<?, ?, ?> nUplet2 = (NUplet2Impl<?, ?, ?>) o;
        return Objects.equals(a, nUplet2.a) && Objects.equals(b, nUplet2.b);
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
