package net.thevpc.nuts.util;

public interface NTuple2<A extends T, B extends T, T> extends NTuple<T> {
    A first();

    B second();

    NTuple2<A, B, T> set(T t, int index);

    NTuple2<A, B, T> setFirst(A t);

    NTuple2<A, B, T> setSecond(B t);
}
