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
package net.thevpc.nuts.pipeline;

import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NIntPair;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Describable Iterator
 *
 * @param <T> T
 */
public interface NIterator<T> extends Iterator<T>, NRedescribable<NIterator<T>>, AutoCloseable {
    /**
     * Creates a new instance of of.
     *
     * @param o o
     * @return of result
     */
    static <T> NIterator<T> of(Iterator<T> o) {
        return NUtilsRPI.of().iteratorToNIterator(o);
    }

    /**
     * Creates a new instance of of int2.
     *
     * @param a a
     * @param b b
     * @return of int2 result
     */
    static NIterator<NIntPair> ofInt2(int a, int b) {
        return NUtilsRPI.of().int2Iterator(a, b);
    }

    /**
     * Creates a new instance of of int2.
     *
     * @return of int2 result
     */
    static NIterator<NIntPair> ofInt2() {
        return NUtilsRPI.of().int2Iterator(0, 0);
    }

    /**
     * Creates a new instance of of empty.
     *
     * @return of empty result
     */
    static <T> NIterator<T> ofEmpty() {
        return NUtilsRPI.of().createEmptyIterator();
    }

    /**
     * Creates a new instance of of singleton.
     *
     * @param element element
     * @return of singleton result
     */
    static <T> NIterator<T> ofSingleton(T element) {
        return NUtilsRPI.of().iteratorToNIterator(Collections.singletonList(element).iterator());
    }

    /**
     * Creates a new instance of of with description.
     *
     * @param base base
     * @param description description
     * @param onClose on close
     * @return of with description result
     */
    static <T> NIterator<T> ofWithDescription(NIterator<T> base, Supplier<NElement> description, Runnable onClose) {
        return NUtilsRPI.of().iteratorWithDescription(base, description, onClose);
    }

    /**
     * Creates a new instance of of auto closable.
     *
     * @param t t
     * @param close close
     * @return of auto closable result
     */
    static <T> NIterator<T> ofAutoClosable(NIterator<T> t, NRunnable close) {
        return NUtilsRPI.of().createIteratorAutoClosable(t, close);
    }

    /**
     * Creates a new instance of of safe.
     *
     * @param type type
     * @param t t
     * @return of safe result
     */
    static <T> NIterator<T> ofSafe(NIteratorErrorHandlerType type, NIterator<T> t) {
        return NUtilsRPI.of().createIteratorSafe(type, t);
    }

    /**
     * Creates a new instance of of safe ignore.
     *
     * @param t t
     * @return of safe ignore result
     */
    static <T> NIterator<T> ofSafeIgnore(NIterator<T> t) {
        return NUtilsRPI.of().createIteratorSafeIgnore(t);
    }

    /**
     * Creates a new instance of of safe postpone.
     *
     * @param t t
     * @return of safe postpone result
     */
    static <T> NIterator<T> ofSafePostpone(NIterator<T> t) {
        return NUtilsRPI.of().createIteratorSafePostpone(t);
    }

    /**
     * Checks if is null or empty.
     *
     * @param t t
     * @return is null or empty result
     */
    static <T> boolean isNullOrEmpty(Iterator<T> t) {
        return NUtilsRPI.of().iteratorIsNullOrEmpty(t);
    }

    /**
     * Creates a new instance of of non null.
     *
     * @param t t
     * @return of non null result
     */
    static <T> NIterator<T> ofNonNull(NIterator<T> t) {
        return NUtilsRPI.of().iteratorNonNull(t);
    }

    /**
     * Creates a new instance of of concat.
     *
     * @param all all
     * @return of concat result
     */
    static <T> NIterator<T> ofConcat(List<NIterator<? extends T>> all) {
        return NUtilsRPI.of().iteratorConcat(all);
    }

    /**
     * Creates a new instance of of coalesce2.
     *
     * @param all all
     * @return of coalesce2 result
     */
    static <T> NIterator<T> ofCoalesce2(List<NIterator<T>> all) {
        return NUtilsRPI.of().iteratorCoalesce2(all);
    }

