package net.thevpc.nuts.elem;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public interface NElementMapperStore {
    <T> NElementMapperStore setMapper(Type cls, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType elementType, Type type, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType elementType, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType elementType, String name, Type type, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType[] elementType, Type type, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType[] elementType, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType[] elementType, String name, Type type, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType[] elementType, String[] name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType[] elementType, String[] name, Type type, NElementMapper<T> instance);

    <K, T> NElementMapperStore setMapper(NElementKeyResolver<K> resolver, K key, Type type, NElementMapper<T> instance);

    <T> NElementMapper<T> getMapper(NElement element, boolean defaultOnly);

    <T> NElementMapper<T> getMapper(Type type, boolean defaultOnly);

    <T> NElementMapper<T> getMapper(NElement element);

    <T> NElementMapper<T> getMapper(Type type);

    NElementMapperStore copyFrom(NElementMapperStore other);

    interface NElementKeyResolver<T> {
        T keyOf(NElement e);
    }

    <T> NElementMapperBuilder<T> builderOf(Type type);

    <T> NElementMapperBuilder<T> builderOf(Class<T> type);


    public Predicate<Class<?>> getIndestructibleObjects();

    public NElementMapperStore setDefaultIndestructibleFilter();

    public NElementMapperStore setIndestructibleFilter(Predicate<Class<?>> destructTypeFilter);
}
