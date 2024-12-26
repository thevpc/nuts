package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectTypeMapper;
import net.thevpc.nuts.util.NOptional;

import java.lang.reflect.Type;
import java.time.temporal.Temporal;
import java.util.*;

public class TypeMapperRepositoryDef {
    private final Map<Key, NReflectTypeMapper> definitions = new HashMap<>();

    private final Map<Key, CacheItem> cache = new HashMap<>();

    private final TypeMapperRepositoryDef parent;

    public TypeMapperRepositoryDef(TypeMapperRepositoryDef parent) {
        this.parent = parent;
    }

    public static class Key {
        Class from;
        Type to;

        public Key(Class from, Type to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(from, key.from) && Objects.equals(to, key.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to);
        }
    }

    public static class CacheItem {
        private final Key key;
        private final Key base;
        private final NReflectTypeMapper mapper;
        private final int far;

        public CacheItem(Key key, Key base, NReflectTypeMapper mapper, int far) {
            this.key = key;
            this.base = base;
            this.mapper = mapper;
            this.far = far;
        }

        public Key getKey() {
            return key;
        }

        public Key getBase() {
            return base;
        }

        public NReflectTypeMapper getMapper() {
            return mapper;
        }

        public int getFar() {
            return far;
        }
    }

    public NOptional<NReflectTypeMapper> findTypeMapper(Class from, Type to) {
        CacheItem cacheItem = findCacheItem(from, to);
        if (cacheItem.getMapper() == null) {
            return NOptional.ofNamedEmpty("mapper from " + from.getSimpleName() + " to " + to);
        }
        return NOptional.of(cacheItem.mapper);
    }

    private CacheItem findCacheItem(Class from, Type to) {
        Key k = new Key(from, to);
        CacheItem o = cache.get(k);
        if (o != null) {
            return o;
        }
        NReflectTypeMapper m = definitions.get(k);
        if (m != null) {
            o = new CacheItem(k, k, m, 0);
            cache.put(k, o);
            return o;
        }

        List<Class> fromList = new ArrayList<>();
        Class superclass = from.getSuperclass();
        fromList.add(from);
        fromList.add(superclass);
        fromList.addAll(Arrays.asList(from.getInterfaces()));
        fromList.removeIf(Objects::isNull);

        List<Type> toList = new ArrayList<>();
        Type superType = TypeHelper.getGenericSuperclass(to);
        toList.add(superType);
        toList.addAll(Arrays.asList(TypeHelper.getGenericInterfaces(to)));
        toList.removeIf(Objects::isNull);
        CacheItem found = null;

        for (Class a : fromList) {
            for (Type b : toList) {
                Key k2 = new Key(from, to);
                //skip current!!
                if (!k2.equals(k)) {
                    CacheItem cacheItem = findCacheItem(from, superclass);
                    if (cacheItem.mapper != null) {
                        int farExtent =
                                (a.equals(from) ? 1 : 0)
                                        + (b.equals(to) ? 1 : 0);
                        int nextFar = cacheItem.far + farExtent;
                        if (found == null || found.far > nextFar) {
                            found = new CacheItem(k, cacheItem.base, cacheItem.mapper, nextFar);
                        }
                    }
                }
            }
        }
        if (found == null) {
            NReflectTypeMapper typeMapper = resolveDefaultMapper(from, to);
            if (typeMapper != null) {
                found = new CacheItem(k, k, typeMapper, 0);
            }
        }

        if (found == null) {
            if (parent != null) {
                found = parent.findCacheItem(from, to);
                if (found.getMapper() == null) {
                    found = null;
                }
            }
        }
        if (found == null) {
            found = new CacheItem(k, k, null, -1);
        }
        cache.put(k, found);
        return found;
    }

    public void dispose() {

    }

    public void invalidateCache() {
        cache.clear();
    }

    public void tryRegister(Class<?> from, Type to, NReflectTypeMapper mapper) {
        register(from, to, mapper);
    }

    public void register(Class<?> from, Type to, NReflectTypeMapper mapper) {
        invalidateCache();
        definitions.put(new Key(from, to), mapper);
    }

    private static boolean isImmutableType(Type type) {
        if (TypeHelper.isBoxedOrPrimitive(type)) {
            return true;
        }
        if (String.class.equals(type)) {
            return true;
        }
        if (TypeHelper.isAssignableFrom(Temporal.class, type)) {
            return true;
        }
        if (TypeHelper.isAssignableFrom(Number.class, type)) {
            return true;
        }
        if (TypeHelper.isEnum(type)) {
            return true;
        }
        //Date is not immutable is it?
        return TypeHelper.isAssignableFrom(Date.class, type);
    }

    private NReflectTypeMapper resolveDefaultMapper(Class from, Type to) {
        if (from.isArray()) {
            if (TypeHelper.isArray(to)) {
                return new ArrayToArrayTypeMapper(to);
            } else if (TypeHelper.isAssignableFrom(Collection.class, to)) {
                return new ArrayToCollectionTypeMapper(to);
            }
        } else if (Collection.class.isAssignableFrom(from)) {
            if (TypeHelper.isArray(to)) {
                return new CollectionToArrayTypeMapper(to);
            } else if (TypeHelper.isAssignableFrom(Collection.class, to)) {
                return new CollectionToCollectionTypeMapper(to);
            }
        }
        if (
                TypeHelper.isAssignableFrom(Map.class, from)
                        && TypeHelper.isAssignableFrom(Map.class, to)) {
            return new MapToMapTypeMapper(to);
        }

        if (TypeHelper.isBoxedOrPrimitive(from) &&
                Objects.equals(TypeHelper.toPrimitiveName(from), TypeHelper.toPrimitiveName(to))) {
            return IdentityTypeMapper.IDENTITY_TYPE_MAPPER;
        }
        if (isImmutableType(from) &&
                Objects.equals(TypeHelper.toPrimitiveName(from), TypeHelper.toPrimitiveName(to))) {
            return IdentityTypeMapper.IDENTITY_TYPE_MAPPER;
        }
        if (!TypeHelper.isBoxedOrPrimitive(from) && !TypeHelper.isBoxedOrPrimitive(to)) {
            return new DataObjectTypeMapper(from, to);
        }
        return null;
    }
}
