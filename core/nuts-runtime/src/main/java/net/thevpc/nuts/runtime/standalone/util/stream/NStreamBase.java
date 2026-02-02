/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.runtime.standalone.util.CallOnceRunnable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
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
public class NStreamBase<T> implements NStream<T> {

    public static <X> NStream<X> ofEmpty(String name) {
        return new NStreamBase<>(name, () -> NIteratorBuilder.emptyIterator(), NElement.ofName("empty"), null);
    }

    public static <X> NStream<X> ofJavaStream(String name, Stream<X> o) {
        return new NStreamBase<>(name, () -> NIterator.of(o.iterator()), NDescribables.describeResolveOr(o, () -> NElement.ofName("fromJavaStream")), null);
    }

    public static <X> NStream<X> ofIterable(String name, NIterable<X> o) {
        return new NStreamBase<>(name, () -> o.iterator(), NDescribables.describeResolveOr(o, () -> NElement.ofName("fromIterable")), null);
    }

    public static <X> NStream<X> ofIterator(String name, NIterator<X> o) {
        return new NStreamBase<>(name, () -> o, NDescribables.describeResolveOr(o, () -> NElement.ofName("fromIterator")), null);
    }

    public static <X> NStream<X> ofCollection(String name, Collection<X> o) {
        return new NStreamBase<>(name, () -> NIterator.of(o.iterator()), NDescribables.describeResolveOr(o, () -> NElement.ofName("fromCollection")), null);
    }

    interface NStreamBaseTransform<A, B> {
        NIterator<B> transformIterator(NIterator<A> iterator);

        NElement transformDescription(NElement desc);
    }

    protected String name;
    protected Supplier<NIterator<T>> iteratorSupplier;
    protected NElement description;
    protected Runnable onClose;

    private static <A, B> NStream<B> transform(NStreamBase<A> a, NStreamBaseTransform<A, B> t) {
        return new NStreamBase<>(a.name,
                () -> t.transformIterator(a.iteratorSupplier.get()),
                t.transformDescription(a.description),
                a.onClose
        );
    }

    public NStreamBase(String name, Supplier<NIterator<T>> iteratorSupplier, NElement description, Runnable onClose) {
        this.name = name;
        this.iteratorSupplier = iteratorSupplier;
        this.description = description;
        this.onClose = CallOnceRunnable.of(onClose);
    }

