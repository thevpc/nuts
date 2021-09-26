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
package net.thevpc.nuts;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Find Result items from find command
 *
 * @param <T> Result Type
 * @author thevpc
 * @app.category Base
 * @see NutsSearchCommand#getResultIds()
 * @since 0.5.4
 */
public interface NutsStream<T> extends Iterable<T> {

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
     * return the first value or null if none found.
     * <p>
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the first value or null if none found
     */
    T first();

    /**
     * return the last value or null if none found. consumes all of the stream
     * <p>
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the last value or null if none found
     */
    T last();

    /**
     * return the first value or NutsNotFoundException if not found.
     * <p>
     * Calling this method twice will result in unexpected behavior (may return
     * an incorrect value such as null as the result is already consumed or
     * throw an Exception)
     *
     * @return the first value or NutsNotFoundException if not found
     */
    T required() throws NutsNotFoundException;

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
    T singleton() throws NutsTooManyElementsException, NutsNotFoundException;

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
     * @param mapper mapper
     * @param <R>    to type
     * @return NutsStream a stream consisting of the results of applying the given function to the elements of this stream.
     */
    <R> NutsStream<R> map(Function<? super T, ? extends R> mapper);

    NutsStream<T> sorted();

    NutsStream<T> sorted(Comparator<T> comp);

    NutsStream<T> distinct();

    <R> NutsStream<T> distinctBy(Function<T, R> d);

    NutsStream<T> nonNull();

    NutsStream<T> nonBlank();

    NutsStream<T> filter(Predicate<? super T> predicate);

    NutsStream<T> coalesce(Iterator<? extends T> other);

    T[] toArray(IntFunction<T[]> generator);

    <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper,
                           Function<? super T, ? extends U> valueMapper);

    <K, U> Map<K, U> toOrderedMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends U> valueMapper);

    <K, U> Map<K, U> toSortedMap(Function<? super T, ? extends K> keyMapper,
                                 Function<? super T, ? extends U> valueMapper);

    <R> NutsStream<R> flatMapIter(Function<? super T, ? extends Iterator<? extends R>> mapper);

    <R> NutsStream<R> flatMapStream(Function<? super T, ? extends Stream<? extends R>> mapper);

    <K> Map<K, List<T>> groupBy(Function<? super T, ? extends K> classifier);

    <K> NutsStream<Map.Entry<K, List<T>>> groupedBy(Function<? super T, ? extends K> classifier);
}
