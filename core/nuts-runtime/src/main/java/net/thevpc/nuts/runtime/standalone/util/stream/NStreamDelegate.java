package net.thevpc.nuts.runtime.standalone.util.stream;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public abstract class NStreamDelegate<T> implements NStream<T> {
    public abstract NStream<T> baseStream();

    @Override
    public List<T> toList() {
        return baseStream().toList();
    }

    @Override
    public Set<T> toSet() {
        return baseStream().toSet();
    }

    @Override
    public Set<T> toSortedSet() {
        return baseStream().toSortedSet();
    }

    @Override
    public Set<T> toOrderedSet() {
        return baseStream().toOrderedSet();
    }

    @Override
    public NOptional<T> findLast() {
        return baseStream().findLast();
    }

    @Override
    public NOptional<T> findSingleton() {
        return baseStream().findSingleton();
    }

    @Override
    public Stream<T> stream() {
        return baseStream().stream();
    }

    @Override
    public long count() {
        return baseStream().count();
    }

    @Override
    public <R> NStream<R> map(Function<? super T, ? extends R> mapper) {
        return baseStream().map(mapper);
    }


    @Override
    public NStream<T> skip(long n) {
        return baseStream().skip(n);
    }

    @Override
    public <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper, Function<Exception, ? extends R> onError) {
        return baseStream().mapUnsafe(mapper, onError);
    }

    @Override
    public NStream<T> sorted() {
        return baseStream().sorted();
    }

    @Override
    public NStream<T> sorted(NComparator<T> comp) {
        return baseStream().sorted();
    }

    @Override
    public NStream<T> distinct() {
        return baseStream().distinct();
    }

    @Override
    public <R> NStream<T> distinctBy(Function<T, R> d) {
        return baseStream().distinctBy(d);
    }

    @Override
    public NStream<T> nonNull() {
        return baseStream().nonNull();
    }

    @Override
    public NStream<T> nonBlank() {
        return baseStream().nonBlank();
    }

    @Override
    public NStream<T> filter(Predicate<? super T> predicate) {
        return baseStream().filter(predicate);
    }

    @Override
    public NStream<T> filterNonNull() {
        return baseStream().filterNonNull();
    }

    @Override
    public NStream<T> filterNonBlank() {
        return baseStream().filterNonBlank();
    }

    @Override
    public NStream<T> coalesce(NIterator<? extends T> other) {
        return baseStream().coalesce(other);
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return baseStream().toArray(generator);
    }

    @Override
    public <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return baseStream().toMap(keyMapper, valueMapper);
    }

    @Override
    public <K, U> Map<K, U> toOrderedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return baseStream().toOrderedMap(keyMapper, valueMapper);
    }

    @Override
    public <K, U> Map<K, U> toSortedMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return baseStream().toSortedMap(keyMapper, valueMapper);
    }

    @Override
    public <R> NStream<R> flatMapIter(Function<? super T, ? extends Iterator<? extends R>> mapper) {
        return baseStream().flatMapIter(mapper);
    }

    @Override
    public <R> NStream<R> flatMapList(Function<? super T, ? extends List<? extends R>> mapper) {
        return baseStream().flatMapList(mapper);
    }

    @Override
    public <R> NStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper) {
        return baseStream().flatMapArray(mapper);
    }

    @Override
    public <R> NStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return baseStream().flatMap(mapper);
    }

    @Override
    public <R> NStream<R> flatMapStream(Function<? super T, ? extends NStream<? extends R>> mapper) {
        return baseStream().flatMapStream(mapper);
    }

    @Override
    public <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier) {
        return baseStream().groupBy(classifier);
    }

    @Override
    public <K> NStream<Map.Entry<K, List<T>>> groupedBy(Function<? super T, ? extends K> classifier) {
        return baseStream().groupedBy(classifier);
    }

    @Override
    public NOptional<T> findAny() {
        return baseStream().findAny();
    }

    @Override
    public NOptional<T> findFirst() {
        return baseStream().findFirst();
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return baseStream().flatMapToDouble(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return baseStream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return baseStream().flatMapToLong(mapper);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return baseStream().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return baseStream().noneMatch(predicate);
    }

    @Override
    public NStream<T> limit(long maxSize) {
        return baseStream().limit(maxSize);
    }

    @Override
    public NIterator<T> iterator() {
        return baseStream().iterator();
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return baseStream().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return baseStream().collect(collector);
    }

    @Override
    public NOptional<T> min(Comparator<? super T> comparator) {
        return baseStream().min(comparator);
    }

    @Override
    public NOptional<T> max(Comparator<? super T> comparator) {
        return baseStream().max(comparator);
    }

    @Override
    public NElement describe() {
        return baseStream().describe();
    }

    @Override
    public NStream<T> redescribe(Supplier<NElement> description) {
        NStream<T> ts = baseStream();
        return ts.redescribe(description);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        baseStream().forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return baseStream().spliterator();
    }

    @Override
    public <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper) {
        return baseStream().mapUnsafe(mapper);
    }

    @Override
    public NStream<T> concat(NIterator<? extends T> other) {
        return baseStream().concat(other);
    }

    @Override
    public NStream<T> concat(NStream<? extends T> other) {
        return baseStream().concat(other);
    }

    @Override
    public NStream<T> coalesce(NStream<? extends T> other) {
        return baseStream().coalesce(other);
    }

    @Override
    public void close() {
        baseStream().close();
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return baseStream().mapToDouble(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return baseStream().mapToLong(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return baseStream().mapToInt(mapper);
    }

    @Override
    public double[] toDoubleArray() {
        return baseStream().toDoubleArray();
    }

    @Override
    public long[] toLongArray() {
        return baseStream().toLongArray();
    }

    @Override
    public int[] toIntArray() {
        return baseStream().toIntArray();
    }

    @Override
    public float[] toFloatArray() {
        return baseStream().toFloatArray();
    }

    @Override
    public short[] toShortArray() {
        return baseStream().toShortArray();
    }

    @Override
    public char[] toCharArray() {
        return baseStream().toCharArray();
    }

    @Override
    public byte[] toByteArray() {
        return baseStream().toByteArray();
    }

    @Override
    public boolean[] toBooleanArray() {
        return baseStream().toBooleanArray();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return baseStream().anyMatch(predicate);
    }
}
