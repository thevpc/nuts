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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.util.NIteratorUtils;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class NStreamBase<T> implements NStream<T> {

    protected String nutsBase;

    public NStreamBase(String nutsBase) {
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
    public NOptional<T> findLast() {
        T t = null;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            t = it.next();
        }
        return NOptional.ofEmpty(() ->
                nutsBase == null ?
                        NMsg.ofPlain("missing last") :
                        NMsg.ofC("missing last %s", nutsBase)
        );
    }

    @Override
    public NOptional<T> findSingleton() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            T t = it.next();
            if (it.hasNext()) {
                return NOptional.ofError(
                        () -> NMsg.ofC("too many results for %s", nutsBase),
                        new NTooManyElementsException(NMsg.ofC("too many results for %s", nutsBase))
                );
            }
            return NOptional.of(t);
        } else {
            return NOptional.ofEmpty(() ->
                    nutsBase == null ?
                            NMsg.ofPlain("missing") :
                            NMsg.ofC("missing %s", nutsBase)
            );
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
    public <R> NStream<R> map(Function<? super T, ? extends R> mapper) {
        return new NStreamBase<R>(nutsBase) {
            @Override
            public NIterator<R> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                return (NIterator) NIteratorBuilder.of(it).map(NFunction.of(mapper)).build();
            }
        };
    }

    @Override
    public <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper, Function<Exception, ? extends R> onError) {
        return map(NFunction.ofUnsafe((UnsafeFunction<? super T, R>) mapper, onError));
    }

    @Override
    public <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper) {
        return mapUnsafe(mapper, null);
    }

    @Override
    public NStream<T> sorted() {
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                return NIteratorUtils.sort(it, null, false)
                        ;
            }
        };
    }

    @Override
    public NStream<T> sorted(NComparator<T> comp) {
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                return NIteratorUtils.sort(it, comp, false)
                        ;
            }
        };
    }

    @Override
    public NStream<T> distinct() {
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                return NIteratorUtils.distinct(it)
                        ;
            }
        };
    }

    @Override
    public <R> NStream<T> distinctBy(Function<T, R> condition) {
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                return NIteratorUtils.distinct(it, condition)
                        ;
            }
        };
    }

    @Override
    public NStream<T> nonNull() {
        return filter(Objects::nonNull).redescribe(NDescribables.ofDesc("nonNull"));
    }

    @Override
    public NStream<T> nonBlank() {
        return filter(x -> {
            if (x == null) {
                return false;
            }
            if (x instanceof CharSequence) {
                return NBlankable.isBlank((CharSequence) x);
            }
            if (x instanceof char[]) {
                return NBlankable.isBlank((char[]) x);
            }
            if (x instanceof NBlankable) {
                return !((NBlankable) x).isBlank();
            }
            return true;
        }).redescribe(NDescribables.ofDesc("nonBlank"));
    }

    @Override
    public NStream<T> filter(Predicate<? super T> predicate) {
//        NDescribables.cast(predicate);
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                return NIteratorBuilder.of(it).filter(NPredicate.of(predicate)).build()
                        ;//,"mapped("+it+")"
            }
        };
    }

    @Override
    public NStream<T> filterNonNull() {
        return filter(Objects::nonNull).redescribe(NDescribables.ofDesc("nonNull"));
    }

    @Override
    public NStream<T> filterNonBlank() {
        return filter(x -> !NBlankable.isBlank(x)).redescribe(NDescribables.ofDesc("nonBlank"));
    }

    @Override
    public NStream<T> coalesce(NIterator<? extends T> other) {
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                List<NIterator<? extends T>> iterators = Arrays.asList(it, other);
                return NIteratorUtils.coalesce(iterators)
                        ;//,"mapped("+it+")"
            }
        };
    }

    @Override
    public NStream<T> concat(NIterator<? extends T> other) {
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                List<NIterator<? extends T>> iterators = Arrays.asList(it, other);
                return NIteratorUtils.concat(iterators)
                        ;//,"mapped("+it+")"
            }
        };
    }

    @Override
    public NStream<T> coalesce(NStream<? extends T> other) {
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                List<NIterator<? extends T>> iterators = Arrays.asList(it, other.iterator());
                return NIteratorUtils.coalesce(iterators)
                        ;//,"mapped("+it+")"
            }
        };
    }

    @Override
    public NStream<T> concat(NStream<? extends T> other) {
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> it = NStreamBase.this.iterator();
                List<NIterator<? extends T>> iterators = Arrays.asList(it, other.iterator());
                return NIteratorUtils.concat(iterators)
                        ;//,"mapped("+it+")"
            }
        };
    }

    @Override
    public boolean[] toBooleanArray() {
        Boolean[] b = toArray(Boolean[]::new);
        boolean[] c = new boolean[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = b[i];
        }
        return c;
    }

    @Override
    public byte[] toByteArray() {
        Number[] b = toArray(Number[]::new);
        byte[] c = new byte[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = b[i].byteValue();
        }
        return c;
    }

    @Override
    public char[] toCharArray() {
        Character[] b = toArray(Character[]::new);
        char[] c = new char[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = b[i];
        }
        return c;
    }

    @Override
    public short[] toShortArray() {
        Short[] b = toArray(Short[]::new);
        short[] c = new short[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = b[i];
        }
        return c;
    }

    @Override
    public float[] toFloatArray() {
        Float[] b = toArray(Float[]::new);
        float[] c = new float[b.length];
        for (int i = 0; i < b.length; i++) {
            c[i] = b[i];
        }
        return c;
    }

    @Override
    public int[] toIntArray() {
        return mapToInt(x->((Number)x).intValue()).toArray();
    }

    @Override
    public long[] toLongArray() {
        return mapToLong(x->((Number)x).longValue()).toArray();
    }

    @Override
    public double[] toDoubleArray() {
        return mapToDouble(x->((Number)x).doubleValue()).toArray();
    }


    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper){
        return stream().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper){
        return stream().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper){
        return stream().mapToDouble(mapper);
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
    public <R> NStream<R> flatMapIter(Function<? super T, ? extends Iterator<? extends R>> mapper) {
        return new NStreamBase<R>(nutsBase) {
            @Override
            public NIterator<R> iterator() {
                return NIteratorBuilder.of(NStreamBase.this.iterator()).flatMap(mapper).build();
            }
        };
    }

    @Override
    public <R> NStream<R> flatMapList(Function<? super T, ? extends List<? extends R>> mapper) {
        return new NStreamBase<R>(nutsBase) {
            @Override
            public NIterator<R> iterator() {
                NIteratorBuilder<T> r = NIteratorBuilder.of(NStreamBase.this.iterator());
                return (NIterator<R>) r.flatMap(
                        NFunction.of(tt -> mapper.apply((T) tt).iterator()).redescribe(() -> NFunction.of(mapper).describe())
                ).build()
                        ;
            }
        };
    }

    @Override
    public <R> NStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper) {
        return new NStreamBase<R>(nutsBase) {
            @Override
            public NIterator<R> iterator() {
                return NIteratorBuilder.of(NStreamBase.this.iterator())
                        .flatMap(
                                NFunction.of(t -> Arrays.asList(mapper.apply((T) t)).iterator())
                                        .redescribe(() -> NFunction.of(mapper).describe())
                        ).build()
                        ;
            }
        };
    }

    @Override
    public <R> NStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return new NStreamBase<R>(nutsBase) {
            @Override
            public NIterator<R> iterator() {
                return (NIterator<R>) NIteratorBuilder.of(NStreamBase.this.iterator()).flatMap(
                        NFunction.of(t -> mapper.apply(t).iterator())
                ).build().redescribe(() -> NFunction.of(mapper).describe());
            }
        };
    }

    @Override
    public <R> NStream<R> flatMapStream(Function<? super T, ? extends NStream<? extends R>> mapper) {
        return new NStreamBase<R>(nutsBase) {
            @Override
            public NIterator<R> iterator() {
                return (NIterator<R>) NIteratorBuilder.of(NStreamBase.this.iterator())
                        .flatMap(
                                NFunction.of(t -> mapper.apply(t).iterator())
                        ).build().redescribe(() -> NFunction.of(mapper).describe())
                        ;
            }
        };
    }

    @Override
    public <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
        Stream<T> it = NStreamBase.this.stream();
        return it.collect(Collectors.groupingBy(classifier));
    }

    @Override
    public <K> NStream<Map.Entry<K, List<T>>> groupedBy(Function<? super T, ? extends K> classifier) {
        Stream<T> it = NStreamBase.this.stream();
        Set<Map.Entry<K, List<T>>> entries = (Set) it.collect(Collectors.groupingBy(classifier)).entrySet();
        return new NStreamFromNIterator<Map.Entry<K, List<T>>>(
                nutsBase, NIterator.of(entries.iterator()).redescribe(
                () -> NElement.ofObjectBuilder()
                        .name("GroupBy")
                        .set("by", NFunction.of(classifier).describe())
                        .set("base", iterator().describe())
                        .build()
        )
        );
    }

    @Override
    public NOptional<T> findAny() {
        return NOptional.ofOptional(stream().findAny(), () -> NMsg.ofC("missing : %s", nutsBase));
    }

    @Override
    public NOptional<T> findFirst() {
        Iterator<T> it = iterator();
        if (it.hasNext()) {
            return NOptional.of(it.next());
        }
        return NOptional.ofEmpty(() ->
                nutsBase == null ?
                        NMsg.ofPlain("missing first") :
                        NMsg.ofC("missing first %s", nutsBase)
        );
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
    public boolean anyMatch(Predicate<? super T> predicate) {
        return stream().anyMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return stream().noneMatch(predicate);
    }

    @Override
    public NStream<T> limit(long maxSize) {
        return NStream.ofStream(stream().limit(maxSize));
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
    public NOptional<T> min(Comparator<? super T> comparator) {
        return NOptional.ofOptional(stream().min(comparator), () -> NMsg.ofC("missing : %s", nutsBase));
    }

    @Override
    public NOptional<T> max(Comparator<? super T> comparator) {
        return NOptional.ofOptional(stream().max(comparator), () -> NMsg.ofC("missing : %s", nutsBase));
    }

    @Override
    public NElement describe() {
        return iterator().describe();
    }

    public NStream<T> redescribe(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        return new NStreamWithDescription<>(this, description);
    }

    @Override
    public NStream<T> skip(long n) {
        return new NStreamBase<T>(nutsBase) {
            @Override
            public NIterator<T> iterator() {
                NIterator<T> iterator = NStreamBase.this.iterator();
                int count=0;
                while(count<n){
                    if(iterator.hasNext()){
                        iterator.next();
                    }else{
                        return iterator;
                    }
                    count++;
                }
                return iterator;
            }
        };
    }

    @Override
    public void close() {

    }
}