    /**
     * Creates a new instance of of coalesce.
     *
     * @param all all
     * @return of coalesce result
     */
    static <T> NIterator<T> ofCoalesce(NIterator<? extends T>... all) {
        return NUtilsRPI.of().iteratorCoalesce(all);
    }

    /**
     * Creates a new instance of of concat.
     *
     * @param all all
     * @return of concat result
     */
    static <T> NIterator<T> ofConcat(NIterator<? extends T>... all) {
        return NUtilsRPI.of().iteratorConcat(all);
    }

    /**
     * Creates a new instance of of concat lists.
     *
     * @param all all
     * @return of concat lists result
     */
    static <T> NIterator<T> ofConcatLists(List<NIterator<? extends T>>... all) {
        return NUtilsRPI.of().iteratorConcatLists(all);
    }

    /**
     * Creates a new instance of of coalesce.
     *
     * @param all all
     * @return of coalesce result
     */
    static <T> NIterator<T> ofCoalesce(List<NIterator<? extends T>> all) {
        return NUtilsRPI.of().iteratorCoalesce(all);
    }


    /**
     * Converts to list.
     *
     * @param it it
     * @return to list result
     */
    static <T> List<T> toList(Iterator<T> it) {
        return NUtilsRPI.of().iteratorToList(it);
    }

    /**
     * Converts to set.
     *
     * @param it it
     * @return to set result
     */
    static <T> Set<T> toSet(NIterator<T> it) {
        return NUtilsRPI.of().iteratorToSet(it);
    }

    /**
     * Converts to tree set.
     *
     * @param it it
     * @param c c
     * @return to tree set result
     */
    static <T> Set<T> toTreeSet(NIterator<T> it, NComparator<T> c) {
        return NUtilsRPI.of().iteratorToTreeSet(it, c);
    }

    /**
     * Creates a new instance of of sorted.
     *
     * @param it it
     * @param c c
     * @param removeDuplicates remove duplicates
     * @return of sorted result
     */
    static <T> NIterator<T> ofSorted(NIterator<T> it, NComparator<T> c, boolean removeDuplicates) {
        return NUtilsRPI.of().iteratorSort(it, c, removeDuplicates);
    }

    /**
     * Creates a new instance of of distinct.
     *
     * @param it it
     * @return of distinct result
     */
    static <T> NIterator<T> ofDistinct(NIterator<T> it) {
        return NUtilsRPI.of().iteratorDistinct(it);
    }

    /**
     * Creates a new instance of of distinct.
     *
     * @param it it
     * @param converter converter
     * @return of distinct result
     */
    static <F, T> NIterator<F> ofDistinct(NIterator<F> it, final Function<F, T> converter) {
        return NUtilsRPI.of().iteratorDistinct(it, converter);
    }

    /**
     * Creates a new instance of of collector.
     *
     * @param it it
     * @param consumer consumer
     * @return of collector result
     */
    static <T> NIterator<T> ofCollector(Iterator<T> it, Consumer<T> consumer) {
        return NUtilsRPI.of().iteratorCollector(it, consumer);
    }

    /**
     * Creates a new instance of of nullify if empty.
     *
     * @param other other
     * @return of nullify if empty result
     */
    static <T> NIterator<T> ofNullifyIfEmpty(NIterator<T> other) {
        return NUtilsRPI.of().iteratorNullifyIfEmpty(other);
    }

    /**
     * Creates a new instance of of convert non null.
     *
     * @param from from
     * @param converter converter
     * @param name name
     * @return of convert non null result
     */
    static <F, T> NIterator<T> ofConvertNonNull(NIterator<F> from, Function<F, T> converter, String name) {
        return NUtilsRPI.of().iteratorConvertNonNull(from, converter, name);
    }

    /**
     * Converts to list.
     *
     * @return to list result
     */
    default List<T> toList() {
        List<T> list = new ArrayList<>();
        while (hasNext()) {
            list.add(next());
        }
        return list;
    }

    /**
     * On close.
     *
     * @param closeHandler close handler
     * @return on close result
     */
    NIterator<T> onClose(Runnable closeHandler);

    /**
     * Close.
     */
    default void close() {

    }
}
