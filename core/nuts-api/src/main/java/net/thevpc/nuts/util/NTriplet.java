package net.thevpc.nuts.util;

public interface NTriplet<A extends T,B extends T,C extends T,T> extends NTuple<T> {
    A first();

    B second();

    C third();

    NTriplet<A,B,C,T> set(T t, int index);

    NTriplet<A,B,C,T> first(A t);

    NTriplet<A,B,C,T> second(B t);

    NTriplet<A,B,C,T> third(C t);

}
