package net.thevpc.nuts.util;

import net.thevpc.nuts.reflect.NReflectUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

final class NUplet3Impl<A extends T, B extends T, C extends T, T> implements NUplet3<A, B, C, T>,NImmutable {
    private final A a;
    private final B b;
    private final C c;

    public NUplet3Impl(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
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
    public C third() {
        return c;
    }

    @Override
    public T get(int index) {
        switch (index) {
            case 0:
                return a;
            case 1:
                return b;
            case 2:
                return c;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }


    @Override
    public NUplet3<A, B, C, T> set(T newValue, int index) {
        switch (index) {
            case 0:
                return new NUplet3Impl<>((A) newValue, b, c);
            case 1:
                return new NUplet3Impl<>(a, (B) newValue, c);
            case 2:
                return new NUplet3Impl<>(a, b, (C) newValue);
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    @Override
    public NUplet3<A, B, C, T> setFirst(A t) {
        return set(t, 0);
    }

    @Override
    public NUplet3<A, B, C, T> setSecond(B t) {
        return set(t, 1);
    }

    @Override
    public NUplet3<A, B, C, T> setThird(C t) {
        return set(t, 2);
    }

    @Override
    public Iterator<T> iterator() {
        return Arrays.asList(a, b, c).iterator();
    }

    @Override
    public List<T> toList() {
        return Arrays.asList(a, b, c);
    }

    @Override
    public T[] toArray() {
        Class ca = a == null ? null : a.getClass();
        Class cb = b == null ? null : b.getClass();
        Class cc = c == null ? null : c.getClass();
        if (ca == null && cb == null && cc == null) {
            return (T[]) new Object[]{a, b, c};
        }
        T[] ts = (T[]) Array.newInstance(NReflectUtils.commonAncestor(ca, cb, cc), 3);
        ts[0] = a;
        ts[1] = b;
        ts[2] = c;
        return ts;
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NUplet3Impl<?, ?, ?, ?> nUplet3 = (NUplet3Impl<?, ?, ?, ?>) o;
        return Objects.equals(a, nUplet3.a) && Objects.equals(b, nUplet3.b) && Objects.equals(c, nUplet3.c);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c);
    }

    @Override
    public String toString() {
        return "(" +
                a +
                ", " + b +
                ", " + c +
                ')';
    }
}
