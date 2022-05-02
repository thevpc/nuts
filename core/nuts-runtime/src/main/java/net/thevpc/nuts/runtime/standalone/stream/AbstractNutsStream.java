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
package net.thevpc.nuts.runtime.standalone.stream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.util.NutsDescribables;
import net.thevpc.nuts.util.*;

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
        NutsId n = NutsId.of(nutsBase).orNull();
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
            NutsId nid = NutsId.of(nutsBase).orNull();
            if (nid != null) {
                throw new NutsNotFoundException(session, nid);
            }
            throw new NutsNotFoundException(session, null, NutsMessage.cstyle("result not found for %s", nutsBase));
        }
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

    @Override
    public long count() {
        long count = 0;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }

    @Override
    public <R> NutsStream<R> map(NutsFunction<? super T, ? extends R> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public NutsIterator<R> iterator() {
                NutsIterator<T> it = AbstractNutsStream.this.iterator();
                return (NutsIterator) IteratorBuilder.of(it, session).map(mapper).build();
            }
        };
    }

    @Override
    public <R> NutsStream<R> map(Function<? super T, ? extends R> mapper, String name) {
        return map(NutsFunction.of(mapper, name));
    }

    @Override
    public <R> NutsStream<R> map(Function<? super T, ? extends R> mapper, NutsElement name) {
        return map(NutsFunction.of(mapper, name));
    }

    @Override
    public <R> NutsStream<R> map(Function<? super T, ? extends R> mapper, Function<NutsSession, NutsElement> name) {
        return map(NutsFunction.of(mapper, name));
    }

    @Override
    public <R> NutsStream<R> mapUnsafe(NutsUnsafeFunction<? super T, ? extends R> mapper, NutsFunction<Exception, ? extends R> onError) {
        return map(new NutsFunction<T, R>() {
            @Override
            public R apply(T t) {
                try {
                    return mapper.apply(t);
                } catch (Exception e) {
                    return onError == null ? null : onError.apply(e);
                }
            }

            @Override
            public NutsElement describe(NutsSession session) {
                return mapper.describe(session);
            }
        });
    }

    @Override
    public NutsStream<T> sorted() {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public NutsIterator<T> iterator() {
                NutsIterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.sort(it, null, false);
            }
        };
    }

    @Override
    public NutsStream<T> sorted(NutsComparator<T> comp) {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public NutsIterator<T> iterator() {
                NutsIterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.sort(it, comp, false);
            }
        };
    }

    @Override
    public NutsStream<T> distinct() {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public NutsIterator<T> iterator() {
                NutsIterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.distinct(it);
            }
        };
    }

    @Override
    public <R> NutsStream<T> distinctBy(NutsFunction<T, R> condition) {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public NutsIterator<T> iterator() {
                NutsIterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorUtils.distinct(it, condition);
            }
        };
    }

    @Override
    public NutsStream<T> nonNull() {
        return filter(Objects::nonNull, "nonNull");
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
        }, "nonBlank");
    }

    @Override
    public NutsStream<T> filter(NutsPredicate<? super T> predicate) {
        NutsDescribables.cast(predicate);
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public NutsIterator<T> iterator() {
                NutsIterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorBuilder.of(it, session).filter(predicate).build();//,"mapped("+it+")"
            }
        };
    }

    @Override
    public NutsStream<T> filter(Predicate<? super T> predicate, String name) {
        return filter(predicate, e -> NutsElements.of(e).ofString(name));
    }

    @Override
    public NutsStream<T> filter(Predicate<? super T> predicate, NutsElement name) {
        return filter(predicate, e -> name);
    }

    @Override
    public NutsStream<T> filter(Predicate<? super T> predicate, Function<NutsSession, NutsElement> info) {
        NutsPredicate<? super T> p = predicate == null ? null : NutsPredicate.of(predicate, info);
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public NutsIterator<T> iterator() {
                NutsIterator<T> it = AbstractNutsStream.this.iterator();
                return IteratorBuilder.of(it, session).filter(p).build();//,"mapped("+it+")"
            }
        };
    }

    @Override
    public NutsStream<T> filterNonNull() {
        return filter(Objects::nonNull, "nonNull");
    }

    @Override
    public NutsStream<T> filterNonBlank() {
        return filter(x -> !NutsBlankable.isBlank(x), "nonBlank");
    }

    @Override
    public NutsStream<T> coalesce(NutsIterator<? extends T> other) {
        return new AbstractNutsStream<T>(session, nutsBase) {
            @Override
            public NutsIterator<T> iterator() {
                NutsIterator<T> it = AbstractNutsStream.this.iterator();
                List<NutsIterator<? extends T>> iterators = Arrays.asList(it, other);
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
    public <R> NutsStream<R> flatMapIter(NutsFunction<? super T, ? extends Iterator<? extends R>> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public NutsIterator<R> iterator() {
                return IteratorBuilder.of(AbstractNutsStream.this.iterator(), session).flatMap(mapper).build();
            }
        };
    }

    @Override
    public <R> NutsStream<R> flatMapList(NutsFunction<? super T, ? extends List<? extends R>> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public NutsIterator<R> iterator() {
                IteratorBuilder<T> r = IteratorBuilder.of(AbstractNutsStream.this.iterator(), session);
                return (NutsIterator<R>) r.flatMap(
                        NutsFunction.of(t -> mapper.apply(t).iterator(),mapper::describe)
                ).build();
            }
        };
    }

    @Override
    public <R> NutsStream<R> flatMapArray(NutsFunction<? super T, ? extends R[]> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public NutsIterator<R> iterator() {
                return IteratorBuilder.of(AbstractNutsStream.this.iterator(), session)
                        .flatMap(
                                NutsFunction.of(t -> Arrays.asList(mapper.apply(t)).iterator(), mapper::describe)
                        ).build();
            }
        };
    }

    @Override
    public <R> NutsStream<R> flatMap(NutsFunction<? super T, ? extends Stream<? extends R>> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public NutsIterator<R> iterator() {
                return (NutsIterator<R>) IteratorBuilder.of(AbstractNutsStream.this.iterator(), session).flatMap(
                        NutsFunction.of(t -> mapper.apply(t).iterator(),mapper::describe)
                ).build();
            }
        };
    }

    @Override
    public <R> NutsStream<R> flatMapStream(NutsFunction<? super T, ? extends NutsStream<? extends R>> mapper) {
        return new AbstractNutsStream<R>(session, nutsBase) {
            @Override
            public NutsIterator<R> iterator() {
                return (NutsIterator<R>) IteratorBuilder.of(AbstractNutsStream.this.iterator(), session)
                        .flatMap(
                                NutsFunction.of(t -> mapper.apply(t).iterator(),mapper::describe)
                                ).build();
            }
        };
    }

    @Override
    public <K> Map<K, List<T>> groupBy(NutsFunction<? super T, ? extends K> classifier) {
        Stream<T> it = AbstractNutsStream.this.stream();
        return it.collect(Collectors.groupingBy(classifier));
    }

    @Override
    public <K> NutsStream<Map.Entry<K, List<T>>> groupedBy(NutsFunction<? super T, ? extends K> classifier) {
        Stream<T> it = AbstractNutsStream.this.stream();
        Set<Map.Entry<K, List<T>>> entries = (Set) it.collect(Collectors.groupingBy(classifier)).entrySet();
        return new NutsIteratorStream<Map.Entry<K, List<T>>>(
                session, nutsBase, NutsIterator.of(entries.iterator(),
                e -> NutsElements.of(e).ofObject()
                        .set("type", "GroupBy")
                        .set("groupBy", classifier.describe(e))
                        .set("base", iterator().describe(e))
                        .build()
        )
        );
    }

    @Override
    public NutsOptional<T> findAny() {
        return NutsOptional.ofOptional(stream().findAny(),s->NutsMessage.cstyle("missing : %S",nutsBase));
    }

    @Override
    public NutsOptional<T> findFirst() {
        return NutsOptional.ofOptional(stream().findFirst(),s->NutsMessage.cstyle("missing : %S",nutsBase));
    }

    @Override
    public DoubleStream flatMapToDouble(NutsFunction<? super T, ? extends DoubleStream> mapper) {
        return stream().flatMapToDouble(mapper);
    }

    @Override
    public IntStream flatMapToInt(NutsFunction<? super T, ? extends IntStream> mapper) {
        return stream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(NutsFunction<? super T, ? extends LongStream> mapper) {
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
        return NutsStream.of(stream().limit(maxSize), session);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream().collect(collector);
    }

    @Override
    public NutsOptional<T> min(Comparator<? super T> comparator) {
        return NutsOptional.ofOptional(stream().min(comparator),s->NutsMessage.cstyle("missing : %S",nutsBase));
    }

    @Override
    public NutsOptional<T> max(Comparator<? super T> comparator) {
        return NutsOptional.ofOptional(stream().max(comparator),s->NutsMessage.cstyle("missing : %S",nutsBase));
    }

    @Override
    public NutsElement describe(NutsSession session) {
        return iterator().describe(session);
    }
}
