package net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.format.elem.CoreNElementUtils;
import net.thevpc.nuts.runtime.standalone.format.elem.mapper.builder.DefaultNElementMapperBuilder;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NClassMap;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NNameFormat;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

public class UserElementMapperStore implements NElementMapperStore {
    public static final NElementTypeNElementKeyResolver NELEMENTTYPE_KEY_RESOLVER = new NElementTypeNElementKeyResolver();
    public static final NElementTypeAndNameNElementKeyResolver CASE_SENSITIVE_NAME_RESOLVER = new NElementTypeAndNameNElementKeyResolver();
    public static final NElementTypeAndNameNoCaseNElementKeyResolver CASE_INSENSITIVE_NAME_RESOLVER = new NElementTypeAndNameNoCaseNElementKeyResolver();
    public static final NElementTypeAndNameNoFormatNElementKeyResolver FORMAT_INSENSITIVE_NAME_RESOLVER = new NElementTypeAndNameNoFormatNElementKeyResolver();
    private DefaultElementMapperStore defaultElementMapperStore;
    private final NClassMap<NElementMapper> lvl1_customMappersByType = new NClassMap<>(null, NElementMapper.class);
    private final List<NElementKeyResolverEntry> lvl2_customMappersByKey = new ArrayList<>();
    private Predicate<Type> indestructibleObjects = CoreNElementUtils.DEFAULT_INDESTRUCTIBLE;
    private NReflectRepository reflectRepository;

    static class NElementKeyResolverEntry<T> {
        NElementKeyResolver<T> resolver;
        Map<T, NElementMapper> byKey = new HashMap<>();
        Type type;

        public NElementKeyResolverEntry(NElementKeyResolver<T> resolver) {
            this.resolver = resolver;
        }
    }

    public UserElementMapperStore() {
        this.defaultElementMapperStore = NWorkspaceExt.of().getModel().defaultElementMapperStore;
    }

    public NReflectRepository getReflectRepository() {
        return reflectRepository;
    }

    public UserElementMapperStore setReflectRepository(NReflectRepository reflectRepository) {
        this.reflectRepository = reflectRepository;
        return this;
    }

    @Override
    public NElementMapperStore copyFrom(NElementMapperStore other) {
        if (other != null) {
            if (other instanceof UserElementMapperStore) {
                UserElementMapperStore u = (UserElementMapperStore) other;
                for (Class c : u.lvl1_customMappersByType.keySet()) {
                    setMapper(c, u.lvl1_customMappersByType.get(c));
                }
                for (NElementKeyResolverEntry e : u.lvl2_customMappersByKey) {
                    for (Object eeo : e.byKey.entrySet()) {
                        Map.Entry ee = (Map.Entry) eeo;
                        NElementKeyResolver<Object> resolver = e.resolver;
                        Object key = ee.getKey();
                        NElementMapper value = (NElementMapper) ee.getValue();
                        setMapper(resolver, key, (Type) e.type, value);
                    }
                }
            }
            this.indestructibleObjects = other.getIndestructibleObjects();
        }
        return this;
    }

    @Override
    public <T> NElementMapperBuilder<T> builderOf(Type type) {
        return new DefaultNElementMapperBuilder<>(reflectRepository, type);
    }

    @Override
    public <T> NElementMapperBuilder<T> builderOf(Class<T> type) {
        return new DefaultNElementMapperBuilder<>(reflectRepository, type);
    }

    public Predicate<Type> getIndestructibleObjects() {
        return indestructibleObjects;
    }

    @Override
    public UserElementMapperStore setDefaultIndestructibleFilter() {
        return setIndestructibleFilter(CoreNElementUtils.DEFAULT_INDESTRUCTIBLE);
    }

    public UserElementMapperStore setIndestructibleFilter(Predicate<Type> destructTypeFilter) {
        this.indestructibleObjects = destructTypeFilter;
        return this;
    }


    public final UserElementMapperStore setMapper(Type cls, NElementMapper instance) {
        if (instance == null) {
            lvl1_customMappersByType.remove((Class) cls);
        } else {
            lvl1_customMappersByType.put((Class) cls, instance);
        }
        return this;
    }

    public final <K, T> NElementMapperStore setMapper(NElementKeyResolver<K> resolver, K key, Type type, NElementMapper<T> instance) {
        NElementKeyResolverEntry ok = null;
        for (NElementKeyResolverEntry e : lvl2_customMappersByKey) {
            if (e.resolver.equals(resolver)) {
                ok = e;
                break;
            }
        }
        if (ok == null) {
            ok = new NElementKeyResolverEntry<>(resolver);
            ok.type = type;
            lvl2_customMappersByKey.add(ok);
        }
        if (type instanceof Class) {
            lvl1_customMappersByType.put((Class) type, instance);
        }
        ok.byKey.put(key, instance);
        return this;
    }

    public <T> NElementMapperStore setMapper(NElementType elementType, Type type, NElementMapper<T> instance) {
        return setMapper(NELEMENTTYPE_KEY_RESOLVER, elementType, type, instance);
    }

