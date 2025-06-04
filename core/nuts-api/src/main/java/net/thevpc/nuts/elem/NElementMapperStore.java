package net.thevpc.nuts.elem;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public interface NElementMapperStore {
    <T> NElementMapperStore setMapper(Type cls, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType elementType, Type type, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType elementType, String name, NElementMappedNameStrategy mappedName, Type type, NElementMapper<T> instance);

    <T> NElementMapperStore setMapper(NElementType elementType, String name, Type type, NElementMapper<T> instance);

    <K, T> NElementMapperStore setMapper(NElementKeyResolver<K> resolver, K key, Type type, NElementMapper<T> instance);

    interface NElementKeyResolver<T> {
        T keyOf(NElement e);
    }

    public static enum NElementMappedNameStrategy {
        IGNORE,
        CASE_INSENSITIVE,
        FORMAT_INSENSITIVE,
    }


    public Predicate<Class<?>> getIndestructibleObjects();
    public NElementMapperStore setIndestructibleFormat();
    public NElementMapperStore setIndestructibleObjects(Predicate<Class<?>> destructTypeFilter) ;
}
