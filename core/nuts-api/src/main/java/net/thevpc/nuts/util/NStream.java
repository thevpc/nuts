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
package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElementDescribable;
import net.thevpc.nuts.reserved.rpi.NCollectionsRPI;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Find Result items from find command
 *
 * @param <T> Result Type
 * @author thevpc
 * @app.category Base
 * @see NSearchCmd#getResultIds()
 * @since 0.5.4
 */
public interface NStream<T> extends Iterable<T>, NElementDescribable<NStream<T>> {
    static <T> NStream<T> of(T[] str) {
        return NCollectionsRPI.of().arrayToStream(str);
    }

    static <T> NStream<T> of(Iterable<T> str) {
        return NCollectionsRPI.of().iterableToStream(str);
    }

    static <T> NStream<T> of(Iterator<T> str) {
        return NCollectionsRPI.of().iteratorToStream(str);
    }

    static <T> NStream<T> of(Stream<T> str) {
        return NCollectionsRPI.of().toStream(str);
    }

    static <T> NStream<T> ofEmpty() {
        return NCollectionsRPI.of().emptyStream();
    }

    static <T> NStream<T> ofSingleton(T element) {
        return of(Arrays.asList(element));
    }

    /**
     * return result as a  java.util.List .
     * <p>
     * consumes the result and returns a list Calling this method twice will
     * result in unexpected behavior (may return an empty list as the result is
     * already consumed or throw an Exception)
     *
     * @return result as a  java.util.List
     */
    List<T> toList();

    Set<T> toSet();

    Set<T> toSortedSet();

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
     * return result as a  java.util.stream.Stream .
     * <p>
     * Calling this method twice will result in unexpected behavior (may return
     * 0 as the result is already consumed or throw an Exception)
     *
     * @return result as a  java.util.stream.Stream
     */
    Stream<T> stream();


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
     * return NutsStream a stream consisting of the results of applying the given function to the elements of this stream.
     *
     * @param <R>    to type
     * @param mapper mapper
     * @return NutsStream a stream consisting of the results of applying the given function to the elements of this stream.
     */
    <R> NStream<R> map(Function<? super T, ? extends R> mapper);

    <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper);
    <R> NStream<R> mapUnsafe(UnsafeFunction<? super T, ? extends R> mapper, Function<Exception, ? extends R> onError);

    NStream<T> sorted();

    NStream<T> sorted(NComparator<T> comp);

    NStream<T> distinct();

    <R> NStream<T> distinctBy(Function<T, R> d);

    NStream<T> nonNull();

    NStream<T> nonBlank();

    NStream<T> filter(Predicate<? super T> predicate);

    NStream<T> filterNonNull();

    NStream<T> filterNonBlank();

    NStream<T> coalesce(NIterator<? extends T> other);

    <A> A[] toArray(IntFunction<A[]> generator);

    <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper,
                           Function<? super T, ? extends U> valueMapper);

    <K, U> Map<K, U> toOrderedMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends U> valueMapper);

    <K, U> Map<K, U> toSortedMap(Function<? super T, ? extends K> keyMapper,
                                 Function<? super T, ? extends U> valueMapper);

    <R> NStream<R> flatMapIter(Function<? super T, ? extends Iterator<? extends R>> mapper);

    <R> NStream<R> flatMapList(Function<? super T, ? extends List<? extends R>> mapper);

    <R> NStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper);

    <R> NStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

    <R> NStream<R> flatMapStream(Function<? super T, ? extends NStream<? extends R>> mapper);

    <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier);

    <K> NStream<Map.Entry<K, List<T>>> groupedBy(Function<? super T, ? extends K> classifier);

    NOptional<T> findAny();

    NOptional<T> findFirst();

    DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper);

    IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper);

    LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper);

    boolean allMatch(Predicate<? super T> predicate);

    boolean noneMatch(Predicate<? super T> predicate);

    NStream<T> limit(long maxSize);

    NIterator<T> iterator();

    <R> R collect(Supplier<R> supplier,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);

    <R, A> R collect(Collector<? super T, A, R> collector);

    NOptional<T> min(Comparator<? super T> comparator);

    NOptional<T> max(Comparator<? super T> comparator);
}