    public final <T> NElementMapperStore setMapper(NElementType elementType, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementMapper<T> instance) {
        if (nameSelectorStrategy == null) {
            nameSelectorStrategy = NNameSelectorStrategy.CASE_SENSITIVE;
        }
        NElementKeyResolver<NElementTypeAndName> resolver = CASE_SENSITIVE_NAME_RESOLVER;
        switch (nameSelectorStrategy) {
            case CASE_INSENSITIVE: {
                resolver = CASE_INSENSITIVE_NAME_RESOLVER;
                break;
            }
            case FORMAT_INSENSITIVE: {
                resolver = FORMAT_INSENSITIVE_NAME_RESOLVER;
                break;
            }
        }
        return setMapper(resolver, new NElementTypeAndName(elementType, name), type, instance);
    }

    public final <T> NElementMapperStore setMapper(NElementType elementType, String name, Type type, NElementMapper<T> instance) {
        return setMapper(CASE_SENSITIVE_NAME_RESOLVER, new NElementTypeAndName(elementType, name), type, instance);
    }

    @Override
    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, Type type, NElementMapper<T> instance) {
        for (NElementType elementType : elementTypes) {
            setMapper(elementType, type, instance);
        }
        return this;
    }

    @Override
    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementMapper<T> instance) {
        for (NElementType elementType : elementTypes) {
            setMapper(elementType, name, nameSelectorStrategy, type, instance);
        }
        return this;
    }

    @Override
    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, String name, Type type, NElementMapper<T> instance) {
        for (NElementType elementType : elementTypes) {
            setMapper(elementType, name, type, instance);
        }
        return this;
    }

    @Override
    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, String[] names, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementMapper<T> instance) {
        for (NElementType elementType : elementTypes) {
            for (String name : names) {
                setMapper(elementType, name, nameSelectorStrategy, type, instance);
            }
        }
        return this;
    }

    @Override
    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, String[] names, Type type, NElementMapper<T> instance) {
        for (NElementType elementType : elementTypes) {
            for (String name : names) {
                setMapper(elementType, name, type, instance);
            }
        }
        return this;
    }

    @Override
    public <T> NElementMapper<T> getMapper(NElement element) {
        return getMapper(element, false);
    }

    @Override
    public <T> NElementMapper<T> getMapper(Type type) {
        return getMapper(type, false);
    }

    public <T> NElementMapper<T> getMapper(Type type, boolean defaultOnly) {
        if (type == null) {
            return DefaultElementMapperStore.F_NULL;
        }
        Class cls = ReflectUtils.getRawClass(type);
        if (NSession.class.isAssignableFrom(cls)) {
            throw new NIllegalArgumentException(NMsg.ofC(
                    "%s is not serializable", type
            ));
        }
        if (cls.isArray()) {
            NElementMapper f = defaultElementMapperStore.getCoreMappers().getExact(cls);
            if (f != null) {
                return f;
            }
            return DefaultElementMapperStore.F_NUTS_ARR;
        }
        if (!defaultOnly) {
            NElementMapper f = lvl1_customMappersByType.get(cls);
            if (f != null) {
                return f;
            }
            final NElementMapper r = defaultElementMapperStore.getCoreMappers().get(cls);
            if (r != null) {
                return r;
            }
        }
        final NElementMapper r = defaultElementMapperStore.getDefaultMappers().get(cls);
        if (r != null) {
            return r;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to find element mapper for type : %s", type
        ));
    }


    public <T> NElementMapper<T> getMapper(NElement element, boolean defaultOnly) {
        NAssert.requireNonNull(element, "element");
        if (!defaultOnly) {
            for (NElementKeyResolverEntry e : lvl2_customMappersByKey) {
                Object k = e.resolver.keyOf(element);
                if (k != null) {
                    NElementMapper u = (NElementMapper) e.byKey.get(k);
                    if (u != null) {
                        return u;
                    }
                }
            }
        }
        final NElementMapper r = defaultElementMapperStore.getMapper(element, this);
        if (r != null) {
            return r;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to find element mapper for element type %s. element is : %s", element.type().id(),element
        ));
    }

    private static class NElementTypeNElementKeyResolver implements NElementKeyResolver<NElementType> {
        @Override
        public NElementType keyOf(NElement e) {
            return e.type();
        }
    }


    private static class NElementTypeAndNameNElementKeyResolver implements NElementKeyResolver<NElementTypeAndName> {
        @Override
        public NElementTypeAndName keyOf(NElement e) {
            String name = null;
            if (e.isNamed()) {
                name = e.asNamed().get().name().orNull();
            }
            return new NElementTypeAndName(e.type(), name);
        }
    }

    private static class NElementTypeAndNameNoCaseNElementKeyResolver implements NElementKeyResolver<NElementTypeAndName> {
        @Override
        public NElementTypeAndName keyOf(NElement e) {
            String name = null;
            if (e.isNamed()) {
                name = e.asNamed().get().name().orNull();
            }
            if (name != null) {
                name = name.toLowerCase();
            }
            return new NElementTypeAndName(e.type(), name);
        }
    }

    private static class NElementTypeAndNameNoFormatNElementKeyResolver implements NElementKeyResolver<NElementTypeAndName> {
        @Override
        public NElementTypeAndName keyOf(NElement e) {
            String name = null;
            if (e.isNamed()) {
                name = e.asNamed().get().name().orNull();
            }
            if (name != null) {
                name = NNameFormat.CONST_NAME.format(name);
            }
            return new NElementTypeAndName(e.type(), name);
        }
    }

    private static class NElementTypeAndName {
        private NElementType type;
        private String name;

        public NElementTypeAndName(NElementType type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null || getClass() != object.getClass()) return false;
            NElementTypeAndName that = (NElementTypeAndName) object;
            return type == that.type && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
        }
    }
}
