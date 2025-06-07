package net.thevpc.nuts.util;

public interface NUplet2<A extends T, B extends T, T> extends NUplet<T> {
    A first();

    B second();

    NUplet2<A, B, T> set(T t, int index);

    NUplet2<A, B, T> setFirst(A t);

    NUplet2<A, B, T> setSecond(B t);
}
