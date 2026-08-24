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
package net.thevpc.nuts.pipeline;

import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NRedescribable;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.UnsafeFunction;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Find Result items from find command
 *
 * @param <T> Result Type
 * @author thevpc
 * @app.category Base
 * @see NSearch#getResultIds()
 * @since 0.5.4
 */
public interface NStream<T> extends Iterable<T>, NRedescribable<NStream<T>>, AutoCloseable {

    /**
     * Creates a new instance of of array.
     *
     * @param str str
     * @return of array result
     */
    static <T> NStream<T> ofArray(T... str) {
        return NUtilsRPI.of().arrayToStream(str);
    }

    /**
     * Creates a new instance of of int array.
     *
     * @param items items
     * @return of int array result
     */
    static <T> NStream<T> ofIntArray(int... items) {
      /**
       * Return.
       *
       * @param NElement.ofIntArray(items) n element.of int array(items)
       */
        return (NStream<T>) ofStream(Arrays.stream(items).boxed()).withDescription(() -> NElement.ofIntArray(items));
    }

    /**
     * Creates a new instance of of long array.
     *
     * @param items items
     * @return of long array result
     */
    static <T> NStream<T> ofLongArray(long... items) {
      /**
       * Return.
       *
       * @param NElement.ofLongArray(items) n element.of long array(items)
       */
        return (NStream<T>) ofStream(Arrays.stream(items).boxed()).withDescription(() -> NElement.ofLongArray(items));
    }

    /**
     * Creates a new instance of of boolean array.
     *
     * @param items items
     * @return of boolean array result
     */
    static <T> NStream<T> ofBooleanArray(boolean... items) {
      /**
       * Return.
       *
       * @param NElement.ofBooleanArray(items) n element.of boolean array(items)
       */
        return (NStream<T>) ofStream(IntStream.range(0, items.length).mapToObj(i -> items[i])).withDescription(() -> NElement.ofBooleanArray(items));
    }

    /**
     * Creates a new instance of of byte array.
     *
     * @param items items
     * @return of byte array result
     */
    static <T> NStream<T> ofByteArray(byte... items) {
      /**
       * Return.
       *
       * @param NElement.ofByteArray(items) n element.of byte array(items)
       */
        return (NStream<T>) ofStream(IntStream.range(0, items.length).mapToObj(i -> items[i])).withDescription(() -> NElement.ofByteArray(items));
    }


    /**
     * Creates a new instance of of char array.
     *
     * @param items items
     * @return of char array result
     */
    static <T> NStream<T> ofCharArray(char... items) {
      /**
       * Return.
       *
       * @param NElement.ofCharArray(items) n element.of char array(items)
       */
        return (NStream<T>) ofStream(IntStream.range(0, items.length).mapToObj(i -> items[i])).withDescription(() -> NElement.ofCharArray(items));
    }

    /**
     * Creates a new instance of of short array.
     *
     * @param items items
     * @return of short array result
     */
    static <T> NStream<T> ofShortArray(short... items) {
      /**
       * Return.
       *
       * @param NElement.ofShortArray(items) n element.of short array(items)
       */
        return (NStream<T>) ofStream(IntStream.range(0, items.length).mapToObj(i -> items[i])).withDescription(() -> NElement.ofShortArray(items));
    }

    /**
     * Creates a new instance of of float array.
     *
     * @param items items
     * @return of float array result
     */
    static <T> NStream<T> ofFloatArray(float... items) {
      /**
       * Return.
       *
       * @param NElement.ofFloatArray(items) n element.of float array(items)
       */
        return (NStream<T>) ofStream(IntStream.range(0, items.length).mapToObj(i -> items[i])).withDescription(() -> NElement.ofFloatArray(items));
    }

    /**
     * Creates a new instance of of double array.
     *
     * @param items items
     * @return of double array result
     */
    static <T> NStream<T> ofDoubleArray(double... items) {
      /**
       * Return.
       *
       * @param NElement.ofDoubleArray(items) n element.of double array(items)
       */
        return (NStream<T>) ofStream(Arrays.stream(items).boxed()).withDescription(() -> NElement.ofDoubleArray(items));
    }

    /**
     * Creates a new instance of of optional.
     *
     * @param str str
     * @return of optional result
     */
    static <T> NStream<T> ofOptional(NOptional<T> str) {
        return NUtilsRPI.of().optionalToStream(str);
    }

    /**
     * Creates a new instance of of optional.
     *
     * @param str str
     * @return of optional result
     */
    static <T> NStream<T> ofOptional(Optional<T> str) {
        return NUtilsRPI.of().optionalToStream(str);
    }

    /**
     * Creates a new instance of of iterable.
     *
     * @param str str
     * @return of iterable result
     */
    static <T> NStream<T> ofIterable(Iterable<T> str) {
        return NUtilsRPI.of().iterableToStream(str);
    }

