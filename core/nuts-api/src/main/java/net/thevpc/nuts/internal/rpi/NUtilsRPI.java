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

import net.thevpc.nuts.collections.*;
import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NDataSerializer;
import net.thevpc.nuts.io.NPageStore;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.pipeline.*;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.text.NMsgType;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Collections related Internal Programming Interface
 */
public interface NUtilsRPI extends NComponent {
    static NUtilsRPI of() {
        return NExtensions.of(NUtilsRPI.class);
    }

    <T> NStream<T> arrayToStream(T[] str);

    <T> NStream<T> iterableToStream(Iterable<T> str);

    <T> NStream<T> iteratorToStream(Iterator<T> str);

    <T> NStream<T> iteratorToStream(Iterator<T> str, Runnable onClose);

    <T> NStream<T> streamToNStream(Stream<T> str);

    <T> NStream<T> createEmptyStream();

    <T> NIterator<T> createEmptyIterator();

    <T> NIterator<T> iteratorToNIterator(Iterator<T> str);

    <T> NIterable<T> iterableToNIterable(Iterable<T> str);

    <T> NStream<T> optionalToStream(Optional<T> str);

    <T> NStream<T> optionalToStream(NOptional<T> str);

    <T> NChunkedStoreBuilder<T> createChunkedStoreBuilder(NPath folder, NChunkedStoreFactory<T> storeFactory);

    NChunkedStoreFactory<String> createLineChunkedStoreFactory();

    /**
     * Constructor
     */
    <K, V> NBPlusTree<K, V> createBtreePlus(int order, boolean allowDuplicates, Comparator<K> comparator);

    <K, V> NBPlusTree<K, V> createBtreePlus(NPageStore store, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer, Comparator<K> comparator);

    NPageStore createInMemoryPageStore(int pageSize);

    NPageStore createFilePageStore(NPath path, int pageSize);

    <K, V> NClassMap<K, V> createClassMap(Class<V> valueType);

    <K, V> NClassMap<K, V> createClassMap(Class<K> keyType, Class<V> valueType);

    <A, B, V> NClassPairMap<A, B, V> createClassPairMap(Class<A> baseKey1Type, Class<B> baseKey2Type, Class<V> valueType, boolean symmetric);

    <A, B, V> NClassPairMultiMap<A, B, V> createClassPairMultiMap(Class<A> baseKey1Type, Class<B> baseKey2Type, Class<V> valueType, boolean symmetric);

    <K, V> NClassMap<K, V> createClassMap(Class<K> keyType, Class<V> valueType, int initialCapacity);

    NClassMap<Object, Class> createClassClassMap();

    <T> NNormalizedStringMap<T> createInsensitiveMap();

    <K, V> NMultiKeyMap<K, V> createMultiKeyMap();

    <V> NStringMap<V> createStringMap(Map<String, V> map, char separator);

    <T> NIterator<T> iteratorWithDescription(NIterator<T> base, Supplier<NElement> description, Runnable onClose);

    <T> NIterator<T> createIteratorAutoClosable(NIterator<T> t, NRunnable close);

    <T> NIterator<T> createIteratorSafe(NIteratorErrorHandlerType type, NIterator<T> t);

    <T> NIterator<T> createIteratorSafeIgnore(NIterator<T> t);

    <T> NIterator<T> createIteratorSafePostpone(NIterator<T> t);

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

    NIterator<NIntTuple2> int2Iterator(int a, int b);

    <K, V> NClassMultiMap<K, V> createClassMultiMap(Class<K> key1Type, Class<V> valueType);

    <T> NClassDecisionFilter<T> createClassDecisionFilter(Class<T> type, NDecision defaultDecision);

    <T> NDecisionFilter<T> createDecisionFilter(Class<T> type, NDecisionConflict decisionConflict, NDecision defaultDecision);

    <K, V> NListMultiValueMap<K, V> createListMultiValueMap();

    <K, V> NListMultiValueMap<K, V> createListMultiValueMap(Map<K, List<V>> map);

    <T> NNormalizedStringMap<T> createFormatInsensitiveMap();

    <T> NNormalizedStringMap<T> createNormalizedMap(Function<String, String> normalizer);

    <K, V> NLRUMap<K, V> createLruMap(int size);

    NEvictingByteQueue createEvictingByteQueue(int size);

    NEvictingCharQueue createEvictingCharQueue(int size);

    NEvictingIntQueue createEvictingIntQueue(int size);

    <T> NEvictingQueue<T> createEvictingQueue(int size);

    <K, V> NIndexedMap<K, V> createIndexedMap();

    <K, V> NSetMultiValueMap<K, V> createSetMultiValueMap();

    <K, V> NSetMultiValueMap<K, V> createSetMultiValueMap(Map<K, Set<V>> map);


    List<String> extractMessageParams(String msg, NMsgType type, String customMessageTypeId);

    NByteList createByteList(int initialSize);

    NByteList createByteList();

    NByteList createByteList(byte[] values, int offset, int size);

    NIntList createIntList(int initialSize);

    NIntList createIntList();

    NIntList createIntList(int[] values, int offset, int size);

    NLongList createLongList(int initialSize);

    NLongList createLongList();

    NLongList createLongList(long[] values, int offset, int size);

    NDoubleList createDoubleList(int initialSize);

    NDoubleList createDoubleList();

    NDoubleList createDoubleList(double[] values, int offset, int size);

    <K, V> NObservableMap<K, V> createObservableMap();

    <K, V> NObservableMap<K, V> createObservableMap(Map<K, V> base);

    <K> NObservableSet<K> createObservableSet();

    <K> NObservableSet<K> createObservableSet(Set<K> base);

    <K> NObservableList<K> createObservableList();

    <K> NObservableList<K> createObservableList(List<K> base);

    <V, K> NOptionalMap<K, V> createOptionalMap();

    <V, K> NOptionalMap<K, V> createOptionalMap(Map<K, V> base);

    NCharQueue createCharQueue();

    NCharQueue createCharQueue(int size);

    NCharQueue createCharQueue(int size, int increment);

    NCharQueue createCharQueue(char[] content);

    NByteQueue createByteQueue();

    NByteQueue createByteQueue(int size);

    NByteQueue createByteQueue(int size, int increment);

    NByteQueue createByteQueue(byte[] content);
}
