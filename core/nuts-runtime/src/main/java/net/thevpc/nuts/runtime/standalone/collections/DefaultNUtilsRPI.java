package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.*;
import net.thevpc.nuts.concurrent.NRunnable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.io.NDataSerializer;
import net.thevpc.nuts.io.NPageStore;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.pipeline.*;
import net.thevpc.nuts.reflect.*;
import net.thevpc.nuts.runtime.standalone.util.stream.NStreamBase;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NMsgCustomFormatter;
import net.thevpc.nuts.text.NMsgType;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@NComponentScope(NScopeType.WORKSPACE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNUtilsRPI implements NUtilsRPI {
    private static final Set<NMsgType> ACCEPTED_FORMATS = new HashSet<>(Arrays.asList(
            NMsgType.CFORMAT,
            NMsgType.JFORMAT,
            NMsgType.VFORMAT,
            NMsgType.MFORMAT,
            NMsgType.SFORMAT
    ));

    public DefaultNUtilsRPI() {
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
    public <K extends Comparable<K>, V> NBPlusTree<K, V> btreePlus(int order, boolean allowDuplicates) {
        return new NBPlusTreeImpl<>(new NBPlusTreeStoreMem<K, V>(order, allowDuplicates));
    }

    public <K extends Comparable<K>, V> NBPlusTree<K, V> btreePlus(int order) {
        return new NBPlusTreeImpl<>(new NBPlusTreeStoreMem<K, V>(order, false));
    }

    @Override
    public <K extends Comparable<K>, V> NBPlusTree<K, V> btreePlus(NPageStore store, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer) {
        try {
            return new NBPlusTreeImpl<>(new NBPlusTreeStoreFixedDisk<>(store, order, allowDuplicates, keySerializer, valSerializer));
        } catch (IOException e) {
            throw new net.thevpc.nuts.io.NIOException(e);
        }
    }

    @Override
    public NPageStore createInMemoryPageStore(int pageSize) {
        return new NPageStoreMem(pageSize);
    }

    @Override
    public NPageStore createFilePageStore(NPath path, int pageSize) {
        try {
            return new NPageStoreFile(new java.io.File(path.toString()), pageSize);
        } catch (IOException e) {
            throw new net.thevpc.nuts.io.NIOException(e);
        }
    }

    @Override
    public <K, V> NClassMap<K, V> classMap(Class<V> valueType) {
        return new NClassMapImpl<>(valueType);
    }

    @Override
    public <K, V> NClassMap<K, V> classMap(Class<K> keyType, Class<V> valueType) {
        return new NClassMapImpl<>(keyType, valueType);
    }

    @Override
    public <A, B, V> NClassPairMap<A, B, V> classPairMap(Class<A> baseKey1Type, Class<B> baseKey2Type, Class<V> valueType, boolean symmetric) {
        return new NClassPairMapImpl<>(baseKey1Type, baseKey2Type, valueType, symmetric);
    }

    @Override
    public <A, B, V> NClassPairMultiMap<A, B, V> classPairMultiMap(Class<A> baseKey1Type, Class<B> baseKey2Type, Class<V> valueType, boolean symmetric) {
        return new NClassPairMultiMapImpl<>(baseKey1Type, baseKey2Type, valueType, symmetric);
    }

    @Override
    public <K, V> NClassMap<K, V> classMap(Class<K> keyType, Class<V> valueType, int initialCapacity) {
        return new NClassMapImpl<>(keyType, valueType, initialCapacity);
    }

    @Override
    public NClassMap<Object, Class> classClassMap() {
        return new NClassClassMap();
    }

    @Override
    public <T> NNormalizedStringMap<T> createInsensitiveMap() {
        return NNormalizedStringMapImpl.ofCaseInsensitive();
    }

    @Override
    public <T> NNormalizedStringMap<T> createFormatInsensitiveMap() {
        return NNormalizedStringMapImpl.ofFormatInsensitive();
    }

    @Override
    public <T> NNormalizedStringMap<T> createNormalizedMap(Function<String, String> normalizer) {
        return new NNormalizedStringMapImpl<>(normalizer);
    }

    @Override
    public <K, V> NLRUMap<K, V> createLruMap(int size) {
        return new NLRUMapImpl<>(size);
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

    @Override
    public NIterator<NIntTuple2> int2Iterator(int a, int b) {
        return NIterator.of(new NIntUplet2Iterator(a, b));
    }

    @Override
    public <K, V> NClassMultiMap<K, V> createClassMultiMap(Class<K> key1Type, Class<V> valueType) {
        return new NClassMultiMapImpl<>(key1Type, valueType);
    }

    @Override
    public <T> NClassDecisionFilter<T> createClassDecisionFilter(Class<T> type, NDecision defaultDecision) {
        return new NClassDecisionFilterImpl<>(type, defaultDecision);
    }

    @Override
    public <T> NDecisionFilter<T> createDecisionFilter(Class<T> type, NDecisionConflict decisionConflict, NDecision defaultDecision) {
        return new NDecisionFilterImpl<>(type, decisionConflict, defaultDecision);
    }

    @Override
    public <K, V> NListMultiValueMap<K, V> createListMultiValueMap() {
        return new NListMultiValueMapImpl<>();
    }

    @Override
    public <K, V> NListMultiValueMap<K, V> createListMultiValueMap(Map<K, List<V>> map) {
        return new NListMultiValueMapImpl<>(map);
    }

    @Override
    public NEvictingCharQueue createEvictingCharQueue(int size) {
        return new NEvictingCharQueueImpl(size);
    }

    @Override
    public NEvictingByteQueue createEvictingByteQueue(int size) {
        return new NEvictingByteQueueImpl(size);
    }

    @Override
    public NEvictingIntQueue createEvictingIntQueue(int size) {
        return new NEvictingIntQueueImpl(size);
    }

    @Override
    public <T> NEvictingQueue<T> createEvictingQueue(int size) {
        return new NEvictingQueueImpl<>(size);
    }

    @Override
    public <K, V> NIndexedMap<K, V> createIndexedMap() {
        return new NIndexedMapImpl<>();
    }

    @Override
    public <K, V> NSetMultiValueMap<K, V> createSetMultiValueMap() {
        return new NSetMultiValueMapImpl<>();
    }


    @Override
    public <K, V> NSetMultiValueMap<K, V> createSetMultiValueMap(Map<K, Set<V>> map) {
        return new NSetMultiValueMapImpl<>(map);
    }

    @Override
    public List<String> extractMessageParams(String message, NMsgType format, String customMessageTypeId) {
        if (format == null || message == null) return Collections.emptyList();
        try {
            Set<String> paramSet = new HashSet<>();
            List<String> params = new ArrayList<>();
            switch (format) {
                case CFORMAT: {
                    StringReader r = new StringReader(message);
                    while (true) {
                        int i = r.read();
                        if (i < 0) {
                            break;
                        }
                        char c = (char) i;
                        if (c == '%') {
                            i = r.read();
                            if (i >= 0) {
                                char c2 = (char) i;
                                if (c2 == 'n') {
                                    //ignore
                                } else {
                                    params.add("");
                                }
                            }
                        }
                    }
                    break;
                }
                case JFORMAT: {
                    StringReader r = new StringReader(message);
                    int maxElems = 0;
                    int currentIndex = -1;
                    while (true) {
                        int i = r.read();
                        if (i < 0) {
                            break;
                        }
                        char c = (char) i;
                        if (c == '{') {
                            StringBuilder n = new StringBuilder();
                            while (true) {
                                i = r.read();
                                if (i < 0) {
                                    break;
                                }
                                c = (char) i;
                                if (c == '\\') {
                                    i = r.read();
                                    if (i < 0) {
                                        break;
                                    } else {
                                        n.append(c = (char) i);
                                    }
                                } else if (c == '}') {
                                    break;
                                } else {
                                    n.append(c);
                                }
                            }
                            currentIndex++;
                            String ns = n.toString();
                            int sep = ns.indexOf(':');
                            String nsIntString = "";
                            Integer nsInt = null;
                            if (sep < 0) {
                                nsIntString = NStringUtils.strip(ns);
                            } else {
                                nsIntString = NStringUtils.strip(ns.substring(0, sep));
                            }
                            if (nsIntString.isEmpty()) {
                                nsIntString = String.valueOf(currentIndex);
                            }
                            NLiteral lit = NLiteral.of(nsIntString);
                            if (lit.asInt().isPresent()) {
                                nsInt = lit.asInt().get();
                            }
                            if (nsInt != null) {
                                while (maxElems <= nsInt) {
                                    if (paramSet.add(String.valueOf(maxElems))) {
                                        params.add(String.valueOf(maxElems));
                                    }
                                    maxElems++;
                                }
                            } else {
                                if (paramSet.add(nsIntString)) {
                                    params.add(nsIntString);
                                }
                            }
                        } else if (c == '\\') {
                            r.read();
                        } else {
                            //ignore
                        }
                    }
                    break;
                }
                case VFORMAT: {
                    NStringUtils.parseDollarPlaceHolder(message).forEach(s -> {
                        if (s.ttype == NToken.TT_DOLLAR || s.ttype == NToken.TT_DOLLAR_BRACE) {
                            String ns = s.sval;
                            if (paramSet.add(ns)) {
                                params.add(ns);
                            }
                        }
                    });
                    break;
                }
                case MFORMAT: {
                    NStringUtils.parseMoustachePlaceHolder(message).forEach(s -> {
                        if (s.ttype == NToken.TT_MOUSTACHE_START) {
                            String ns = s.sval;
                            if (paramSet.add(ns)) {
                                params.add(ns);
                            }
                        }
                    });
                    break;
                }
                case SFORMAT: {
                    String sMsg = message;
                    int posIndex = 0;
                    char quote = 0; // 0 = not in string literal
                    int n = sMsg.length();
                    for (int i = 0; i < n; i++) {
                        char c = sMsg.charAt(i);
                        if (quote != 0) {
                            if (c == quote) {
                                // handle doubled-quote escape: 'it''s'
                                if (i + 1 < n && sMsg.charAt(i + 1) == quote) {
                                } else {
                                    quote = 0;
                                }
                            }
                            continue;
                        }

                        if (c == '\'' || c == '"') {
                            quote = c;
                            continue;
                        }

                        if (c == '\\' && i + 1 < n && (sMsg.charAt(i + 1) == '?' || sMsg.charAt(i + 1) == ':')) {
                            continue;
                        }

                        if (c == ':' && i + 1 < n && sMsg.charAt(i + 1) == ':') {
                            i++;
                            continue;
                        }

                        if (c == '?') {
                            // guard against jsonb ?, ?|, ?& if you care to special-case; otherwise treat as positional
                            params.add(String.valueOf(posIndex));
                            posIndex++;
                            continue;
                        }

                        if (c == ':' && i + 1 < n && (Character.isLetter(sMsg.charAt(i + 1)) || sMsg.charAt(i + 1) == '_')) {
                            int j = i + 1;
                            while (j < n && (Character.isLetterOrDigit(sMsg.charAt(j)) || sMsg.charAt(j) == '_')) {
                                j++;
                            }
                            String name = sMsg.substring(i + 1, j);
                            params.add(name);
                            i = j - 1;
                            continue;
                        }
                    }
                    break;
                }
                case CUSTOM: {
                    if (customMessageTypeId != null) {
                        NMsgCustomFormatter ff = NWorkspaceExt.of().getModel().textModel.customFormatters.get(customMessageTypeId);
                        if (ff == null) {
                            throw new NIllegalArgumentException(NMsg.ofC("missing customer NMsg formatter %s", customMessageTypeId));
                        }
                        List<String> extractParams = ff.extractParams(message);
                        if (extractParams != null) {
                            params.addAll(extractParams);
                        }
                    }
                    break;
                }
                default: {
                    throw new IllegalArgumentException("invalid format. only " + ACCEPTED_FORMATS + " are allowed");
                }
            }
            return params;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NByteList createByteList(int initialSize) {
        return new NByteArrayList(initialSize);
    }

    @Override
    public NByteList createByteList() {
        return new NByteArrayList();
    }

    @Override
    public NByteList createByteList(byte[] values, int offset, int size) {
        return new NByteArrayList(values, offset, size);
    }

    @Override
    public NIntList createIntList(int initialSize) {
        return new NIntArrayList(initialSize);
    }

    @Override
    public NIntList createIntList() {
        return new NIntArrayList();
    }

    @Override
    public NIntList createIntList(int[] values, int offset, int size) {
        return new NIntArrayList(values, offset, size);
    }

    @Override
    public NLongList createLongList(int initialSize) {
        return new NLongArrayList(initialSize);
    }

    @Override
    public NLongList createLongList() {
        return new NLongArrayList();
    }

    @Override
    public NLongList createLongList(long[] values, int offset, int size) {
        return new NLongArrayList(values, offset, size);
    }

    @Override
    public NDoubleList createDoubleList(int initialSize) {
        return new NDoubleArrayList(initialSize);
    }

    @Override
    public NDoubleList createDoubleList() {
        return new NDoubleArrayList();
    }

    @Override
    public NDoubleList createDoubleList(double[] values, int offset, int size) {
        return new NDoubleArrayList(values, offset, size);
    }

    @Override
    public <K, V> NObservableMap<K, V> createObservableMap() {
        return new NDefaultObservableMap<>();
    }

    @Override
    public <K, V> NObservableMap<K, V> createObservableMap(Map<K, V> base) {
        return new NDefaultObservableMap<>(base);
    }

    @Override
    public <K> NObservableSet<K> createObservableSet() {
        return new NDefaultObservableSet<>();
    }

    @Override
    public <K> NObservableSet<K> createObservableSet(Set<K> base) {
        return new NDefaultObservableSet<>(base);
    }

    @Override
    public <K> NObservableList<K> createObservableList() {
        return new NDefaultObservableList<>();
    }

    @Override
    public <K> NObservableList<K> createObservableList(List<K> base) {
        return new NDefaultObservableList<>(base);
    }

    @Override
    public <V, K> NOptionalMap<K, V> createOptionalMap() {
        return new DefaultNOptionalMap<>();
    }

    @Override
    public <V, K> NOptionalMap<K, V> createOptionalMap(Map<K, V> base) {
        return new DefaultNOptionalMap<>(base);
    }

    @Override
    public NCharQueue createCharQueue() {
        return new DefaultNCharQueue();
    }

    @Override
    public NCharQueue createCharQueue(int size) {
        return new DefaultNCharQueue(size <= 0 ? 256 : size);
    }

    @Override
    public NCharQueue createCharQueue(int size, int increment) {
        return new DefaultNCharQueue(size <= 0 ? 256 : size, increment <= 0 ? 1 : increment);
    }

    @Override
    public DefaultNCharQueue createCharQueue(char[] content) {
        return new DefaultNCharQueue(content, -1, -1);
    }

    @Override
    public NByteQueue createByteQueue() {
        return new DefaultNByteQueue();
    }

    @Override
    public NByteQueue createByteQueue(int size) {
        return new DefaultNByteQueue(size <= 0 ? 256 : size);
    }

    @Override
    public NByteQueue createByteQueue(int size, int increment) {
        return new DefaultNByteQueue(size <= 0 ? 256 : size, increment);
    }

    @Override
    public NByteQueue createByteQueue(byte[] content) {
        return new DefaultNByteQueue(content, -1, -1);
    }
}
