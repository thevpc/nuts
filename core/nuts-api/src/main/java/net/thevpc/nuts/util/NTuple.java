package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.List;

public interface NTuple<T> extends Iterable<T> {
    static <A extends T,B extends T,T> NPair<A,B,T> of(A a, B b) {
        return new NPairImpl<>(a, b);
    }

    static <A extends T,B extends T,C extends T,T> NTuple<T> of(A a, B b, C c) {
        return new NTripletImpl<>(a,b,c);
    }

    static <T> NTuple<T> of(T... a) {
        switch (a.length) {
            case 2:{
                return of(a[0],a[1]);
            }
            case 3:{
                return of(a[0],a[1],a[3]);
            }
        }
        return new NTupleImpl<>(Arrays.copyOf(a,a.length));
    }

    T get(int index);

    NTuple<T> set(T newValue, int index);

    List<T> toList();

    T[] toArray();

    int size();
}
