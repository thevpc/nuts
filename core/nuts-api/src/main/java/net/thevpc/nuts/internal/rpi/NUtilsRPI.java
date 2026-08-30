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

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.collections.*;
import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NDataSerializer;
import net.thevpc.nuts.io.NPageStore;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.pipeline.*;
import net.thevpc.nuts.platform.NRuntimeDistribution;
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
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NUtilsRPI of() {
        return NExtensions.of(NUtilsRPI.class);
    }

    /**
     * Array to stream.
     *
     * @param str str
     * @return array to stream result
     */
    <T> NStream<T> arrayToStream(T[] str);

    /**
     * Iterable to stream.
     *
     * @param str str
     * @return iterable to stream result
     */
    <T> NStream<T> iterableToStream(Iterable<T> str);

    /**
     * Iterator to stream.
     *
     * @param str str
     * @return iterator to stream result
     */
    <T> NStream<T> iteratorToStream(Iterator<T> str);

    /**
     * Iterator to stream.
     *
     * @param str     str
     * @param onClose on close
     * @return iterator to stream result
     */
    <T> NStream<T> iteratorToStream(Iterator<T> str, Runnable onClose);

    /**
     * Stream to n stream.
     *
     * @param str str
     * @return stream to n stream result
     */
    <T> NStream<T> streamToNStream(Stream<T> str);

    /**
     * Creates a new instance of create empty stream.
     *
     * @return create empty stream result
     */
    <T> NStream<T> createEmptyStream();

    /**
     * Creates a new instance of create empty iterator.
     *
     * @return create empty iterator result
     */
    <T> NIterator<T> createEmptyIterator();

    /**
     * Iterator to n iterator.
     *
     * @param str str
     * @return iterator to n iterator result
     */
    <T> NIterator<T> iteratorToNIterator(Iterator<T> str);

    /**
     * Iterable to n iterable.
     *
     * @param str str
     * @return iterable to n iterable result
     */
    <T> NIterable<T> iterableToNIterable(Iterable<T> str);

    /**
     * Optional to stream.
     *
     * @param str str
     * @return optional to stream result
     */
    <T> NStream<T> optionalToStream(Optional<T> str);

    /**
     * Optional to stream.
     *
     * @param str str
     * @return optional to stream result
     */
    <T> NStream<T> optionalToStream(NOptional<T> str);

    /**
     * Creates a new instance of create chunked store builder.
     *
     * @param folder       folder
     * @param storeFactory store factory
     * @return create chunked store builder result
     */
    <T> NChunkedStoreBuilder<T> createChunkedStoreBuilder(NPath folder, NChunkedStoreFactory<T> storeFactory);

    /**
     * Creates a new instance of create line chunked store factory.
     *
     * @return create line chunked store factory result
     */
    NChunkedStoreFactory<String> createLineChunkedStoreFactory();

    /**
     * Constructor
     */
    <K, V> NBPlusTree<K, V> createBtreePlus(int order, boolean allowDuplicates, Comparator<K> comparator);

    /**
     * Creates a new instance of create btree plus.
     *
     * @param store           store
     * @param order           order
     * @param allowDuplicates allow duplicates
     * @param keySerializer   key serializer
     * @param valSerializer   val serializer
     * @param comparator      comparator
     * @return create btree plus result
     */
    <K, V> NBPlusTree<K, V> createBtreePlus(NPageStore store, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer, Comparator<K> comparator);

    /**
     * Creates a new instance of create in memory page store.
     *
     * @param pageSize page size
     * @return create in memory page store result
     */
    NPageStore createInMemoryPageStore(int pageSize);

    /**
     * Creates a new instance of create file page store.
     *
     * @param path     path
     * @param pageSize page size
     * @return create file page store result
     */
    NPageStore createFilePageStore(NPath path, int pageSize);

    /**
     * Creates a new instance of create class map.
     *
     * @param valueType value type
     * @return create class map result
     */
    <K, V> NClassMap<K, V> createClassMap(Class<V> valueType);

    /**
     * Creates a new instance of create class map.
     *
     * @param keyType   key type
     * @param valueType value type
     * @return create class map result
     */
    <K, V> NClassMap<K, V> createClassMap(Class<K> keyType, Class<V> valueType);

    /**
     * Creates a new instance of create class pair map.
     *
     * @param baseKey1Type base key1 type
     * @param baseKey2Type base key2 type
     * @param valueType    value type
     * @param symmetric    symmetric
     * @return create class pair map result
     */
    <A, B, V> NClassPairMap<A, B, V> createClassPairMap(Class<A> baseKey1Type, Class<B> baseKey2Type, Class<V> valueType, boolean symmetric);

    /**
     * Creates a new instance of create class pair multi map.
     *
     * @param baseKey1Type base key1 type
     * @param baseKey2Type base key2 type
     * @param valueType    value type
     * @param symmetric    symmetric
     * @return create class pair multi map result
     */
    <A, B, V> NClassPairMultiMap<A, B, V> createClassPairMultiMap(Class<A> baseKey1Type, Class<B> baseKey2Type, Class<V> valueType, boolean symmetric);

    /**
     * Creates a new instance of create class map.
     *
     * @param keyType         key type
     * @param valueType       value type
     * @param initialCapacity initial capacity
     * @return create class map result
     */
    <K, V> NClassMap<K, V> createClassMap(Class<K> keyType, Class<V> valueType, int initialCapacity);

    /**
     * Creates a new instance of create class class map.
     *
     * @return create class class map result
     */
    NClassMap<Object, Class> createClassClassMap();

    /**
     * Creates a new instance of create insensitive map.
     *
     * @return create insensitive map result
     */
    <T> Map<String, T> createInsensitiveMap();

    /**
     * Creates a new instance of create multi key map.
     *
     * @return create multi key map result
     */
    <K, V> NMultiKeyMap<K, V> createMultiKeyMap();

    /**
     * Creates a new instance of create string map.
     *
     * @param map       map
     * @param separator separator
     * @return create string map result
     */
    <V> NStringMap<V> createStringMap(Map<String, V> map, char separator);

    /**
     * Iterator with description.
     *
     * @param base        base
     * @param description description
     * @param onClose     on close
     * @return iterator with description result
     */
    <T> NIterator<T> iteratorWithDescription(NIterator<T> base, Supplier<NElement> description, Runnable onClose);

    /**
     * Creates a new instance of create iterator auto closable.
     *
     * @param t     t
     * @param close close
     * @return create iterator auto closable result
     */
    <T> NIterator<T> createIteratorAutoClosable(NIterator<T> t, NRunnable close);

    /**
     * Creates a new instance of create iterator safe.
     *
     * @param type type
     * @param t    t
     * @return create iterator safe result
     */
    <T> NIterator<T> createIteratorSafe(NIteratorErrorHandlerType type, NIterator<T> t);

    /**
     * Creates a new instance of create iterator safe ignore.
     *
     * @param t t
     * @return create iterator safe ignore result
     */
    <T> NIterator<T> createIteratorSafeIgnore(NIterator<T> t);

    /**
     * Creates a new instance of create iterator safe postpone.
     *
     * @param t t
     * @return create iterator safe postpone result
     */
    <T> NIterator<T> createIteratorSafePostpone(NIterator<T> t);

    /**
     * Iterator is null or empty.
     *
     * @param t t
     * @return iterator is null or empty result
     */
    <T> boolean iteratorIsNullOrEmpty(Iterator<T> t);

    /**
     * Iterator non null.
     *
     * @param t t
     * @return iterator non null result
     */
    <T> NIterator<T> iteratorNonNull(NIterator<T> t);

    /**
     * Iterator concat.
     *
     * @param all all
     * @return iterator concat result
     */
    <T> NIterator<T> iteratorConcat(List<NIterator<? extends T>> all);

    /**
     * Iterator coalesce2.
     *
     * @param all all
     * @return iterator coalesce2 result
     */
    <T> NIterator<T> iteratorCoalesce2(List<NIterator<T>> all);

    /**
     * Iterator coalesce.
     *
     * @param all all
     * @return iterator coalesce result
     */
    <T> NIterator<T> iteratorCoalesce(NIterator<? extends T>... all);

    /**
     * Iterator concat.
     *
     * @param all all
     * @return iterator concat result
     */
    <T> NIterator<T> iteratorConcat(NIterator<? extends T>... all);

    /**
     * Iterator concat lists.
     *
     * @param all all
     * @return iterator concat lists result
     */
    <T> NIterator<T> iteratorConcatLists(List<NIterator<? extends T>>... all);

    /**
     * Iterator coalesce.
     *
     * @param all all
     * @return iterator coalesce result
     */
    <T> NIterator<T> iteratorCoalesce(List<NIterator<? extends T>> all);

    /**
     * Iterator convert non null.
     *
     * @param from      from
     * @param converter converter
     * @param name      name
     * @return iterator convert non null result
     */
    <F, T> NIterator<T> iteratorConvertNonNull(NIterator<F> from, Function<F, T> converter, String name);

    /**
     * Iterator to list.
     *
     * @param it it
     * @return iterator to list result
     */
    <T> List<T> iteratorToList(Iterator<T> it);

    /**
     * Iterator to set.
     *
     * @param it it
     * @return iterator to set result
     */
    <T> Set<T> iteratorToSet(NIterator<T> it);

    /**
     * Iterator to tree set.
     *
     * @param it it
     * @param c  c
     * @return iterator to tree set result
     */
    <T> Set<T> iteratorToTreeSet(NIterator<T> it, NComparator<T> c);

    /**
     * Iterator sort.
     *
     * @param it               it
     * @param c                c
     * @param removeDuplicates remove duplicates
     * @return iterator sort result
     */
    <T> NIterator<T> iteratorSort(NIterator<T> it, NComparator<T> c, boolean removeDuplicates);

    /**
     * Iterator distinct.
     *
     * @param it it
     * @return iterator distinct result
     */
    <T> NIterator<T> iteratorDistinct(NIterator<T> it);

    /**
     * Iterator distinct.
     *
     * @param it        it
     * @param converter converter
     * @return iterator distinct result
     */
    <F, T> NIterator<F> iteratorDistinct(NIterator<F> it, Function<F, T> converter);

    /**
     * Iterator collector.
     *
     * @param it       it
     * @param consumer consumer
     * @return iterator collector result
     */
    <T> NIterator<T> iteratorCollector(Iterator<T> it, Consumer<T> consumer);

    /**
     * Iterator nullify if empty.
     *
     * @param other other
     * @return iterator nullify if empty result
     */
    <T> NIterator<T> iteratorNullifyIfEmpty(NIterator<T> other);

    /**
     * Iterator builder of coalesce.
     *
     * @param t t
     * @return iterator builder of coalesce result
     */
    <T> NIteratorBuilder<T> iteratorBuilderOfCoalesce(List<NIterator<? extends T>> t);

    /**
     * Iterator builder of concat.
     *
     * @param t t
     * @return iterator builder of concat result
     */
    <T> NIteratorBuilder<T> iteratorBuilderOfConcat(List<NIterator<? extends T>> t);

    /**
     * Iterator builder.
     *
     * @param t t
     * @return iterator builder result
     */
    <T> NIteratorBuilder<T> iteratorBuilder(Iterator<T> t);

    /**
     * Iterator builder of runnable.
     *
     * @param t t
     * @return iterator builder of runnable result
     */
    <T> NIteratorBuilder<T> iteratorBuilderOfRunnable(NRunnable t);

    /**
     * Iterator builder of runnable.
     *
     * @param t t
     * @param n n
     * @return iterator builder of runnable result
     */
    <T> NIteratorBuilder<T> iteratorBuilderOfRunnable(Runnable t, String n);

    /**
     * Iterator builder of supplier.
     *
     * @param from from
     * @param name name
     * @return iterator builder of supplier result
     */
    <T> NIteratorBuilder<T> iteratorBuilderOfSupplier(Supplier<Iterator<T>> from, Supplier<NElement> name);

    /**
     * Iterator builder of array values.
     *
     * @param t t
     * @param n n
     * @return iterator builder of array values result
     */
    <T> NIteratorBuilder<T> iteratorBuilderOfArrayValues(T[] t, NElement n);

    /**
     * Iterator builder of array values.
     *
     * @param t t
     * @param n n
     * @return iterator builder of array values result
     */
    <T> NIteratorBuilder<T> iteratorBuilderOfArrayValues(T[] t, String n);

    /**
     * Iterator builder of array values.
     *
     * @param t t
     * @param n n
     * @return iterator builder of array values result
     */
    <T> NIteratorBuilder<T> iteratorBuilderOfArrayValues(T[] t, Supplier<NElement> n);

    /**
     * Iterator empty builder.
     *
     * @return iterator empty builder result
     */
    <T> NIteratorBuilder<T> iteratorEmptyBuilder();

    /**
     * Iterator builder of flat map.
     *
     * @param from from
     * @return iterator builder of flat map result
     */
    <T> NIteratorBuilder<T> iteratorBuilderOfFlatMap(NIterator<? extends Collection<T>> from);

    /**
     * Int2 iterator.
     *
     * @param a a
     * @param b b
     * @return int2 iterator result
     */
    NIterator<NIntPair> int2Iterator(int a, int b);

    /**
     * Creates a new instance of create class multi map.
     *
     * @param key1Type  key1 type
     * @param valueType value type
     * @return create class multi map result
     */
    <K, V> NClassMultiMap<K, V> createClassMultiMap(Class<K> key1Type, Class<V> valueType);

    /**
     * Creates a new instance of create class decision filter.
     *
     * @param type            type
     * @param defaultDecision default decision
     * @return create class decision filter result
     */
    <T> NClassDecisionFilter<T> createClassDecisionFilter(Class<T> type, NDecision defaultDecision);

    /**
     * Creates a new instance of create decision filter.
     *
     * @param type             type
     * @param decisionConflict decision conflict
     * @param defaultDecision  default decision
     * @return create decision filter result
     */
    <T> NDecisionFilter<T> createDecisionFilter(Class<T> type, NDecisionConflict decisionConflict, NDecision defaultDecision);

    /**
     * Creates a new instance of create list multi value map.
     *
     * @return create list multi value map result
     */
    <K, V> NListMultiValueMap<K, V> createListMultiValueMap();

    /**
     * Creates a new instance of create list multi value map.
     *
     * @param map map
     * @return create list multi value map result
     */
    <K, V> NListMultiValueMap<K, V> createListMultiValueMap(Map<K, List<V>> map);

    /**
     * Creates a new instance of create format insensitive map.
     *
     * @return create format insensitive map result
     */
    <T> Map<String, T> createFormatInsensitiveMap();

    /**
     * Creates a new instance of create normalized map.
     *
     * @param normalizer normalizer
     * @return create normalized map result
     */
    <T> Map<String, T> createNormalizedMap(Function<String, String> normalizer);

    /**
     * Creates a new instance of create lru map.
     *
     * @param size size
     * @return create lru map result
     */
    <K, V> NCappedMap<K, V> createLruMap(int size);

    /**
     * Creates a new instance of create evicting byte queue.
     *
     * @param size size
     * @return create evicting byte queue result
     */
    NEvictingByteQueue createEvictingByteQueue(int size);

    /**
     * Creates a new instance of create evicting char queue.
     *
     * @param size size
     * @return create evicting char queue result
     */
    NEvictingCharQueue createEvictingCharQueue(int size);

    /**
     * Creates a new instance of create evicting int queue.
     *
     * @param size size
     * @return create evicting int queue result
     */
    NEvictingIntQueue createEvictingIntQueue(int size);

    /**
     * Creates a new instance of create evicting queue.
     *
     * @param size size
     * @return create evicting queue result
     */
    <T> NEvictingQueue<T> createEvictingQueue(int size);

    /**
     * Creates a new instance of create indexed map.
     *
     * @return create indexed map result
     */
    <K, V> NIndexedMap<K, V> createIndexedMap();

    /**
     * Creates a new instance of create set multi value map.
     *
     * @return create set multi value map result
     */
    <K, V> NSetMultiValueMap<K, V> createSetMultiValueMap();

    /**
     * Creates a new instance of create set multi value map.
     *
     * @param map map
     * @return create set multi value map result
     */
    <K, V> NSetMultiValueMap<K, V> createSetMultiValueMap(Map<K, Set<V>> map);


    /**
     * Extract message params.
     *
     * @param msg                 msg
     * @param type                type
     * @param customMessageTypeId custom message type id
     * @return extract message params result
     */
    List<String> extractMessageParams(String msg, NMsgType type, String customMessageTypeId);

    /**
     * Creates a new instance of create byte list.
     *
     * @param initialSize initial size
     * @return create byte list result
     */
    NByteList createByteList(int initialSize);

    /**
     * Creates a new instance of create byte list.
     *
     * @return create byte list result
     */
    NByteList createByteList();

    /**
     * Creates a new instance of create byte list.
     *
     * @param values values
     * @param offset offset
     * @param size   size
     * @return create byte list result
     */
    NByteList createByteList(byte[] values, int offset, int size);

    /**
     * Creates a new instance of create int list.
     *
     * @param initialSize initial size
     * @return create int list result
     */
    NIntList createIntList(int initialSize);

    /**
     * Creates a new instance of create int list.
     *
     * @return create int list result
     */
    NIntList createIntList();

    /**
     * Creates a new instance of create int list.
     *
     * @param values values
     * @param offset offset
     * @param size   size
     * @return create int list result
     */
    NIntList createIntList(int[] values, int offset, int size);

    /**
     * Creates a new instance of create long list.
     *
     * @param initialSize initial size
     * @return create long list result
     */
    NLongList createLongList(int initialSize);

    /**
     * Creates a new instance of create long list.
     *
     * @return create long list result
     */
    NLongList createLongList();

    /**
     * Creates a new instance of create long list.
     *
     * @param values values
     * @param offset offset
     * @param size   size
     * @return create long list result
     */
    NLongList createLongList(long[] values, int offset, int size);

    /**
     * Creates a new instance of create double list.
     *
     * @param initialSize initial size
     * @return create double list result
     */
    NDoubleList createDoubleList(int initialSize);

    /**
     * Creates a new instance of create double list.
     *
     * @return create double list result
     */
    NDoubleList createDoubleList();

    /**
     * Creates a new instance of create double list.
     *
     * @param values values
     * @param offset offset
     * @param size   size
     * @return create double list result
     */
    NDoubleList createDoubleList(double[] values, int offset, int size);

    /**
     * Creates a new instance of create float list.
     *
     * @param initialSize initial size
     * @return create float list result
     */
    NFloatList createFloatList(int initialSize);

    /**
     * Creates a new instance of create float list.
     *
     * @return create float list result
     */
    NFloatList createFloatList();

    /**
     * Creates a new instance of create float list.
     *
     * @param values values
     * @param offset offset
     * @param size   size
     * @return create float list result
     */
    NFloatList createFloatList(float[] values, int offset, int size);

    /**
     * Creates a new instance of create properties.
     *
     * @return create properties result
     */
    NProperties createProperties();

    /**
     * Creates a new instance of create observable map.
     *
     * @return create observable map result
     */
    <K, V> NObservableMap<K, V> createObservableMap();

    /**
     * Creates a new instance of create observable map.
     *
     * @param base base
     * @return create observable map result
     */
    <K, V> NObservableMap<K, V> createObservableMap(Map<K, V> base);

    /**
     * Creates a new instance of create observable set.
     *
     * @return create observable set result
     */
    <K> NObservableSet<K> createObservableSet();

    /**
     * Creates a new instance of create observable set.
     *
     * @param base base
     * @return create observable set result
     */
    <K> NObservableSet<K> createObservableSet(Set<K> base);

    /**
     * Creates a new instance of create observable list.
     *
     * @return create observable list result
     */
    <K> NObservableList<K> createObservableList();

    /**
     * Creates a new instance of create observable list.
     *
     * @param base base
     * @return create observable list result
     */
    <K> NObservableList<K> createObservableList(List<K> base);

    /**
     * Creates a new instance of create optional map.
     *
     * @return create optional map result
     */
    <V, K> NOptionalMap<K, V> createOptionalMap();

    /**
     * Creates a new instance of create optional map.
     *
     * @param base base
     * @return create optional map result
     */
    <V, K> NOptionalMap<K, V> createOptionalMap(Map<K, V> base);

    /**
     * Creates a new instance of create char queue.
     *
     * @return create char queue result
     */
    NCharQueue createCharQueue();

    /**
     * Creates a new instance of create char queue.
     *
     * @param size size
     * @return create char queue result
     */
    NCharQueue createCharQueue(int size);

    /**
     * Creates a new instance of create char queue.
     *
     * @param size      size
     * @param increment increment
     * @return create char queue result
     */
    NCharQueue createCharQueue(int size, int increment);

    /**
     * Creates a new instance of create char queue.
     *
     * @param content content
     * @return create char queue result
     */
    NCharQueue createCharQueue(char[] content);

    /**
     * Creates a new instance of create byte queue.
     *
     * @return create byte queue result
     */
    NByteQueue createByteQueue();

    /**
     * Creates a new instance of create byte queue.
     *
     * @param size size
     * @return create byte queue result
     */
    NByteQueue createByteQueue(int size);

    /**
     * Creates a new instance of create byte queue.
     *
     * @param size      size
     * @param increment increment
     * @return create byte queue result
     */
    NByteQueue createByteQueue(int size, int increment);

    /**
     * Creates a new instance of create byte queue.
     *
     * @param content content
     * @return create byte queue result
     */
    NByteQueue createByteQueue(byte[] content);


    <A, B> List<B> createImmutableConvertedList(List<A> base, Function<A, B> converter);

    <K, V> NCappedMap<K, V> createConcurrentReadWriteLRUMap(int size);

    <T, K> NCollectionDiffBuilder<T, K> createCollectionDiffBuilder();

    NRuntimeDistribution createRuntimeDistribution(NId id, String vendor, String product, String variant, String name, String path, String version, String packaging, int priority);

    NLiteral createLiteral(Object any);

    NArg createCmdlineArg(String value, NCmdLine cmdline);

    NStringBuilder createStringBuilder(String value);
}
