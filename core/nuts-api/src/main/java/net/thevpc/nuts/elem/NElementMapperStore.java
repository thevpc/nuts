package net.thevpc.nuts.elem;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

/**
 * NElementMapperStore interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementMapperStore {
    /**
     * Returns the serializer.
     *
     * @param type type
     * @param defaultOnly default only
     * @return get serializer result
     */
    <T> NElementSerializer<T> getSerializer(Type type, boolean defaultOnly);

    /**
     * Sets the deserializer.
     *
     * @param cls cls
     * @param instance instance
     * @return set deserializer result
     */
    NElementMapperStore setDeserializer(Type cls, NElementDeserializer instance);

    /**
     * Sets the deserializer.
     *
     * @param resolver resolver
     * @param key key
     * @param type type
     * @param instance instance
     * @return set deserializer result
     */
    <K, T> NElementMapperStore setDeserializer(NElementKeyResolver<K> resolver, K key, Type type, NElementDeserializer<T> instance);

    /**
     * Sets the deserializer.
     *
     * @param elementType element type
     * @param type type
     * @param instance instance
     * @return set deserializer result
     */
    <T> NElementMapperStore setDeserializer(NElementType elementType, Type type, NElementDeserializer<T> instance);

    /**
     * Sets the deserializer.
     *
     * @param elementType element type
     * @param name name
     * @param nameSelectorStrategy name selector strategy
     * @param type type
     * @param instance instance
     * @return set deserializer result
     */
    <T> NElementMapperStore setDeserializer(NElementType elementType, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDeserializer<T> instance);

    /**
     * Sets the deserializer.
     *
     * @param elementType element type
     * @param name name
     * @param type type
     * @param instance instance
     * @return set deserializer result
     */
    <T> NElementMapperStore setDeserializer(NElementType elementType, String name, Type type, NElementDeserializer<T> instance);

    /**
     * Sets the deserializer.
     *
     * @param elementTypes element types
     * @param type type
     * @param instance instance
     * @return set deserializer result
     */
    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, Type type, NElementDeserializer<T> instance);

    /**
     * Sets the deserializer.
     *
     * @param elementTypes element types
     * @param name name
     * @param nameSelectorStrategy name selector strategy
     * @param type type
     * @param instance instance
     * @return set deserializer result
     */
    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDeserializer<T> instance);

    /**
     * Sets the deserializer.
     *
     * @param elementTypes element types
     * @param name name
     * @param type type
     * @param instance instance
     * @return set deserializer result
     */
    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String name, Type type, NElementDeserializer<T> instance);

    /**
     * Sets the deserializer.
     *
     * @param elementTypes element types
     * @param names names
     * @param nameSelectorStrategy name selector strategy
     * @param type type
     * @param instance instance
     * @return set deserializer result
     */
    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String[] names, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDeserializer<T> instance);

    /**
     * Sets the deserializer.
     *
     * @param elementTypes element types
     * @param names names
     * @param type type
     * @param instance instance
     * @return set deserializer result
     */
    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String[] names, Type type, NElementDeserializer<T> instance);

    /**
     * Returns the deserializer.
     *
     * @param element element
     * @return get deserializer result
     */
    <T> NElementDeserializer<T> getDeserializer(NElement element);

    /**
     * Returns the deserializer.
     *
     * @param type type
     * @return get deserializer result
     */
    <T> NElementDeserializer<T> getDeserializer(Type type);

    /**
     * Sets the serializer.
     *
     * @param cls cls
     * @param instance instance
     * @return set serializer result
     */
    NElementMapperStore setSerializer(Type cls, NElementSerializer instance);

    /**
     * Returns the serializer.
     *
     * @param type type
     * @return get serializer result
     */
    <T> NElementSerializer<T> getSerializer(Type type);

    /**
     * Returns the deserializer.
     *
     * @param type type
     * @param defaultOnly default only
     * @return get deserializer result
     */
    <T> NElementDeserializer<T> getDeserializer(Type type, boolean defaultOnly);

    /**
     * Returns the deserializer.
     *
     * @param element element
     * @param defaultOnly default only
     * @return get deserializer result
     */
    <T> NElementDeserializer<T> getDeserializer(NElement element, boolean defaultOnly);

    /**
     * Sets the simplifier.
     *
     * @param cls cls
     * @param instance instance
     * @return set simplifier result
     */
    NElementMapperStore setSimplifier(Type cls, NElementSimplifier instance);

    /**
     * Returns the simplifier.
     *
     * @param type type
     * @return get simplifier result
     */
    <T> NElementSimplifier<T> getSimplifier(Type type);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NElementMapperStore copyFrom(NElementMapperStore other);

    /**
     * Returns the simplifier.
     *
     * @param type type
     * @param defaultOnly default only
     * @return get simplifier result
     */
    <T> NElementSimplifier<T> getSimplifier(Type type, boolean defaultOnly);

    interface NElementKeyResolver<T> {
        /**
         * Key of.
         *
         * @param e e
         * @return key of result
         */
        T keyOf(NElement e);
    }

    /**
     * Deserializer builder of.
     *
     * @param type type
     * @return deserializer builder of result
     */
    <T> NElementDeserializerBuilder<T> deserializerBuilderOf(Type type);

    /**
     * Deserializer builder of.
     *
     * @param type type
     * @return deserializer builder of result
     */
    <T> NElementDeserializerBuilder<T> deserializerBuilderOf(Class<T> type);


    /**
     * Returns the simple types filters.
     *
     * @return get simple types filters result
     */
    List<Predicate<Type>> getSimpleTypesFilters();

    /**
     * Adds the specified simple types filter.
     *
     * @param destructTypeFilter destruct type filter
     * @return add simple types filter result
     */
    NElementMapperStore addSimpleTypesFilter(Predicate<Type> destructTypeFilter);

    /**
     * Adds the specified simple types filter.
     *
     * @param destructTypeFilter destruct type filter
     * @return add simple types filter result
     */
    NElementMapperStore addSimpleTypesFilter(DefaultSimpleTypesFilter destructTypeFilter);

    /**
     * Removes the specified all simple types filters.
     *
     * @return remove all simple types filters result
     */
    NElementMapperStore removeAllSimpleTypesFilters();

    /**
     * Removes the specified simple types filter.
     *
     * @param destructTypeFilter destruct type filter
     * @return remove simple types filter result
     */
    NElementMapperStore removeSimpleTypesFilter(Predicate<Type> destructTypeFilter);

    /**
     * Removes the specified simple types filter.
     *
     * @param destructTypeFilter destruct type filter
     * @return remove simple types filter result
     */
    NElementMapperStore removeSimpleTypesFilter(DefaultSimpleTypesFilter destructTypeFilter);

    enum DefaultSimpleTypesFilter {
        ALL,
        PRIMITIVES,
    }
}
