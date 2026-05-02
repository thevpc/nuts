package net.thevpc.nuts.elem;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

public interface NElementMapperStore {
    <T> NElementSerializer<T> getSerializer(Type type, boolean defaultOnly);

    NElementMapperStore setDeserializer(Type cls, NElementDeserializer instance);

    <K, T> NElementMapperStore setDeserializer(NElementKeyResolver<K> resolver, K key, Type type, NElementDeserializer<T> instance);

    <T> NElementMapperStore setDeserializer(NElementType elementType, Type type, NElementDeserializer<T> instance);

    <T> NElementMapperStore setDeserializer(NElementType elementType, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDeserializer<T> instance);

    <T> NElementMapperStore setDeserializer(NElementType elementType, String name, Type type, NElementDeserializer<T> instance);

    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, Type type, NElementDeserializer<T> instance);

    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDeserializer<T> instance);

    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String name, Type type, NElementDeserializer<T> instance);

    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String[] names, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDeserializer<T> instance);

    <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String[] names, Type type, NElementDeserializer<T> instance);

    <T> NElementDeserializer<T> getDeserializer(NElement element);

    <T> NElementDeserializer<T> getDeserializer(Type type);

    NElementMapperStore setSerializer(Type cls, NElementSerializer instance);

    <T> NElementSerializer<T> getSerializer(Type type);

    <T> NElementDeserializer<T> getDeserializer(Type type, boolean defaultOnly);

    <T> NElementDeserializer<T> getDeserializer(NElement element, boolean defaultOnly);

    NElementMapperStore setSimplifier(Type cls, NElementSimplifier instance);

    <T> NElementSimplifier<T> getSimplifier(Type type);

    NElementMapperStore copyFrom(NElementMapperStore other);

    <T> NElementSimplifier<T> getSimplifier(Type type, boolean defaultOnly);

    interface NElementKeyResolver<T> {
        T keyOf(NElement e);
    }

    <T> NElementDeserializerBuilder<T> deserializerBuilderOf(Type type);

    <T> NElementDeserializerBuilder<T> deserializerBuilderOf(Class<T> type);


    List<Predicate<Type>> getSimpleTypesFilters();

    NElementMapperStore addSimpleTypesFilter(Predicate<Type> destructTypeFilter);

    NElementMapperStore addSimpleTypesFilter(DefaultSimpleTypesFilter destructTypeFilter);

    NElementMapperStore removeAllSimpleTypesFilters();

    NElementMapperStore removeSimpleTypesFilter(Predicate<Type> destructTypeFilter);

    NElementMapperStore removeSimpleTypesFilter(DefaultSimpleTypesFilter destructTypeFilter);

    enum DefaultSimpleTypesFilter {
        ALL,
        PRIMITIVES,
    }
}
