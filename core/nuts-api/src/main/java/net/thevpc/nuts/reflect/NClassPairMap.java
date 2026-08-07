package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NTuple;

import java.util.List;
import java.util.Set;

public interface NClassPairMap<A,B,V> {

    static <A,B,V> NClassPairMap<A,B,V> of(Class<V> clazz) {
        return of(null, null, clazz, false);
    }

    static <A,B,V> NClassPairMap<A,B,V> of(Class<A> key1Type, Class<B> key2Type, Class<V> valueType, boolean symmetric) {
        return NUtilsRPI.of().classPairMap(key1Type, key2Type, valueType, symmetric);
    }


    Set<NTuple<Class>> keySet();

    V put(Class<? extends A> classKey1, Class<? extends B> classKey2, V value);

    V remove(Class<? extends A> classKey1, Class<? extends B> classKey2);

    List<NTuple<Class>> getSearchPath(Class<? extends A> classKey1, Class<? extends B> classKey2);

    V getExact(Class<? extends A> classKey1, Class<? extends B> classKey2);

    V get(Class<? extends A> classKey1, Class<? extends B> classKey2);

    List<V> findMatches(Class<? extends A> classKey1, Class<? extends B> classKey2);

    boolean isEmpty();
    boolean clear();
}
