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

import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Describable Iterator
 *
 * @param <T> T
 */
public interface NIterator<T> extends Iterator<T>, NRedescribable<NIterator<T>>, AutoCloseable{
    static <T> NIterator<T> of(Iterator<T> o) {
        return NCollectionsRPI.of().toIterator(o);
    }

    static <T> NIterator<T> ofEmpty() {
        return NCollectionsRPI.of().emptyIterator();
    }

    static <T> NIterator<T> ofSingleton(T element) {
        return NCollectionsRPI.of().toIterator(Collections.singletonList(element).iterator());
    }

    static <T> NIterator<T> ofWithDescription(NIterator<T> base, Supplier<NElement> description, Runnable onClose) {
        return NCollectionsRPI.of().iteratorWithDescription(base, description, onClose);
    }

    static <T> NIterator<T> ofAutoClosable(NIterator<T> t, NRunnable close) {
        return NCollectionsRPI.of().iteratorAutoClosable(t, close);
    }

    static <T> NIterator<T> ofSafe(NIteratorErrorHandlerType type, NIterator<T> t) {
        return NCollectionsRPI.of().iteratorSafe(type, t);
    }

    static <T> NIterator<T> ofSafeIgnore(NIterator<T> t) {
        return NCollectionsRPI.of().iteratorSafeIgnore(t);
    }

    static <T> NIterator<T> ofSafePostpone(NIterator<T> t) {
        return NCollectionsRPI.of().iteratorSafePostpone(t);
    }

    static <T> boolean isNullOrEmpty(Iterator<T> t) {
        return NCollectionsRPI.of().iteratorIsNullOrEmpty(t);
    }

    static <T> NIterator<T> ofNonNull(NIterator<T> t) {
        return NCollectionsRPI.of().iteratorNonNull(t);
    }

    static <T> NIterator<T> ofConcat(List<NIterator<? extends T>> all) {
        return NCollectionsRPI.of().iteratorConcat(all);
    }

    static <T> NIterator<T> ofCoalesce2(List<NIterator<T>> all) {
        return NCollectionsRPI.of().iteratorCoalesce2(all);
    }

    static <T> NIterator<T> ofCoalesce(NIterator<? extends T>... all) {
        return NCollectionsRPI.of().iteratorCoalesce(all);
    }

    static <T> NIterator<T> ofConcat(NIterator<? extends T>... all) {
        return NCollectionsRPI.of().iteratorConcat(all);
    }

    static <T> NIterator<T> ofConcatLists(List<NIterator<? extends T>>... all) {
        return NCollectionsRPI.of().iteratorConcatLists(all);
    }

    static <T> NIterator<T> ofCoalesce(List<NIterator<? extends T>> all) {
        return NCollectionsRPI.of().iteratorCoalesce(all);
    }


    static <T> List<T> toList(Iterator<T> it) {
        return NCollectionsRPI.of().iteratorToList(it);
    }

    static <T> Set<T> toSet(NIterator<T> it) {
        return NCollectionsRPI.of().iteratorToSet(it);
    }

    static <T> Set<T> toTreeSet(NIterator<T> it, NComparator<T> c) {
        return NCollectionsRPI.of().iteratorToTreeSet(it, c);
    }

    static <T> NIterator<T> ofSorted(NIterator<T> it, NComparator<T> c, boolean removeDuplicates) {
        return NCollectionsRPI.of().iteratorSort(it, c, removeDuplicates);
    }

    static <T> NIterator<T> ofDistinct(NIterator<T> it) {
        return NCollectionsRPI.of().iteratorDistinct(it);
    }

    static <F, T> NIterator<F> ofDistinct(NIterator<F> it, final Function<F, T> converter) {
        return NCollectionsRPI.of().iteratorDistinct(it, converter);
    }

    static <T> NIterator<T> ofCollector(Iterator<T> it, Consumer<T> consumer) {
        return NCollectionsRPI.of().iteratorCollector(it, consumer);
    }

    static <T> NIterator<T> ofNullifyIfEmpty(NIterator<T> other) {
        return NCollectionsRPI.of().iteratorNullifyIfEmpty(other);
    }

    static <F, T> NIterator<T> ofConvertNonNull(NIterator<F> from, Function<F, T> converter, String name) {
        return NCollectionsRPI.of().iteratorConvertNonNull(from, converter, name);
    }

    default List<T> toList() {
        List<T> list = new ArrayList<>();
        while (hasNext()) {
            list.add(next());
        }
        return list;
    }

    NIterator<T> onClose(Runnable closeHandler);

    default void close(){

    }
}
