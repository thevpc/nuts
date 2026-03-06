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
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Collections related Internal Programming Interface
 */
public interface NCollectionsRPI extends NComponent {
    static NCollectionsRPI of() {
        return NExtensions.of(NCollectionsRPI.class);
    }

    <T> NStream<T> arrayToStream(T[] str);

    <T> NStream<T> iterableToStream(Iterable<T> str);

    <T> NStream<T> iteratorToStream(Iterator<T> str);
    <T> NStream<T> iteratorToStream(Iterator<T> str,Runnable onClose);

    <T> NStream<T> toStream(Stream<T> str);

    <T> NStream<T> emptyStream();

    <T> NIterator<T> emptyIterator();

    <T> NIterator<T> toIterator(Iterator<T> str);

    <T> NIterable<T> toIterable(Iterable<T> str);

    <T> NStream<T> optionalToStream(Optional<T> str);

    <T> NStream<T> optionalToStream(NOptional<T> str);

    <T> NChunkedStoreBuilder<T> chunkedStoreBuilder(NPath folder, NChunkedStoreFactory<T> storeFactory);

    NChunkedStoreFactory<String> lineChunkedStoreFactory();

    /**
     * Constructor
     */
    <K extends Comparable<K>, V> NBPlusTree<K, V> btreePlus(int m, boolean allowDuplicates);

    <K extends Comparable<K>, V> NBPlusTree<K, V> btreePlus(int m);
    <K extends Comparable<K>, V> NBPlusTree<K, V> btreePlus(NBPlusTreeStore<K, V> store);

    <V> NClassMap<V> classMap(Class<V> valueType);

    <V> NClassMap<V> classMap(Class keyType, Class<V> valueType);

    <V> NClassMap<V> classMap(Class keyType, Class<V> valueType, int initialCapacity);

    NClassMap<Class<?>> classClassMap();

    <T> Map<String, T> caseInsensitiveMap();

    <T> Map<String, T> caseInsensitiveMap(Map<String, T> other);

    <K, V> NMultiKeyMap<K, V> multiKeyMap();

    <V> NStringMap<V> stringMap(Map<String, V> map, char separator);

    <T> NIterator<T> iteratorWithDescription(NIterator<T> base, Supplier<NElement> description, Runnable onClose);

    <T> NIterator<T> iteratorAutoClosable(NIterator<T> t, NRunnable close);

    <T> NIterator<T> iteratorSafe(NIteratorErrorHandlerType type, NIterator<T> t);

    <T> NIterator<T> iteratorSafeIgnore(NIterator<T> t);

    <T> NIterator<T> iteratorSafePostpone(NIterator<T> t);

    <T> boolean iteratorIsNullOrEmpty(Iterator<T> t);

    <T> NIterator<T> iteratorNonNull(NIterator<T> t);

    <T> NIterator<T> iteratorConcat(List<NIterator<? extends T>> all);

    <T> NIterator<T> iteratorCoalesce2(List<NIterator<T>> all);

    <T> NIterator<T> iteratorCoalesce(NIterator<? extends T>... all);

    <T> NIterator<T> iteratorConcat(NIterator<? extends T>... all);

    <T> NIterator<T> iteratorConcatLists(List<NIterator<? extends T>>... all);

    <T> NIterator<T> iteratorCoalesce(List<NIterator<? extends T>> all);

    <F, T> NIterator<T> iteratorConvertNonNull(NIterator<F> from, Function<F, T> converter, String name);

    <T> List<T> iteratorToList(Iterator<T> it);

    <T> Set<T> iteratorToSet(NIterator<T> it);

    <T> Set<T> iteratorToTreeSet(NIterator<T> it, NComparator<T> c);

    <T> NIterator<T> iteratorSort(NIterator<T> it, NComparator<T> c, boolean removeDuplicates);

    <T> NIterator<T> iteratorDistinct(NIterator<T> it);

    <F, T> NIterator<F> iteratorDistinct(NIterator<F> it, Function<F, T> converter);

    <T> NIterator<T> iteratorCollector(Iterator<T> it, Consumer<T> consumer);

    <T> NIterator<T> iteratorNullifyIfEmpty(NIterator<T> other);

    <T> NIteratorBuilder<T> iteratorBuilderOfCoalesce(List<NIterator<? extends T>> t);

    <T> NIteratorBuilder<T> iteratorBuilderOfConcat(List<NIterator<? extends T>> t);

    <T> NIteratorBuilder<T> iteratorBuilder(Iterator<T> t);

    <T> NIteratorBuilder<T> iteratorBuilderOfRunnable(NRunnable t);

    <T> NIteratorBuilder<T> iteratorBuilderOfRunnable(Runnable t, String n);

    <T> NIteratorBuilder<T> iteratorBuilderOfSupplier(Supplier<Iterator<T>> from, Supplier<NElement> name);

    <T> NIteratorBuilder<T> iteratorBuilderOfArrayValues(T[] t, NElement n);

    <T> NIteratorBuilder<T> iteratorBuilderOfArrayValues(T[] t, String n);

    <T> NIteratorBuilder<T> iteratorBuilderOfArrayValues(T[] t, Supplier<NElement> n);

    <T> NIteratorBuilder<T> iteratorEmptyBuilder();

    <T> NIteratorBuilder<T> iteratorBuilderOfFlatMap(NIterator<? extends Collection<T>> from);
}
