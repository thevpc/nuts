package net.thevpc.nuts.collections;

import net.thevpc.nuts.artifact.NIdLocation;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * NCollections class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NCollections {
    /**
     * Append.
     *
     * @param a1 a1
     * @param b1 b1
     * @return append result
     */
    public static <A> List<A> append(Collection<A> a1, A b1) {
        List<A> li = new ArrayList<>(a1);
        li.add(b1);
        return li;
    }

    /**
     * Append all.
     *
     * @param a1 a1
     * @param b1 b1
     * @return append all result
     */
    public static <A> List<A> appendAll(Collection<A> a1, Collection<A> b1) {
        List<A> li = new ArrayList<>(a1);
        li.addAll(b1);
        return li;
    }

    /**
     * Prepend.
     *
     * @param b1 b1
     * @param a1 a1
     * @return prepend result
     */
    public static <A> List<A> prepend(A b1, Collection<A> a1) {
        List<A> li = new ArrayList<>();
        li.add(b1);
        li.addAll(a1);
        return li;
    }

    /**
     * List.
     *
     * @param it it
     * @return list result
     */
    public static <T> List<T> list(Collection<T> it) {
        return new ArrayList<>(it);
    }

    /**
     * List.
     *
     * @param it it
     * @return list result
     */
    public static <T> List<T> list(Iterable<T> it) {
        /**
         * List.
         *
         * @param it.iterator() it.iterator()
         * @return list result
         */
        return list(it.iterator());
    }

    /**
     * List.
     *
     * @param it it
     * @return list result
     */
    public static <T> List<T> list(Iterator<T> it) {
        List<T> all = new ArrayList<>();
        while (it.hasNext()) {
            all.add(it.next());
        }
        return all;
    }

    /**
     * List.
     *
     * @param it it
     * @return list result
     */
    public static <T> List<T> list(Enumeration<T> it) {
        List<T> all = new ArrayList<>();
        while (it.hasMoreElements()) {
            all.add(it.nextElement());
        }
        return all;
    }

    /**
     * Stream.
     *
     * @param it it
     * @return stream result
     */
    public static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }

    /**
     * Stream.
     *
     * @param iterator iterator
     * @return stream result
     */
    public static <T> Stream<T> stream(Iterator<T> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false);
    }

    /**
     * Distinct by key.
     *
     * @param distinctMapper distinct mapper
     * @return distinct by key result
     */
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> distinctMapper) {
        Map<Object, Boolean> visited = new ConcurrentHashMap<>();
        return t -> visited.putIfAbsent(distinctMapper.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Converts to distinct stripped non empty list.
     *
     * @param values0 values0
     * @return to distinct stripped non empty list result
     */
    public static List<String> toDistinctStrippedNonEmptyList(List<String> values0) {
        Set<String> set = toStrippedNonEmptySet(
                values0 == null ? null : values0.toArray(new String[0])
        );
        return new ArrayList<>(set);
    }

    /**
     * Converts to distinct stripped non empty list.
     *
     * @param values0 values0
     * @param values  values
     * @return to distinct stripped non empty list result
     */
    public static List<String> toDistinctStrippedNonEmptyList(List<String> values0, List<String>... values) {
        Set<String> set = toStrippedNonEmptySet(
                values0 == null ? null : values0.toArray(new String[0])
        );
        if (values != null) {
            for (List<String> value : values) {
                set.addAll(toStrippedNonEmptySet(
                        values0 == null ? null : values0.toArray(new String[0])
                ));
            }
        }
        return new ArrayList<>(set);
    }

    /**
     * Adds the specified all non null.
     *
     * @param container   container
     * @param newElements new elements
     * @return add all non null result
     */
    public static <T> boolean addAllNonNull(Collection<T> container, Collection<T> newElements) {
        boolean someAdded = false;
        if (newElements != null) {
            for (T t : newElements) {
                if (t != null) {
                    container.add(t);
                    someAdded = true;
                }
            }
        }
        return someAdded;
    }

    /**
     * Unmodifiable list.
     *
     * @param other other
     * @return unmodifiable list result
     */
    public static <T> List<T> unmodifiableList(Collection<T> other) {
        return other == null ? Collections.emptyList() : Collections.unmodifiableList(nonNullList(other));
    }

    /**
     * Unmodifiable non null list.
     *
     * @param other other
     * @return unmodifiable non null list result
     */
    public static <T> List<T> unmodifiableNonNullList(Collection<T> other) {
        /**
         * Unmodifiable list.
         *
         * @param other other
         * @param Objects::nonNull objects::non null
         * @return unmodifiable list result
         */
        return unmodifiableList(other, Objects::nonNull);
    }

    /**
     * Unmodifiable list.
     *
     * @param other  other
     * @param filter filter
     * @return unmodifiable list result
     */
    public static <T> List<T> unmodifiableList(Collection<T> other, Predicate<T> filter) {
        if (other == null) {
            return Collections.emptyList();
        }
        if (filter == null) {
            return Collections.unmodifiableList(new ArrayList<>(other));
        }
        return Collections.unmodifiableList(
                other.stream()
                        .filter(filter)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Unmodifiable non null set.
     *
     * @param other other
     * @return unmodifiable non null set result
     */
    public static <T> Set<T> unmodifiableNonNullSet(Collection<T> other) {
        /**
         * Unmodifiable set.
         *
         * @param other other
         * @param Objects::nonNull objects::non null
         * @return unmodifiable set result
         */
        return unmodifiableSet(other, Objects::nonNull);
    }

    /**
     * Unmodifiable set.
     *
     * @param other  other
     * @param filter filter
     * @return unmodifiable set result
     */
    public static <T> Set<T> unmodifiableSet(Collection<T> other, Predicate<T> filter) {
        if (other == null) {
            return Collections.emptySet();
        }
        if (filter == null) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(other)); // preserve order
        }
        return Collections.unmodifiableSet(
                other.stream()
                        .filter(filter)
                        .collect(Collectors.toSet())
        );
    }


    /**
     * Non null list.
     *
     * @param other other
     * @return non null list result
     */
    public static <T> List<T> nonNullList(Collection<T> other) {
        if (other == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(other);
    }

    /**
     * Converts to stripped non empty set.
     *
     * @param values0 values0
     * @return to stripped non empty set result
     */
    public static Set<String> toStrippedNonEmptySet(String[] values0) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                a = NStringUtils.strip(a);
                if (!NBlankable.isBlank(a)) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    /**
     * Converts to distinct stripped non empty list.
     *
     * @param values0 values0
     * @return to distinct stripped non empty list result
     */
    public static ArrayList<String> toDistinctStrippedNonEmptyList(String[] values0) {
        return new ArrayList<>(toStrippedNonEmptySet(values0));
    }

    /**
     * Converts to set.
     *
     * @param classifierMappings classifier mappings
     * @return to set result
     */
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


    /**
     * Non null list from array.
     *
     * @param other other
     * @return non null list from array result
     */
    public static <T> List<T> nonNullListFromArray(T[] other) {
        /**
         * Non null list.
         *
         * @param Arrays.asList(other) arrays.as list(other)
         * @return non null list result
         */
        return nonNullList(Arrays.asList(other));
    }

    /**
     * Finite stream.
     *
     * @param supplier supplier
     * @return finite stream result
     */
    public static <T> Stream<T> finiteStream(Supplier<T> supplier) {
        /**
         * Stream.
         *
         * @param supplier supplier
         * @param null null
         * @return stream result
         */
        return stream(supplier, null);
    }

    /**
     * Stream.
     *
     * @param supplier      supplier
     * @param stopCondition stop condition
     * @return stream result
     */
    public static <T> Stream<T> stream(Supplier<T> supplier, Predicate<T> stopCondition) {
        if (stopCondition == null) {
            stopCondition = Objects::isNull;
        }
        Predicate<T> finalStopCondition = stopCondition;
        /**
         * Stream.
         *
         * @param Iterator<T>( iterator<t>(
         * @return stream result
         */
        return stream(new Iterator<T>() {
            T value;

            @Override
            public boolean hasNext() {
                value = supplier.get();
                return !finalStopCondition.test(value);
            }

            @Override
            public T next() {
                return value;
            }
        });
    }


    /// /////////////


    /**
     * Head.
     *
     * @param anyList any list
     * @param maxSize max size
     * @return head result
     */
    public static <T> List<T> head(List<T> anyList, int maxSize) {
        if (maxSize < 0) {
            maxSize = anyList.size() + maxSize;
        }
        if (anyList.size() > maxSize) {
            return anyList.subList(0, maxSize);
        }
        return anyList;
    }

    /**
     * Tail.
     *
     * @param anyList any list
     * @param maxSize max size
     * @return tail result
     */
    public static <T> List<T> tail(List<T> anyList, int maxSize) {
        if (anyList.size() > maxSize) {
            return anyList.subList(anyList.size() - maxSize, maxSize);
        }
        return anyList;
    }

    /**
     * Split by.
     *
     * @param anyList   any list
     * @param groupSize group size
     * @return split by result
     */
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

    /**
     * Group by.
     *
     * @param anyList   any list
     * @param groupSize group size
     * @return group by result
     */
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

    /**
     * Convert.
     *
     * @param list      list
     * @param converter converter
     * @return convert result
     */
    public static <A, B> List<B> convert(List<A> list, Function<A, B> converter) {
        return NUtilsRPI.of().createImmutableConvertedList(list, converter);
    }

    /**
     * Filter.
     *
     * @param collection collection
     * @param filter     filter
     * @return filter result
     */
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


    /**
     * Unmodifiable list.
     *
     * @param list list
     * @return unmodifiable list result
     */
    public static <V> List<V> unmodifiableList(List<V> list) {
        return list == null ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    /**
     * Unmodifiable list or null.
     *
     * @param list list
     * @return unmodifiable list or null result
     */
    public static <V> List<V> unmodifiableListOrNull(List<V> list) {
        return list == null ? null : Collections.unmodifiableList(list);
    }

    /**
     * Unmodifiable collection.
     *
     * @param list list
     * @return unmodifiable collection result
     */
    public static <V> Collection<V> unmodifiableCollection(Collection<V> list) {
        return list == null ? Collections.emptyList() : Collections.unmodifiableCollection(list);
    }

    /**
     * Unmodifiable collection or null.
     *
     * @param list list
     * @return unmodifiable collection or null result
     */
    public static <V> Collection<V> unmodifiableCollectionOrNull(Collection<V> list) {
        return list == null ? null : Collections.unmodifiableCollection(list);
    }

    /**
     * Retain all.
     *
     * @param values values
     * @param filter filter
     * @return retain all result
     */
    public static <T> Collection<T> retainAll(Collection<T> values, Predicate<T> filter) {
        if (filter == null) {
            /**
             * Null pointer exception.
             *
             * @param null" null"
             * @return null pointer exception result
             */
            throw new NullPointerException("Filter could not be null");
        }
        for (Iterator<T> i = values.iterator(); i.hasNext(); ) {
            if (!filter.test(i.next())) {
                i.remove();
            }
        }
        return values;
    }


    /**
     * Removes the specified all.
     *
     * @param values values
     * @param filter filter
     * @return remove all result
     */
    public static <T> Collection<T> removeAll(Collection<T> values, Predicate<T> filter) {
        if (filter == null) {
            /**
             * Null pointer exception.
             *
             * @param null" null"
             * @return null pointer exception result
             */
            throw new NullPointerException("Filter could not be null");
        }
        for (Iterator<T> i = values.iterator(); i.hasNext(); ) {
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
            /**
             * Null pointer exception.
             *
             * @param converter" converter"
             * @return null pointer exception result
             */
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


    /**
     * Converts to set.
     *
     * @param values0     values0
     * @param strip       strip
     * @param ignoreEmpty ignore empty
     * @param ignoreNull  ignore null
     * @return to set result
     */
    public static Set<String> toSet(String[] values0, boolean strip, boolean ignoreEmpty, boolean ignoreNull) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                if (a != null) {
                    if (strip) {
                        a = NStringUtils.strip(a);
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

}