    private <B> NStream<B> transform(NStreamBaseTransform<T, B> t) {
        return new NStreamBase<>(this.name,
                () -> t.transformIterator(this.iteratorSupplier.get()),
                t.transformDescription(this.description),
                this.onClose
        );
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("duplicate key %s", u));
        };
    }

    @Override
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        try {
            for (T a : this) {
                list.add(a);
            }
        } finally {
            close();
        }
        return list;
    }

    @Override
    public Set<T> toSet() {
        try {
            return stream().collect(Collectors.toSet());
        } finally {
            close();
        }
    }

    @Override
    public Set<T> toSortedSet() {
        try {
            return stream().collect(Collectors.toCollection(TreeSet::new));
        } finally {
            close();
        }
    }

    @Override
    public Set<T> toOrderedSet() {
        try {
            return stream().collect(Collectors.toCollection(LinkedHashSet::new));
        } finally {
            close();
        }
    }

    @Override
    public NOptional<T> findLast() {
        try {
            T t = null;
            Iterator<T> it = iterator();
            while (it.hasNext()) {
                t = it.next();
            }
            return NOptional.ofEmpty(()
                    -> name == null
                    ? NMsg.ofPlain("missing last")
                    : NMsg.ofC("missing last %s", name)
            );
        } finally {
            close();
        }
    }

    @Override
    public NOptional<T> findSingleton() {
        try {
            Iterator<T> it = iterator();
            if (it.hasNext()) {
                T t = it.next();
                if (it.hasNext()) {
                    return NOptional.ofError(
                            () -> NMsg.ofC("too many results for %s", name),
                            new NTooManyElementsException(NMsg.ofC("too many results for %s", name))
                    );
                }
                return NOptional.of(t);
            } else {
                return NOptional.ofEmpty(()
                        -> name == null
                        ? NMsg.ofPlain("missing")
                        : NMsg.ofC("missing %s", name)
                );
            }
        } finally {
            close();
        }
    }

    @Override
    public Stream<T> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false)
                .onClose(this::close);
    }

    @Override
    public long count() {
        try {
            return stream().count();
        } finally {
            close();
        }
    }

    @Override
    public <R> NStream<R> map(Function<? super T, ? extends R> mapper) {
        return transform(new NStreamBaseTransform<T, R>() {
            @Override
            public NIterator<R> transformIterator(NIterator<T> iterator) {
                return (NIterator) NIteratorBuilder.of(iterator).map(NFunction.of(mapper)).build();
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("map", mapper, desc);
            }
        });
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
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                return NIteratorUtils.sort(iterator, null, false);
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("sort", desc);
            }
        });
    }

    @Override
    public NStream<T> sorted(NComparator<T> comp) {
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                return NIteratorUtils.sort(iterator, comp, false);
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("sort", NElement.ofPair("comparator", NDescribables.describeResolveOrDestruct(comp)), desc);
            }
        });
    }

    @Override
    public NStream<T> distinct() {
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                return NIteratorUtils.distinct(iterator);
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("distinct", desc);
            }
        });
    }

    @Override
    public <R> NStream<T> distinctBy(Function<T, R> condition) {
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                return NIteratorUtils.distinct(iterator, condition);
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("distinct", NElement.ofPair("by", NDescribables.describeResolveOrDestruct(condition)), desc);
            }
        });
    }

    @Override
    public NStream<T> nonNull() {
        return filter(NPredicate.of(Objects::nonNull, NElement.ofName("nonNull")));
    }

    @Override
    public NStream<T> nonBlank() {
        return filter(NPredicate.of(x -> {
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
        }, NElement.ofName("nonBlank")));
    }

    @Override
    public NStream<T> filter(Predicate<? super T> predicate) {
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                return NIteratorBuilder.of(iterator).filter(NPredicate.of(predicate)).build();//,"mapped("+it+")"
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("filter", NElement.ofPair("accept", NDescribables.describeResolveOrDestruct(predicate)), desc);
            }
        });
    }


    @Override
    public NStream<T> coalesce(Iterator<? extends T> other) {
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                List<NIterator<? extends T>> iterators = Arrays.asList(iterator, NIterator.of(other));
                return NIteratorUtils.coalesce(iterators);//,"mapped("+it+")"
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("coalesce", desc, NDescribables.describeResolveOrDestruct(other));
            }
        });
    }

    @Override
    public NIterator<T> iterator() {
        return this.iteratorSupplier.get()
                .withDescription(this::describe)
                .onClose(this::close)
                ;
    }

    @Override
    public NStream<T> concat(Iterator<? extends T> other) {
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                List<NIterator<? extends T>> iterators = Arrays.asList(iterator, NIterator.of(other));
                return NIteratorUtils.concat(iterators);//,"mapped("+it+")"
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("concat", desc, NDescribables.describeResolveOrDestruct(other));
            }
        });
    }

    @Override
    public NStream<T> coalesce(NStream<? extends T> other) {
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                List<NIterator<? extends T>> iterators = Arrays.asList(iterator, other.iterator());
                return NIteratorUtils.coalesce(iterators);//,"mapped("+it+")"
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("coalesce", desc, NDescribables.describeResolveOrDestruct(other));
            }
        });
    }

    @Override
    public NStream<T> concat(NStream<? extends T> other) {
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                List<NIterator<? extends T>> iterators = Arrays.asList(iterator, other.iterator());
                return NIteratorUtils.concat(iterators);//,"mapped("+it+")"
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("concat", desc, NDescribables.describeResolveOrDestruct(other));
            }
        });
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
        return mapToInt(x -> ((Number) x).intValue()).toArray();
    }

    @Override
    public long[] toLongArray() {
        return mapToLong(x -> ((Number) x).longValue()).toArray();
    }

    @Override
    public double[] toDoubleArray() {
        return mapToDouble(x -> ((Number) x).doubleValue()).toArray();
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream().mapToDouble(mapper);
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        try {
            return stream().toArray(generator);
        } finally {
            close();
        }
    }

    @Override
    public <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends U> valueMapper) {
        try {
            return stream().collect(Collectors.toMap(keyMapper, valueMapper));
        } finally {
            close();
        }
    }

    @Override
    public <K, U> Map<K, U> toOrderedMap(Function<? super T, ? extends K> keyMapper,
                                         Function<? super T, ? extends U> valueMapper) {
        try {
            return stream().collect(Collectors.toMap(keyMapper, valueMapper, throwingMerger(), LinkedHashMap::new));
        } finally {
            close();
        }
    }

    @Override
    public <K, U> Map<K, U> toSortedMap(Function<? super T, ? extends K> keyMapper,
                                        Function<? super T, ? extends U> valueMapper) {
        try {
            return stream().collect(Collectors.toMap(keyMapper, valueMapper, throwingMerger(), TreeMap::new));
        } finally {
            close();
        }
    }

    @Override
    public <R> NStream<R> flatMapIter(Function<? super T, ? extends Iterator<? extends R>> mapper) {
        return transform(new NStreamBaseTransform<T, R>() {
            @Override
            public NIterator<R> transformIterator(NIterator<T> iterator) {
                return NIteratorBuilder.of(iterator).flatMap(mapper).build();
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("flatMapIter", NElement.ofPair("by", NDescribables.describeResolveOrDestruct(mapper)), desc);
            }
        });
    }

    @Override
    public <R> NStream<R> flatMapList(Function<? super T, ? extends List<? extends R>> mapper) {
        return transform(new NStreamBaseTransform<T, R>() {
            @Override
            public NIterator<R> transformIterator(NIterator<T> iterator) {
                NIteratorBuilder<T> r = NIteratorBuilder.of(iterator);
                return (NIterator<R>) r.flatMap(
                        NFunction.of(tt -> mapper.apply((T) tt).iterator()).withDescription(() -> NFunction.of(mapper).describe())
                ).build();
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("flatMapList", NElement.ofPair("by", NDescribables.describeResolveOrDestruct(mapper)), desc);
            }
        });
    }

    @Override
    public <R> NStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper) {
        return transform(new NStreamBaseTransform<T, R>() {
            @Override
            public NIterator<R> transformIterator(NIterator<T> iterator) {
                return NIteratorBuilder.of(iterator)
                        .flatMap(
                                NFunction.of(t -> Arrays.asList(mapper.apply((T) t)).iterator())
                                        .withDescription(() -> NFunction.of(mapper).describe())
                        ).build();
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("flatMapArray", NElement.ofPair("by", NDescribables.describeResolveOrDestruct(mapper)), desc);
            }
        });
    }

    @Override
    public <R> NStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return transform(new NStreamBaseTransform<T, R>() {
            @Override
            public NIterator<R> transformIterator(NIterator<T> iterator) {
                return (NIterator<R>) NIteratorBuilder.of(iterator).flatMap(
                        NFunction.of(t -> mapper.apply(t).iterator())
                ).build().withDescription(() -> NFunction.of(mapper).describe());
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("flatMap", NElement.ofPair("by", NDescribables.describeResolveOrDestruct(mapper)), desc);
            }
        });
    }

    @Override
    public <R> NStream<R> flatMapStream(Function<? super T, ? extends NStream<? extends R>> mapper) {
        return transform(new NStreamBaseTransform<T, R>() {
            @Override
            public NIterator<R> transformIterator(NIterator<T> iterator) {
                return (NIterator<R>) NIteratorBuilder.of(iterator)
                        .flatMap(
                                NFunction.of(t -> mapper.apply(t).iterator())
                        ).build().withDescription(() -> NFunction.of(mapper).describe());
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NDescribables.describeWithTransform("flatMapStream", NElement.ofPair("by", NDescribables.describeResolveOrDestruct(mapper)), desc);
            }
        });
    }

    @Override
    public <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
        try {
            Stream<T> it = NStreamBase.this.stream();
            return it.collect(Collectors.groupingBy(classifier));
        } finally {
            close();
        }
    }

    @Override
    public <K> NStream<Map.Entry<K, List<T>>> groupedBy(Function<? super T, ? extends K> classifier) {
        NObjectElement description = NElement.ofObjectBuilder()
                .name("GroupBy")
                .set("by", NFunction.of(classifier).describe())
                .set("base", iterator().describe())
                .build();
        return new NStreamBase<>(
                name
                , () -> {
            Set<Map.Entry<K, List<T>>> entries = (Set) collect(Collectors.groupingBy(classifier)).entrySet();
            return NIterator.of(entries.iterator());
        }
                , description
                // onclose already consumed
                , null
        );
    }

    @Override
    public NOptional<T> findAny() {
        try {
            return NOptional.ofOptional(stream().findAny(), () -> NMsg.ofC("missing : %s", name));
        } finally {
            close();
        }
    }

    @Override
    public NOptional<T> findFirst() {
        try {
            Iterator<T> it = iterator();
            if (it.hasNext()) {
                return NOptional.of(it.next());
            }
            return NOptional.ofEmpty(()
                    -> name == null
                    ? NMsg.ofPlain("missing first")
                    : NMsg.ofC("missing first %s", name)
            );
        } finally {
            close();
        }
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
        try {
            return stream().allMatch(predicate);
        } finally {
            close();
        }
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        try {
            return stream().anyMatch(predicate);
        } finally {
            close();
        }
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        try {
            return stream().noneMatch(predicate);
        } finally {
            close();
        }
    }

    @Override
    public NStream<T> limit(long maxSize) {
        return NStream.ofStream(stream().limit(maxSize));
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        try {
            return stream().collect(supplier, accumulator, combiner);
        } finally {
            close();
        }
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        try {
            return stream().collect(collector);
        } finally {
            close();
        }
    }

    @Override
    public NOptional<T> min(Comparator<? super T> comparator) {
        try {
            return NOptional.ofOptional(stream().min(comparator), () -> NMsg.ofC("missing : %s", name));
        } finally {
            close();
        }
    }

    @Override
    public NOptional<T> max(Comparator<? super T> comparator) {
        try {
            return NOptional.ofOptional(stream().max(comparator), () -> NMsg.ofC("missing : %s", name));
        } finally {
            close();
        }
    }

    @Override
    public NElement describe() {
        if(description!=null) {
            return description;
        }
        return iterator().describe();
    }

    public NStream<T> withDescription(Supplier<NElement> description) {
        if (description == null) {
            return this;
        }
        NElement d = description.get();
        if (d == null) {
            return this;
        }
        return new NStreamBase<T>(name, iteratorSupplier, d, onClose);
    }

    @Override
    public NStream<T> skip(long n) {
        return transform(new NStreamBaseTransform<T, T>() {
            @Override
            public NIterator<T> transformIterator(NIterator<T> iterator) {
                int count = 0;
                while (count < n) {
                    if (iterator.hasNext()) {
                        iterator.next();
                    } else {
                        return iterator;
                    }
                    count++;
                }
                return iterator;
            }

            @Override
            public NElement transformDescription(NElement desc) {
                return NElement.ofNamedUplet("skip", NUtils.firstNonNull(desc, NElement.ofString("?")), NElement.ofLong(n));
            }
        });
    }

    @Override
    public <V> NStream<V> instanceOf(Class<V> type) {
        NAssert.requireNamedNonNull(type, "type");
        return map(a -> {
            if (type.isInstance(a)) {
                return (V) a;
            }
            return null;
        }).nonNull();
    }

    @Override
    public NStream<T> onClose(Runnable closeHandler) {
        if (closeHandler == null) {
            return this;
        }
        return new NStreamBase<>(name, iteratorSupplier, description, CallOnceRunnable.ofMany(this.onClose, closeHandler));
    }

    @Override
    public void close() {
        if (onClose != null) {
            onClose.run();
        }
    }
}
