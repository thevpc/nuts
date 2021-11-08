/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.core.commands.ws;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsIteratorStream;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class AbstractNutsStream<T> implements NutsStream<T> {

    protected NutsSession session;
    protected String nutsBase;

    public AbstractNutsStream(NutsSession session, String nutsBase) {
        this.session = session;
        this.nutsBase = nutsBase;
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("duplicate key %s", u));
        };
    }

    @Override
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (T a : this) {
            list.add(a);
        }
        return list;
    }

    @Override
    public Set<T> toSet() {
        return stream().collect(Collectors.toSet());
    }

    @Override
    public Set<T> toSortedSet() {
        return stream().collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<T> toOrderedSet() {
        return stream().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public T first() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public T last() {
        T t = null;
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            t = it.next();
        }
        return t;
    }

    @Override
    public T required() throws NutsNotFoundException {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        NutsId n = NutsIdParser.of(session).setLenient(true).parse(nutsBase);
        if (n != null) {
            throw new NutsNotFoundException(session, n);
        }
        throw new NutsNotFoundException(session, null, NutsMessage.cstyle("artifact not found: %s%s", (nutsBase == null ? "<null>" : nutsBase)), null);
    }

    @Override
    public T singleton() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            T t = it.next();
            if (it.hasNext()) {
                throw new NutsTooManyElementsException(session, NutsMessage.cstyle("too many results for %s", nutsBase));
            }
            return t;
        } else {
            NutsId nid = NutsIdParser.of(session).setLenient(true).parse(nutsBase);
            if (nid != null) {
                throw new NutsNotFoundException(session, nid);
            }
            throw new NutsNotFoundException(session, null, NutsMessage.cstyle("result not found for %s", nutsBase));
        }
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize((Iterator<T>) iterator(), Spliterator.ORDERED), false);
    }

    @Override
    public long count() {
        long count = 0;
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            count++;
        }
        return count;
    }

    @Override
    public <R> NutsStream<R> map(Function<? super T, ? extends R> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public Iterator<R> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.map(it, mapper);
            }
        };
    }

    @Override
    public NutsStream<T> sorted() {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public Iterator<T> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.sort(it, null, false);
            }
        };
    }

    @Override
    public NutsStream<T> sorted(Comparator<T> comp) {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public Iterator<T> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.sort(it, comp, false);
            }
        };
    }

    @Override
    public NutsStream<T> distinct() {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public Iterator<T> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.distinct(it);
            }
        };
    }

    @Override
    public <R> NutsStream<T> distinctBy(Function<T, R> condition) {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public Iterator<T> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.distinct(it, condition);
            }
        };
    }

    @Override
    public NutsStream<T> nonNull() {
        return filter(Objects::nonNull);
    }

    @Override
    public NutsStream<T> nonBlank() {
        return filter(x -> {
            if (x == null) {
                return false;
            }
            if (x instanceof CharSequence) {
                return NutsBlankable.isBlank((CharSequence) x);
            }
            if (x instanceof char[]) {
                return NutsBlankable.isBlank((char[]) x);
            }
            if (x instanceof NutsBlankable) {
                return !((NutsBlankable) x).isBlank();
            }
            return true;
        });
    }

    @Override
    public NutsStream<T> filter(Predicate<? super T> predicate) {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public Iterator<T> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.filter(it, predicate);//,"mapped("+it+")"
            }
        };
    }

    @Override
    public NutsStream<T> coalesce(Iterator<? extends T> other) {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public Iterator<T> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                List<Iterator<? extends T>> iterators = Arrays.asList(it, other);
                return IteratorUtils.coalesce(iterators);//,"mapped("+it+")"
            }
        };
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream().toArray(generator);
    }

    @Override
    public <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends U> valueMapper) {
        return stream().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    @Override
    public <K, U> Map<K, U> toOrderedMap(Function<? super T, ? extends K> keyMapper,
                                         Function<? super T, ? extends U> valueMapper) {
        return stream().collect(Collectors.toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new));
    }

    @Override
    public <K, U> Map<K, U> toSortedMap(Function<? super T, ? extends K> keyMapper,
                                        Function<? super T, ? extends U> valueMapper) {
        return stream().collect(Collectors.toMap(keyMapper, valueMapper, throwingMerger(), TreeMap::new));
    }

    @Override
    public <R> NutsStream<R> flatMapIter(Function<? super T, ? extends Iterator<? extends R>> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public Iterator<R> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.flatMap(it, mapper);//,"mapped("+it+")"
            }
        };
    }

    @Override
    public <R> NutsStream<R> flatMapList(Function<? super T, ? extends List<? extends R>> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public Iterator<R> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.flatMap(it, t -> mapper.apply(t).iterator());
            }
        };
    }

    @Override
    public <R> NutsStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public Iterator<R> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.flatMap(it, t -> Arrays.asList(mapper.apply(t))
                        .iterator());
            }
        };
    }

    @Override
    public <R> NutsStream<R> flatMapStream(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public Iterator<R> iterator() {
                Iterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.flatMap(it, t -> mapper.apply(t).iterator());//,"mapped("+it+")"
            }
        };
    }

    @Override
    public <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
        Stream<T> it = AbstractNutsStream.this.stream();
        return it.collect(Collectors.groupingBy(classifier));
    }

    @Override
    public <K> NutsStream<Map.Entry<K, List<T>>> groupedBy(Function<? super T, ? extends K> classifier) {
        Stream<T> it = AbstractNutsStream.this.stream();
        Set<Map.Entry<K, List<T>>> entries = (Set) it.collect(Collectors.groupingBy(classifier)).entrySet();
        return new NutsIteratorStream<Map.Entry<K, List<T>>>(
                session, nutsBase, entries.iterator()
        );
    }

    @Override
    public Optional<T> findAny() {
        return stream().findAny();
    }

    @Override
    public Optional<T> findFirst() {
        return stream().findFirst();
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream().flatMapToDouble(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream().flatMapToLong(mapper);
    }


    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return stream().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return stream().noneMatch(predicate);
    }

    @Override
    public NutsStream<T> limit(long maxSize) {
        return NutsStream.of(stream().limit(maxSize),session);
    }




}
