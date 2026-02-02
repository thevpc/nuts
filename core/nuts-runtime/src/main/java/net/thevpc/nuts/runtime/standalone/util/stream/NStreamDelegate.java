//package net.thevpc.nuts.runtime.standalone.util.stream;
//
//import net.thevpc.nuts.elem.NElement;
//import net.thevpc.nuts.runtime.standalone.util.CallOnceRunnable;
//import net.thevpc.nuts.util.*;
//
//import java.util.*;
//import java.util.function.*;
//import java.util.stream.*;
//
//public class NStreamDelegate<T> implements NStream<T> {
//
//    private Supplier<NElement> description;
//    protected Runnable closeRunnable;
//    protected Supplier<NStream<T>> baseStreamSupplier;
//
//    public NStreamDelegate(Supplier<NElement> description, Runnable runnable, Supplier<NStream<T>> baseStreamSupplier) {
//        this.description = description;
//        this.closeRunnable = CallOnceRunnable.of(runnable);
//        this.baseStreamSupplier = baseStreamSupplier;
//    }
//
////    public abstract NStream<T> baseStream();
//
//    @Override
//    public List<T> toList() {
//        return baseStreamSupplier.get().toList();
//    }
//
//    @Override
//    public Set<T> toSet() {
//        return baseStreamSupplier.get().toSet();
//    }
//
//    @Override
//    public Set<T> toSortedSet() {
//        return baseStreamSupplier.get().toSortedSet();
//    }
//
//    @Override
//    public Set<T> toOrderedSet() {
//        return baseStreamSupplier.get().toOrderedSet();
//    }
//
//    @Override
//    public NOptional<T> findLast() {
//        return baseStreamSupplier.get().findLast();
//    }
//
//    @Override
//    public NOptional<T> findSingleton() {
//        return baseStreamSupplier.get().findSingleton();
//    }
//
//    @Override
//    public Stream<T> stream() {
//        return baseStreamSupplier.get().stream();
//    }
//
//    @Override
//    public long count() {
//        return baseStreamSupplier.get().count();
//    }
//
//    @Override
//    public <R> NStream<R> map(Function<? super T, ? extends R> mapper) {
//        return baseStreamSupplier.get().map(mapper);
//    }
//
//    @Override
//    public NStream<T> skip(long n) {
//        return baseStreamSupplier.get().skip(n);
//    }
//
//    @Override
//    public <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper, Function<Exception, ? extends R> onError) {
//        return baseStreamSupplier.get().mapUnsafe(mapper, onError);
//    }
//
//    @Override
//    public NStream<T> sorted() {
//        return baseStreamSupplier.get().sorted();
//    }
//
//    @Override
//    public NStream<T> sorted(NComparator<T> comp) {
//        return baseStreamSupplier.get().sorted();
//    }
//
//    @Override
//    public NStream<T> distinct() {
//        return baseStreamSupplier.get().distinct();
//    }
//
//    @Override
//    public <R> NStream<T> distinctBy(Function<T, R> d) {
//        return baseStreamSupplier.get().distinctBy(d);
//    }
//
//    @Override
//    public NStream<T> nonNull() {
//        return baseStreamSupplier.get().nonNull();
//    }
//
//    @Override
//    public NStream<T> nonBlank() {
//        return baseStreamSupplier.get().nonBlank();
//    }
//
//    @Override
//    public NStream<T> filter(Predicate<? super T> predicate) {
//        return baseStreamSupplier.get().filter(predicate);
//    }
//
//
//    @Override
//    public NStream<T> coalesce(NIterator<? extends T> other) {
//        return baseStreamSupplier.get().coalesce(other);
//    }
//
//    @Override
//    public <A> A[] toArray(IntFunction<A[]> generator) {
//        return baseStreamSupplier.get().toArray(generator);
//    }
//
//    @Override
//    public <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
//        return baseStreamSupplier.get().toMap(keyMapper, valueMapper);
//    }
//
//    @Override
//    public <K, U> Map<K, U> toOrderedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
//        return baseStreamSupplier.get().toOrderedMap(keyMapper, valueMapper);
//    }
//
//    @Override
//    public <K, U> Map<K, U> toSortedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
//        return baseStreamSupplier.get().toSortedMap(keyMapper, valueMapper);
//    }
//
//    @Override
//    public <R> NStream<R> flatMapIter(Function<? super T, ? extends Iterator<? extends R>> mapper) {
//        return baseStreamSupplier.get().flatMapIter(mapper);
//    }
//
//    @Override
//    public <R> NStream<R> flatMapList(Function<? super T, ? extends List<? extends R>> mapper) {
//        return baseStreamSupplier.get().flatMapList(mapper);
//    }
//
//    @Override
//    public <R> NStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper) {
//        return baseStreamSupplier.get().flatMapArray(mapper);
//    }
//
//    @Override
//    public <R> NStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
//        return baseStreamSupplier.get().flatMap(mapper);
//    }
//
//    @Override
//    public <R> NStream<R> flatMapStream(Function<? super T, ? extends NStream<? extends R>> mapper) {
//        return baseStreamSupplier.get().flatMapStream(mapper);
//    }
//
//    @Override
//    public <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
//        return baseStreamSupplier.get().groupBy(classifier);
//    }
//
//    @Override
//    public <K> NStream<Map.Entry<K, List<T>>> groupedBy(Function<? super T, ? extends K> classifier) {
//        return baseStreamSupplier.get().groupedBy(classifier);
//    }
//
//    @Override
//    public NOptional<T> findAny() {
//        return baseStreamSupplier.get().findAny();
//    }
//
//    @Override
//    public NOptional<T> findFirst() {
//        return baseStreamSupplier.get().findFirst();
//    }
//
//    @Override
//    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
//        return baseStreamSupplier.get().flatMapToDouble(mapper);
//    }
//
//    @Override
//    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
//        return baseStreamSupplier.get().flatMapToInt(mapper);
//    }
//
//    @Override
//    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
//        return baseStreamSupplier.get().flatMapToLong(mapper);
//    }
//
//    @Override
//    public boolean allMatch(Predicate<? super T> predicate) {
//        return baseStreamSupplier.get().allMatch(predicate);
//    }
//
//    @Override
//    public boolean noneMatch(Predicate<? super T> predicate) {
//        return baseStreamSupplier.get().noneMatch(predicate);
//    }
//
//    @Override
//    public NStream<T> limit(long maxSize) {
//        return baseStreamSupplier.get().limit(maxSize);
//    }
//
//    @Override
//    public NIterator<T> iterator() {
//        return baseStreamSupplier.get().iterator().withDescription(description).onClose(() -> NStreamDelegate.this.close());
//    }
//
//    @Override
//    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
//        return baseStreamSupplier.get().collect(supplier, accumulator, combiner);
//    }
//
//    @Override
//    public <R, A> R collect(Collector<? super T, A, R> collector) {
//        return baseStreamSupplier.get().collect(collector);
//    }
//
//    @Override
//    public NOptional<T> min(Comparator<? super T> comparator) {
//        return baseStreamSupplier.get().min(comparator);
//    }
//
//    @Override
//    public NOptional<T> max(Comparator<? super T> comparator) {
//        return baseStreamSupplier.get().max(comparator);
//    }
//
//    @Override
//    public NElement describe() {
//        if (description != null) {
//            NElement s = description.get();
//            if (s != null) {
//                return s;
//            }
//        }
//        return baseStreamSupplier.get().describe();
//    }
//
//    @Override
//    public NStream<T> withDescription(Supplier<NElement> description) {
//        this.description = description;
//        return this;
//    }
//
//    @Override
//    public void forEach(Consumer<? super T> action) {
//        baseStreamSupplier.get().forEach(action);
//    }
//
//    @Override
//    public Spliterator<T> spliterator() {
//        return baseStreamSupplier.get().spliterator();
//    }
//
//    @Override
//    public <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper) {
//        return baseStreamSupplier.get().mapUnsafe(mapper);
//    }
//
//    @Override
//    public NStream<T> concat(NIterator<? extends T> other) {
//        return baseStreamSupplier.get().concat(other);
//    }
//
//    @Override
//    public NStream<T> concat(NStream<? extends T> other) {
//        return baseStreamSupplier.get().concat(other);
//    }
//
//    @Override
//    public NStream<T> coalesce(NStream<? extends T> other) {
//        return baseStreamSupplier.get().coalesce(other);
//    }
//
//    @Override
//    public void close() {
//        baseStreamSupplier.get().close();
//    }
//
//    @Override
//    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
//        return baseStreamSupplier.get().mapToDouble(mapper);
//    }
//
//    @Override
//    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
//        return baseStreamSupplier.get().mapToLong(mapper);
//    }
//
//    @Override
//    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
//        return baseStreamSupplier.get().mapToInt(mapper);
//    }
//
//    @Override
//    public double[] toDoubleArray() {
//        return baseStreamSupplier.get().toDoubleArray();
//    }
//
//    @Override
//    public long[] toLongArray() {
//        return baseStreamSupplier.get().toLongArray();
//    }
//
//    @Override
//    public int[] toIntArray() {
//        return baseStreamSupplier.get().toIntArray();
//    }
//
//    @Override
//    public float[] toFloatArray() {
//        return baseStreamSupplier.get().toFloatArray();
//    }
//
//    @Override
//    public short[] toShortArray() {
//        return baseStreamSupplier.get().toShortArray();
//    }
//
//    @Override
//    public char[] toCharArray() {
//        return baseStreamSupplier.get().toCharArray();
//    }
//
//    @Override
//    public byte[] toByteArray() {
//        return baseStreamSupplier.get().toByteArray();
//    }
//
//    @Override
//    public boolean[] toBooleanArray() {
//        return baseStreamSupplier.get().toBooleanArray();
//    }
//
//    @Override
//    public boolean anyMatch(Predicate<? super T> predicate) {
//        return baseStreamSupplier.get().anyMatch(predicate);
//    }
//
//    @Override
//    public <V> NStream<V> instanceOf(Class<V> type) {
//        return baseStreamSupplier.get().instanceOf(type);
//    }
//
//    @Override
//    public NStream<T> onClose(Runnable closeHandler) {
//        if (closeHandler == null) {
//            return this;
//        }
//        if (this.closeRunnable == null) {
//            return new NStreamDelegate<T>(
//                    description, closeHandler, baseStreamSupplier
//            );
//        }
//        return new NStreamDelegate<T>(
//                description,
//                () -> {
//                    this.closeRunnable.run();
//                    closeHandler.run();
//                }, baseStreamSupplier);
//    }
//}
