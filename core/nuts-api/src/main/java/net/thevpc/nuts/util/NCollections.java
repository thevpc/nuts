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


    /// /////////////


    public static <T> List<T> head(List<T> anyList, int maxSize) {
        if (maxSize < 0) {
            maxSize = anyList.size() + maxSize;
        }
        if (anyList.size() > maxSize) {
            return anyList.subList(0, maxSize);
        }
        return anyList;
    }

    public static <T> List<T> tail(List<T> anyList, int maxSize) {
        if (anyList.size() > maxSize) {
            return anyList.subList(anyList.size() - maxSize, maxSize);
        }
        return anyList;
    }

    public static <T> List<List<T>> splitBy(Collection<T> anyList, int groupSize) {
        List<List<T>> grouped = new ArrayList<List<T>>();
        for (int i = 0; i < groupSize; i++) {
            grouped.add(new ArrayList<T>());
        }
        if (anyList != null) {
            int i = 0;
            for (T item : anyList) {
                grouped.get(i % groupSize).add(item);
                i++;
            }
        }
        return grouped;
    }

    public static <T> List<List<T>> groupBy(Collection<T> anyList, int groupSize) {
        List<List<T>> grouped = new ArrayList<List<T>>();
        List<T> curr = new ArrayList<T>();
        if (anyList != null) {
            for (T item : anyList) {
                if (curr.size() < groupSize) {
                    curr.add(item);
                } else {
                    grouped.add(curr);
                    curr = new ArrayList<T>();
                    curr.add(item);
                }
            }
        }
        if (curr.size() > 0) {
            grouped.add(curr);
        }
        return grouped;
    }

    public static <A, B> List<B> convert(List<A> list, Function<A, B> converter) {
        return new NImmutableConvertedList<A, B>(list, converter);
    }

    public static <T> List<T> filter(Collection<T> collection, NCollectionFilter<T> filter) {
        ArrayList<T> ret = new ArrayList<T>();
        int i = 0;
        for (T t : collection) {
            if (filter.accept(t, i, collection)) {
                ret.add(t);
            }
            i++;
        }
        return ret;
    }

    public static <T> List<T> toList(Iterator<T> it) {
        List<T> all = new ArrayList<>();
        while (it.hasNext()) {
            all.add(it.next());
        }
        return all;
    }

    public static <T> List<T> toList(Iterable<T> it) {
        return toList(it.iterator());
    }




    public static <V> List<V> unmodifiableList(List<V> list) {
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    public static <V> List<V> unmodifiableListOrNull(List<V> list) {
        return list == null ? null : Collections.unmodifiableList(list);
    }

    public static <V> Collection<V> unmodifiableCollection(Collection<V> list) {
        return list == null ? Collections.emptyList() : Collections.unmodifiableCollection(list);
    }

    public static <V> Collection<V> unmodifiableCollectionOrNull(Collection<V> list) {
        return list == null ? null : Collections.unmodifiableCollection(list);
    }

    public static <T> Collection<T> retainAll(Collection<T> values, Predicate<T> filter) {
        if (filter == null) {
            throw new NullPointerException("Filter could not be null");
        }
        for (Iterator<T> i = values.iterator(); i.hasNext();) {
            if (!filter.test(i.next())) {
                i.remove();
            }
        }
        return values;
    }


    public static <T> Collection<T> removeAll(Collection<T> values, Predicate<T> filter) {
        if (filter == null) {
            throw new NullPointerException("Filter could not be null");
        }
        for (Iterator<T> i = values.iterator(); i.hasNext();) {
            if (filter.test(i.next())) {
                i.remove();
            }
        }
        return values;
    }



    /**
     * created a view on the List where each element is replaced by it converter
     *
     * @param from
     * @param converter
     * @param <F>
     * @param <T>
     * @return
     */
    public <F, T> List<T> convertList(final List<F> from, final Function<F, T> converter) {
        if (converter == null) {
            throw new NullPointerException("Null converter");
        }
        return new AbstractList<T>() {
            @Override
            public T get(int index) {
                F value = from.get(index);
                return converter.apply(value);
            }

            @Override
            public T remove(int index) {
                F removed = from.remove(index);
                if (removed == null) {
                    return null;
                }
                return converter.apply(removed);
            }

            @Override
            public int size() {
                return from.size();
            }
        };
    }

    public static <K, V> Map<K, V> mergeMaps(Map<K, V> source, Map<K, V> dest) {
        if (dest == null) {
            dest = new HashMap<>();
        }
        if (source != null) {
            for (Map.Entry<K, V> e : source.entrySet()) {
                if (e.getValue() != null) {
                    dest.put(e.getKey(), e.getValue());
                } else {
                    dest.remove(e.getKey());
                }
            }
        }
        return dest;
    }

    public static Set<String> toSet(String[] values0, boolean trim, boolean ignoreEmpty, boolean ignoreNull) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                if (a != null) {
                    if (trim) {
                        a = a.trim();
                    }
                    if (a.isEmpty()) {
                        a = null;
                    }
                }
                if (a == null && ignoreNull) {
                    continue;
                }
                if (a != null && a.isEmpty() && ignoreEmpty) {
                    continue;
                }
                set.add(a);
            }
        }
        return set;
    }



//    public static <K, V> KeyValueList<K, V> unmodifiableMapList(KeyValueList<K, V> list) {
//        return list == null ? null : new UnmodifiableKeyValueList<K, V>(list);
//    }
}
