package net.thevpc.nuts.util;

public interface NUplet3<A extends T,B extends T,C extends T,T> extends NUplet<T> {
    A first();

    B second();

    C third();

    NUplet3<A,B,C,T> set(T t, int index);

    NUplet3<A,B,C,T> setFirst(A t);

    NUplet3<A,B,C,T> setSecond(B t);

    NUplet3<A,B,C,T> setThird(C t);

}
