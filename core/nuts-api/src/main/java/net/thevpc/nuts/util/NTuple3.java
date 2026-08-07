package net.thevpc.nuts.util;

public interface NTuple3<A extends T,B extends T,C extends T,T> extends NTuple<T> {
    A first();

    B second();

    C third();

    NTuple3<A,B,C,T> set(T t, int index);

    NTuple3<A,B,C,T> setFirst(A t);

    NTuple3<A,B,C,T> setSecond(B t);

    NTuple3<A,B,C,T> setThird(C t);

}
