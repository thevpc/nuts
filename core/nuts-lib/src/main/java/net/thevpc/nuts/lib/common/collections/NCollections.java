package net.thevpc.nuts.lib.common.collections;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class NCollections {
    public static <A> List<A> append(Collection<A> a1, A b1) {
        List<A> li = new ArrayList<>(a1);
        li.add(b1);
        return li;
    }

    public static <A> List<A> appendAll(Collection<A> a1, Collection<A> b1) {
        List<A> li = new ArrayList<>(a1);
        li.addAll(b1);
        return li;
    }

    public static <A> List<A> prepend(A b1, Collection<A> a1) {
        List<A> li = new ArrayList<>();
        li.add(b1);
        li.addAll(a1);
        return li;
    }

    public static <T> List<T> list(Collection<T> it) {
        return new ArrayList<>(it);
    }

    public static <T> List<T> list(Iterable<T> it) {
        return list(it.iterator());
    }

    public static <T> List<T> list(Iterator<T> it) {
        List<T> all = new ArrayList<>();
        while (it.hasNext()) {
            all.add(it.next());
        }
        return all;
    }

    public static <T> List<T> list(Enumeration<T> it) {
        List<T> all = new ArrayList<>();
        while (it.hasMoreElements()) {
            all.add(it.nextElement());
        }
        return all;
    }

    public static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }

    public static <T> Stream<T> stream(Iterator<T> it) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(it, 0), false);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> distinctMapper) {
        Map<Object, Boolean> visited = new ConcurrentHashMap<>();
        return t -> visited.putIfAbsent(distinctMapper.apply(t), Boolean.TRUE) == null;
    }
}
