package net.thevpc.nuts.util;

import net.thevpc.nuts.artifact.NIdLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

    public static <T> Stream<T> stream(Iterator<T> iterator){
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false);
    }
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> distinctMapper) {
        Map<Object, Boolean> visited = new ConcurrentHashMap<>();
        return t -> visited.putIfAbsent(distinctMapper.apply(t), Boolean.TRUE) == null;
    }

    public static List<String> toDistinctTrimmedNonEmptyList(List<String> values0) {
        Set<String> set = toTrimmedNonEmptySet(
                values0==null?null:values0.toArray(new String[0])
        );
        return new ArrayList<>(set);
    }
    public static List<String> toDistinctTrimmedNonEmptyList(List<String> values0, List<String>... values) {
        Set<String> set = toTrimmedNonEmptySet(
                values0==null?null:values0.toArray(new String[0])
        );
        if (values != null) {
            for (List<String> value : values) {
                set.addAll(toTrimmedNonEmptySet(
                        values0==null?null:values0.toArray(new String[0])
                ));
            }
        }
        return new ArrayList<>(set);
    }

    public static <T> boolean addAllNonNull(Collection<T> container, Collection<T> newElements) {
        boolean someAdded = false;
        if(newElements !=null){
            for (T t : newElements) {
                if(t!=null){
                    container.add(t);
                    someAdded=true;
                }
            }
        }
        return someAdded;
    }

    public static <T> List<T> unmodifiableList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(nonNullList(other));
    }

    public static <T, V> Map<T, V> nonNullMap(Map<T, V> other) {
        if (other == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(other);
    }


    public static <T> List<T> nonNullList(Collection<T> other) {
        if (other == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(other);
    }

    public static Set<String> toTrimmedNonEmptySet(String[] values0) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                a = NStringUtils.trim(a);
                if (!NBlankable.isBlank(a)) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static ArrayList<String> toDistinctTrimmedNonEmptyList(String[] values0) {
        return new ArrayList<>(toTrimmedNonEmptySet(values0));
    }

    public static Set<NIdLocation> toSet(NIdLocation[] classifierMappings) {
        LinkedHashSet<NIdLocation> set = new LinkedHashSet<>();
        if (classifierMappings != null) {
            for (NIdLocation a : classifierMappings) {
                if (a != null) {
                    set.add(a);
                }
            }
        }
        return set;
    }



    public static <T, V> Map<T, V> unmodifiableMap(Map<T, V> other) {
        return other == null ? Collections.emptyMap() : Collections.unmodifiableMap(nonNullMap(other));
    }

    public static <T> List<T> nonNullListFromArray(T[] other) {
        return nonNullList(Arrays.asList(other));
    }

    public static <T> Stream<T> finiteStream(Supplier<T> supplier){
        return stream(supplier, null);
    }
    public static <T> Stream<T> stream(Supplier<T> supplier, Predicate<T> stopCondition){
        if(stopCondition==null){
            stopCondition= Objects::isNull;
        }
        Predicate<T> finalStopCondition = stopCondition;
        return stream(new Iterator<T>() {
            T value;
            @Override
            public boolean hasNext() {
                value = supplier.get();
                if(finalStopCondition.test(value)){
                    return false;
                }
                return true;
            }

            @Override
            public T next() {
                return value;
            }
        });
    }

}
