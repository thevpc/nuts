package net.thevpc.nuts.reflect;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;
import net.thevpc.nuts.util.NUplet;

import java.util.List;
import java.util.Set;

public interface NClassPairMultiMap<A, B, V> {
    static <A, B, V> NClassPairMultiMap<A, B, V> of(Class<V> clazz) {
        return of(null, null, clazz, false);
    }

    static <A, B, V> NClassPairMultiMap<A, B, V> of(Class<A> key1Type, Class<B> key2Type, Class<V> valueType, boolean symmetric) {
        return NCollectionsRPI.of().classPairMultiMap(key1Type, key2Type, valueType, symmetric);
    }

    Set<NUplet<Class>> keySet();

    void add(Class<? extends A> a, Class<? extends B> b, V value);

    void remove(Class<? extends A> a, Class<? extends B> b, V value);

    /**
     * Returns the "Best Match" bucket.
     * Finds the closest list in the hierarchy and returns its contents.
     */
    List<V> get(Class<? extends A> a, Class<? extends B> b);

    /**
     * Returns the "Cumulative Match" list.
     * Finds ALL buckets in the hierarchy and flattens them into one list.
     */
    List<V> findMatches(Class<? extends A> a, Class<? extends B> b);

    /**
     * Returns only the bucket for the exact class pair.
     */
    List<V> getExact(Class<? extends A> a, Class<? extends B> b);

    void clear(Class<? extends A> a, Class<? extends B> b);

    void clear();

}
