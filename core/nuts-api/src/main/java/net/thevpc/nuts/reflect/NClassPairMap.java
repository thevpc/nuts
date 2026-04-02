package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;
import net.thevpc.nuts.util.NUplet;

import java.util.List;
import java.util.Set;

public interface NClassPairMap<A,B,V> {

    static <A,B,V> NClassPairMap<A,B,V> of(Class<V> clazz) {
        return of(null, null, clazz, false);
    }

    static <A,B,V> NClassPairMap<A,B,V> of(Class<A> key1Type, Class<B> key2Type, Class<V> valueType, boolean symmetric) {
        return NCollectionsRPI.of().classPairMap(key1Type, key2Type, valueType, symmetric);
    }


    Set<NUplet<Class>> keySet();

    V put(Class<? extends A> classKey1, Class<? extends B> classKey2, V value);

    V remove(Class<? extends A> classKey1, Class<? extends B> classKey2);

    List<NUplet<Class>> getSearchPath(Class<? extends A> classKey1, Class<? extends B> classKey2);

    V getExact(Class<? extends A> classKey1, Class<? extends B> classKey2);

    V get(Class<? extends A> classKey1, Class<? extends B> classKey2);

    List<V> findMatches(Class<? extends A> classKey1, Class<? extends B> classKey2);

    boolean isEmpty();
    boolean clear();
}
