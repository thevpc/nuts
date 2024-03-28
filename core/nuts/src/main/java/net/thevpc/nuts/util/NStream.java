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
package net.thevpc.nuts.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.spi.NStreams;

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
public interface NStream<T> extends NIterable<T> {
    static <T> NStream<T> of(T[] str, Function<NSession, NElement> name, NSession session) {
        return NStreams.of(session).createStream(str, name);
    }

    static <T> NStream<T> of(Iterable<T> str, Function<NSession, NElement> name, NSession session) {
        return NStreams.of(session).createStream(str, name);
    }

    static <T> NStream<T> of(Iterator<T> str, Function<NSession, NElement> name, NSession session) {
        return NStreams.of(session).createStream(str, name);
    }

    static <T> NStream<T> of(Stream<T> str, Function<NSession, NElement> name, NSession session) {
        return NStreams.of(session).createStream(str, name);
    }

    static <T> NStream<T> of(T[] str, NSession session) {
        return NStreams.of(session).createStream(str, e-> NElements.of(e).ofString("array"));
    }

    static <T> NStream<T> of(Iterable<T> str, NSession session) {
        return NStreams.of(session).createStream(str, e-> NElements.of(e).ofString("iterable"));
    }

    static <T> NStream<T> of(Iterator<T> str, NSession session) {
        return NStreams.of(session).createStream(str, e-> NElements.of(e).ofString("iterator"));
    }

    static <T> NStream<T> of(Stream<T> str, NSession session) {
        return NStreams.of(session).createStream(str, e-> NElements.of(e).ofString("stream"));
    }

    static <T> NStream<T> of(NIterable<T> str, NSession session) {
        return NStreams.of(session).createStream(str);
    }

    static <T> NStream<T> of(NIterator<T> str, NSession session) {
        return NStreams.of(session).createStream(str);
    }

    static <T> NStream<T> ofEmpty(NSession session) {
        return NStreams.of(session).createEmptyStream();
    }

    static <T> NStream<T> ofSingleton(T element, NSession session) {
        return of(Arrays.asList(element), session);
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
    <R> NStream<R> map(NFunction<? super T, ? extends R> mapper);

    <R> NStream<R> map(Function<? super T, ? extends R> mapper, String name);

    <R> NStream<R> map(Function<? super T, ? extends R> mapper, NElement name);

    <R> NStream<R> map(Function<? super T, ? extends R> mapper, Function<NSession, NElement> name);

    <R> NStream<R> mapUnsafe(NUnsafeFunction<? super T, ? extends R> mapper, NFunction<Exception, ? extends R> onError);

    NStream<T> sorted();

    NStream<T> sorted(NComparator<T> comp);

    NStream<T> distinct();

    <R> NStream<T> distinctBy(NFunction<T, R> d);

    NStream<T> nonNull();

    NStream<T> nonBlank();

    NStream<T> filter(NPredicate<? super T> predicate);

    NStream<T> filter(Predicate<? super T> predicate, String name);

    NStream<T> filter(Predicate<? super T> predicate, NElement name);

    NStream<T> filter(Predicate<? super T> predicate, Function<NSession, NElement> info);

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

    <R> NStream<R> flatMapIter(NFunction<? super T, ? extends Iterator<? extends R>> mapper);

    <R> NStream<R> flatMapList(NFunction<? super T, ? extends List<? extends R>> mapper);

    <R> NStream<R> flatMapArray(NFunction<? super T, ? extends R[]> mapper);

    <R> NStream<R> flatMap(NFunction<? super T, ? extends Stream<? extends R>> mapper);

    <R> NStream<R> flatMapStream(NFunction<? super T, ? extends NStream<? extends R>> mapper);

    <K> Map<K, List<T>> groupBy(NFunction<? super T, ? extends K> classifier);

    <K> NStream<Map.Entry<K, List<T>>> groupedBy(NFunction<? super T, ? extends K> classifier);

    NOptional<T> findAny();

    NOptional<T> findFirst();

    DoubleStream flatMapToDouble(NFunction<? super T, ? extends DoubleStream> mapper);

    IntStream flatMapToInt(NFunction<? super T, ? extends IntStream> mapper);

    LongStream flatMapToLong(NFunction<? super T, ? extends LongStream> mapper);

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
