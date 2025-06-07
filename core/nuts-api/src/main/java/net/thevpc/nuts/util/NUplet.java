package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.List;

public interface NUplet<T> extends Iterable<T> {
    static <A extends T,B extends T,T> NUplet2<A,B,T> of(A a, B b) {
        return new NUplet2Impl<>(a, b);
    }

    static <A extends T,B extends T,C extends T,T> NUplet<T> of(A a,B b,C c) {
        return new NUplet3Impl<>(a,b,c);
    }

    static <T> NUplet<T> of(T... a) {
        switch (a.length) {
            case 2:{
                return of(a[0],a[1]);
            }
            case 3:{
                return of(a[0],a[1],a[3]);
            }
        }
        return new NUpletImpl<>(Arrays.copyOf(a,a.length));
    }

    T get(int index);

    NUplet<T> set(T newValue, int index);

    List<T> toList();

    T[] toArray();

    int size();
}