    /**
     * Creates a new instance of of iterator.
     *
     * @param str str
     * @return of iterator result
     */
    static <T> NStream<T> ofIterator(Iterator<T> str) {
        return NUtilsRPI.of().iteratorToStream(str);
    }

    /**
     * Creates a new instance of of stream.
     *
     * @param str str
     * @return of stream result
     */
    static <T> NStream<T> ofStream(Stream<T> str) {
        return NUtilsRPI.of().streamToNStream(str);
    }

    /**
     * Creates a new instance of of empty.
     *
     * @return of empty result
     */
    static <T> NStream<T> ofEmpty() {
        return NUtilsRPI.of().createEmptyStream();
    }

    /**
     * Creates a new instance of of singleton.
     *
     * @param element element
     * @return of singleton result
     */
    static <T> NStream<T> ofSingleton(T element) {
        /**
         * Creates a new instance of of iterable.
         *
         * @param Arrays.asList(element) arrays.as list(element)
         * @return of iterable result
         */
        return ofIterable(Arrays.asList(element));
    }

    /**
     * return result as a java.util.List .
     * <p>
     * consumes the result and returns a list Calling this method twice will
     * result in unexpected behavior (may return an empty list as the result is
     * already consumed or throw an Exception)
     *
     * @return result as a java.util.List
     */
    List<T> toList();

    /**
     * Converts to set.
     *
     * @return to set result
     */
    Set<T> toSet();

    /**
     * Converts to sorted set.
     *
     * @return to sorted set result
     */
    Set<T> toSortedSet();

    /**
     * Converts to ordered set.
     *
     * @return to ordered set result
     */
    Set<T> toOrderedSet();

    /**
     * return the last value or null if none found. consumes all of the stream
     * <p>
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the last value or null if none found
     */
    NOptional<T> findLast();

    /**
     * return the first value while checking that there are no more elements.
     * <p>
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the first value while checking that there are no more elements to
     * consume. An IllegalArgumentException is thrown if there are no elements
     * to consume. An IllegalArgumentException is also thrown if the are more
     * than one element consumed
     */
    NOptional<T> findSingleton();

    /**
     * return result as a java.util.stream.Stream .
     * <p>
     * Calling this method twice will result in unexpected behavior (may return
     * 0 as the result is already consumed or throw an Exception)
     *
     * @return result as a java.util.stream.Stream
     */
    Stream<T> jstream();

    /**
     * Skip.
     *
     * @param n n
     * @return skip result
     */
    NStream<T> skip(long n);

    /**
     * return elements count of this result.
     * <p>
     * consumes the result and returns the number of elements consumed. Calling
     * this method twice will result in unexpected behavior (may return 0 as the
     * result is already consumed or throw an Exception)
     *
     * @return elements count of this result.
     */
    long count();

    /**
     * return NutsStream a stream consisting of the results of applying the
     * given function to the elements of this stream.
     *
     * @param <R>    to type
     * @param mapper mapper
     * @return NutsStream a stream consisting of the results of applying the
     * given function to the elements of this stream.
     */
    <R> NStream<R> map(Function<? super T, ? extends R> mapper);

