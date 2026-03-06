package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.internal.rpi.NCollectionsRPI;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.stream.NStreamBase;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@NComponentScope(NScopeType.SESSION)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNCollectionsRPI implements NCollectionsRPI {

    public DefaultNCollectionsRPI() {
    }

    @Override
    public <T> NStream<T> arrayToStream(T[] str) {
        String name = null;
        if (str == null) {
            return NStreamBase.ofEmpty(name);
        }
        return iterableToStream(Arrays.asList(str)).withDescription(() -> NElement.ofString("array"));
    }

    @Override
    public <T> NStream<T> iterableToStream(Iterable<T> str) {
        String name = null;
        if (str == null) {
            return NStreamBase.ofEmpty(name);
        }
        if (str instanceof Collection) {
            return NStreamBase.ofCollection(name, (Collection<T>) str);
        }

        return NStreamBase.ofIterable(name, NIterable.of(str));
    }

    @Override
    public <T> NStream<T> iteratorToStream(Iterator<T> str) {
        return NStreamBase.ofIterator(null,
                NIterator.of(str)
        );
    }

    @Override
    public <T> NStream<T> iteratorToStream(Iterator<T> str, Runnable onClose) {
        return NStreamBase.ofIterator(null,
                NIterator.of(str),
                onClose
        );
    }

    @Override
    public <T> NStream<T> toStream(Stream<T> str) {
        return NStreamBase.ofJavaStream(null, str);
    }

    @Override
    public <T> NStream<T> emptyStream() {
        return NStreamBase.ofEmpty(null);
    }

    @Override
    public <T> NIterator<T> emptyIterator() {
        return NIteratorBuilderImpl.EMPTY_ITERATOR;
    }

    @Override
    public <T> NIterator<T> toIterator(Iterator<T> str) {
        if (str == null) {
            return null;
        }
        if (str instanceof NIterator<?>) {
            return (NIterator<T>) str;
        }
        return new NIteratorBaseFromJavaIterator<>(str);
    }

    @Override
    public <T> NIterable<T> toIterable(Iterable<T> o) {
        if (o == null) {
            return null;
        }
        if (o instanceof NIterable) {
            return (NIterable<T>) o;
        }
        return new NIterableFromJavaIterable<>(o);
    }


    @Override
    public <T> NStream<T> optionalToStream(Optional<T> item) {
        if (item == null || !item.isPresent()) {
            return emptyStream();
        }
        return NStream.ofArray(item.get());
    }

    @Override
    public <T> NStream<T> optionalToStream(NOptional<T> item) {
        if (item == null || !item.isPresent() || item.isError()) {
            return emptyStream();
        }
        return NStream.ofArray(item.get());
    }

    public NChunkedStoreBuilder<String> lineChunkedStoreWriterBuilder(NPath folder) {
        return chunkedStoreBuilder(folder, new LineNChunkedStoreFactory());
    }

    public <T> NChunkedStoreBuilder<T> chunkedStoreBuilder(NPath folder, NChunkedStoreFactory<T> storeFactory) {
        return NChunkedStoreBuilderImpl.of(folder, storeFactory);
    }

    @Override
    public NChunkedStoreFactory<String> lineChunkedStoreFactory() {
        return new LineNChunkedStoreFactory();
    }

    /**
     * Constructor
     */
    public <K extends Comparable<K>, V> NBPlusTree<K, V> btreePlus(int m, boolean allowDuplicates) {
        return new NBPlusTreeImpl<>(new NBPlusTreeStoreMem<K, V>(m, allowDuplicates));
    }

    public <K extends Comparable<K>, V> NBPlusTree<K, V> btreePlus(int m) {
        return new NBPlusTreeImpl<>(new NBPlusTreeStoreMem<K, V>(m, false));
    }

    public <K extends Comparable<K>, V> NBPlusTree<K, V> btreePlus(NBPlusTreeStore<K, V> store) {
        return new NBPlusTreeImpl<>(store);
    }

    public <K extends Comparable<K>, V> NBTreeMapImpl<K, V> btreeMap(int order) {
        return new NBTreeMapImpl<>(order);
    }

    public <T extends Comparable<T>> NBTreeSet<T> btreeSet(int order) {
        return new NBTreeSetImpl<>(order);
    }

    @Override
    public <V> NClassMap<V> classMap(Class<V> valueType) {
        return new NClassMapImpl<>(valueType);
    }

    @Override
    public <V> NClassMap<V> classMap(Class keyType, Class<V> valueType) {
        return new NClassMapImpl<>(keyType, valueType);
    }

    @Override
    public <V> NClassMap<V> classMap(Class keyType, Class<V> valueType, int initialCapacity) {
        return new NClassMapImpl<>(keyType, valueType, initialCapacity);
    }

    @Override
    public NClassMap<Class<?>> classClassMap() {
        return new NClassClassMap();
    }

    @Override
    public <T> Map<String, T> caseInsensitiveMap() {
        return new NCaseInsensitiveStringMap<>();
    }

    @Override
    public <T> Map<String, T> caseInsensitiveMap(Map<String, T> other) {
        return new NCaseInsensitiveStringMap<>(other);
    }

    @Override
    public <K, V> NMultiKeyMap<K, V> multiKeyMap() {
        return new NMultiKeyMapImpl<>();
    }

    @Override
    public <V> NStringMap<V> stringMap(Map<String, V> map, char separator) {
        return new NStringMapImpl<>(map, separator);
    }

    @Override
    public <T> NIterator<T> iteratorWithDescription(NIterator<T> base, Supplier<NElement> description, Runnable onClose) {
        return NIteratorsImpl.withDescription(base, description, onClose);
    }


    @Override
    public <T> NIterator<T> iteratorAutoClosable(NIterator<T> t, NRunnable close) {
        return NIteratorsImpl.autoClosable(t, close);
    }

    @Override
    public <T> NIterator<T> iteratorSafe(NIteratorErrorHandlerType type, NIterator<T> t) {
        return NIteratorsImpl.safe(type, t);
    }

    @Override
    public <T> NIterator<T> iteratorSafeIgnore(NIterator<T> t) {
        return NIteratorsImpl.safeIgnore(t);
    }

    @Override
    public <T> NIterator<T> iteratorSafePostpone(NIterator<T> t) {
        return NIteratorsImpl.safePostpone(t);
    }

    @Override
    public <T> boolean iteratorIsNullOrEmpty(Iterator<T> t) {
        return NIteratorsImpl.isNullOrEmpty(t);
    }

    @Override
    public <T> NIterator<T> iteratorNonNull(NIterator<T> t) {
        return NIteratorsImpl.nonNull(t);
    }

    @Override
    public <T> NIterator<T> iteratorConcat(List<NIterator<? extends T>> all) {
        return NIteratorsImpl.concat(all);
    }

    @Override
    public <T> NIterator<T> iteratorCoalesce2(List<NIterator<T>> all) {
        return NIteratorsImpl.coalesce2(all);
    }

    @Override
    public <T> NIterator<T> iteratorCoalesce(NIterator<? extends T>... all) {
        return NIteratorsImpl.coalesce(all);
    }

    @Override
    public <T> NIterator<T> iteratorConcat(NIterator<? extends T>... all) {
        return NIteratorsImpl.concat(all);
    }

    @Override
    public <T> NIterator<T> iteratorConcatLists(List<NIterator<? extends T>>... all) {
        return NIteratorsImpl.concatLists(all);
    }

    @Override
    public <T> NIterator<T> iteratorCoalesce(List<NIterator<? extends T>> all) {
        return NIteratorsImpl.coalesce(all);
    }

    @Override
    public <F, T> NIterator<T> iteratorConvertNonNull(NIterator<F> from, Function<F, T> converter, String name) {
        return NIteratorsImpl.convertNonNull(from, converter, name);
    }

    @Override
    public <T> List<T> iteratorToList(Iterator<T> it) {
        return NIteratorsImpl.toList(it);
    }

    @Override
    public <T> Set<T> iteratorToSet(NIterator<T> it) {
        return NIteratorsImpl.toSet(it);
    }

    @Override
    public <T> Set<T> iteratorToTreeSet(NIterator<T> it, NComparator<T> c) {
        return NIteratorsImpl.toTreeSet(it, c);
    }

    @Override
    public <T> NIterator<T> iteratorSort(NIterator<T> it, NComparator<T> c, boolean removeDuplicates) {
        return NIteratorsImpl.sort(it, c, removeDuplicates);
    }

    @Override
    public <T> NIterator<T> iteratorDistinct(NIterator<T> it) {
        return NIteratorsImpl.distinct(it);
    }

    @Override
    public <F, T> NIterator<F> iteratorDistinct(NIterator<F> it, final Function<F, T> converter) {
        return NIteratorsImpl.distinct(it, converter);
    }

    @Override
    public <T> NIterator<T> iteratorCollector(Iterator<T> it, Consumer<T> consumer) {
        return NIteratorsImpl.collector(it, consumer);
    }

    @Override
    public <T> NIterator<T> iteratorNullifyIfEmpty(NIterator<T> other) {
        return NIteratorsImpl.nullifyIfEmpty(other);
    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilderOfCoalesce(List<NIterator<? extends T>> t) {
        return NIteratorsImpl.builderOfCoalesce(t);
    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilderOfConcat(List<NIterator<? extends T>> t) {
        return NIteratorsImpl.builderOfConcat(t);
    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilder(Iterator<T> t) {
        return NIteratorsImpl.builder(t);
    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilderOfRunnable(NRunnable t) {
        return NIteratorsImpl.builderOfRunnable(t);
    }

//    public <T> IteratorBuilder<T> ofRunnable(Runnable t, NElement n) {
//        return ofRunnable(NRunnable.of(t, n));
//    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilderOfRunnable(Runnable t, String n) {
        return NIteratorsImpl.ofRunnable(t, n);
    }
//
//    public <T> IteratorBuilder<T> ofSupplier(Supplier<NutsIterator<T>> from) {
//        return of(new SupplierIterator<T>(from, null));
//    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilderOfSupplier(Supplier<Iterator<T>> from, Supplier<NElement> name) {
        return NIteratorsImpl.ofSupplier(from, name);
    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilderOfArrayValues(T[] t, NElement n) {
        return NIteratorsImpl.ofArrayValues(t, n);
    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilderOfArrayValues(T[] t, String n) {
        return NIteratorsImpl.ofArrayValues(t, n);
    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilderOfArrayValues(T[] t, Supplier<NElement> n) {
        return NIteratorsImpl.ofArrayValues(t, n);
    }

    @Override
    public <T> NIteratorBuilder<T> iteratorEmptyBuilder() {
        return NIteratorsImpl.emptyBuilder();
    }

    @Override
    public <T> NIteratorBuilder<T> iteratorBuilderOfFlatMap(NIterator<? extends Collection<T>> from) {
        return NIteratorsImpl.ofFlatMap(from);
    }

}
