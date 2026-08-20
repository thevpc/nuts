package net.thevpc.nuts.util;

public interface NPair<A extends T, B extends T, T> extends NTuple<T> {
    A first();

    B second();

    NPair<A, B, T> set(T t, int index);

    NPair<A, B, T> first(A t);

    NPair<A, B, T> second(B t);
}