    /**
     * Map unsafe.
     *
     * @param mapper mapper
     * @return map unsafe result
     */
    <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper);

    /**
     * Map unsafe.
     *
     * @param mapper mapper
     * @param onError on error
     * @return map unsafe result
     */
    <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper, Function<Exception, ? extends R> onError);

    /**
     * Instance of.
     *
     * @param type type
     * @return instance of result
     */
    <V> NStream<V> instanceOf(Class<V> type);

    /**
     * Sorted.
     *
     * @return sorted result
     */
    NStream<T> sorted();

    /**
     * Sorted.
     *
     * @param comp comp
     * @return sorted result
     */
    NStream<T> sorted(Comparator<T> comp);

    /**
     * Distinct.
     *
     * @return distinct result
     */
    NStream<T> distinct();

    /**
     * Distinct by.
     *
     * @param d d
     * @return distinct by result
     */
    <R> NStream<T> distinctBy(Function<T, R> d);

    /**
     * Non null.
     *
     * @return non null result
     */
    NStream<T> nonNull();

    /**
     * Non blank.
     *
     * @return non blank result
     */
    NStream<T> nonBlank();

    /**
     * Filter.
     *
     * @param predicate predicate
     * @return filter result
     */
    NStream<T> filter(Predicate<? super T> predicate);

    /**
     * Concat.
     *
     * @param other other
     * @return concat result
     */
    NStream<T> concat(Iterator<? extends T> other);

    /**
     * Coalesce.
     *
     * @param other other
     * @return coalesce result
     */
    NStream<T> coalesce(Iterator<? extends T> other);

    /**
     * Concat.
     *
     * @param other other
     * @return concat result
     */
    NStream<T> concat(NStream<? extends T> other);

    /**
     * Coalesce.
     *
     * @param other other
     * @return coalesce result
     */
    NStream<T> coalesce(NStream<? extends T> other);

    /**
     * Converts to boolean array.
     *
     * @return to boolean array result
     */
    boolean[] toBooleanArray();

    /**
     * Converts to byte array.
     *
     * @return to byte array result
     */
    byte[] toByteArray();

    /**
     * Converts to char array.
     *
     * @return to char array result
     */
    char[] toCharArray();

    /**
     * Converts to short array.
     *
     * @return to short array result
     */
    short[] toShortArray();

    /**
     * Converts to float array.
     *
     * @return to float array result
     */
    float[] toFloatArray();

    /**
     * Converts to int array.
     *
     * @return to int array result
     */
    int[] toIntArray();

    /**
     * Converts to long array.
     *
     * @return to long array result
     */
    long[] toLongArray();

    /**
     * Converts to double array.
     *
     * @return to double array result
     */
    double[] toDoubleArray();

    /**
     * Map to int.
     *
     * @param mapper mapper
     * @return map to int result
     */
    IntStream mapToInt(ToIntFunction<? super T> mapper);

    /**
     * Map to long.
     *
     * @param mapper mapper
     * @return map to long result
     */
    LongStream mapToLong(ToLongFunction<? super T> mapper);

    /**
     * Map to double.
     *
     * @param mapper mapper
     * @return map to double result
     */
    DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper);

    /**
     * Converts to array.
     *
     * @param generator generator
     * @return to array result
     */
    <A> A[] toArray(IntFunction<A[]> generator);

    <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper,
                           Function<? super T, ? extends U> valueMapper);

    <K, U> Map<K, U> toOrderedMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends U> valueMapper);

    <K, U> Map<K, U> toSortedMap(Function<? super T, ? extends K> keyMapper,
                                 Function<? super T, ? extends U> valueMapper);

    /**
     * Flat map iter.
     *
     * @param mapper mapper
     * @return flat map iter result
     */
    <R> NStream<R> flatMapIter(Function<? super T, ? extends Iterator<? extends R>> mapper);

    /**
     * Flat map list.
     *
     * @param mapper mapper
     * @return flat map list result
     */
    <R> NStream<R> flatMapList(Function<? super T, ? extends List<? extends R>> mapper);

    /**
     * Flat map array.
     *
     * @param mapper mapper
     * @return flat map array result
     */
    <R> NStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper);

    /**
     * Flat map.
     *
     * @param mapper mapper
     * @return flat map result
     */
    <R> NStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

    /**
     * Flat map stream.
     *
     * @param mapper mapper
     * @return flat map stream result
     */
    <R> NStream<R> flatMapStream(Function<? super T, ? extends NStream<? extends R>> mapper);

    /**
     * Group by.
     *
     * @param classifier classifier
     * @return group by result
     */
    <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier);

    /**
     * Grouped by.
     *
     * @param classifier classifier
     * @return grouped by result
     */
    <K> NStream<Map.Entry<K, List<T>>> groupedBy(Function<? super T, ? extends K> classifier);

    /**
     * Finds the find any.
     *
     * @return find any result
     */
    NOptional<T> findAny();

    /**
     * Finds the find first.
     *
     * @return find first result
     */
    NOptional<T> findFirst();

    /**
     * Flat map to double.
     *
     * @param mapper mapper
     * @return flat map to double result
     */
    DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper);

    /**
     * Flat map to int.
     *
     * @param mapper mapper
     * @return flat map to int result
     */
    IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper);

    /**
     * Flat map to long.
     *
     * @param mapper mapper
     * @return flat map to long result
     */
    LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper);

    /**
     * All match.
     *
     * @param predicate predicate
     * @return all match result
     */
    boolean allMatch(Predicate<? super T> predicate);

    /**
     * None match.
     *
     * @param predicate predicate
     * @return none match result
     */
    boolean noneMatch(Predicate<? super T> predicate);

    /**
     * Any match.
     *
     * @param predicate predicate
     * @return any match result
     */
    boolean anyMatch(Predicate<? super T> predicate);

    /**
     * Limit.
     *
     * @param maxSize max size
     * @return limit result
     */
    NStream<T> limit(long maxSize);

    /**
     * Iterator.
     *
     * @return iterator result
     */
    NIterator<T> iterator();

    <R> R collect(Supplier<R> supplier,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);

    /**
     * Collect.
     *
     * @param collector collector
     * @return collect result
     */
    <R, A> R collect(Collector<? super T, A, R> collector);

    /**
     * Min.
     *
     * @param comparator comparator
     * @return min result
     */
    NOptional<T> min(Comparator<? super T> comparator);

    /**
     * Max.
     *
     * @param comparator comparator
     * @return max result
     */
    NOptional<T> max(Comparator<? super T> comparator);

    /**
     * Close.
     */
    void close();

    /**
     * On close.
     *
     * @param closeHandler close handler
     * @return on close result
     */
    NStream<T> onClose(Runnable closeHandler);
}
