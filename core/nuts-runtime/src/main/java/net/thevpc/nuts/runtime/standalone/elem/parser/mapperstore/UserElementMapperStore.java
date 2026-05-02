package net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore;

import net.thevpc.nuts.runtime.standalone.elem.mapper.NElementMapper;
import net.thevpc.nuts.runtime.standalone.util.collections.NClassMapImpl;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.elem.CoreNElementUtils;
import net.thevpc.nuts.runtime.standalone.elem.mapper.builder.DefaultNElementDeserializerBuilder;
import net.thevpc.nuts.runtime.standalone.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.reflect.NClassMap;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NNameFormat;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

public class UserElementMapperStore implements NElementMapperStore {
    public static final NElementTypeNElementKeyResolver NELEMENTTYPE_KEY_RESOLVER = new NElementTypeNElementKeyResolver();
    public static final NElementTypeAndNameNElementKeyResolver CASE_SENSITIVE_NAME_RESOLVER = new NElementTypeAndNameNElementKeyResolver();
    public static final NElementTypeAndNameNoCaseNElementKeyResolver CASE_INSENSITIVE_NAME_RESOLVER = new NElementTypeAndNameNoCaseNElementKeyResolver();
    public static final NElementTypeAndNameNoFormatNElementKeyResolver FORMAT_INSENSITIVE_NAME_RESOLVER = new NElementTypeAndNameNoFormatNElementKeyResolver();
    private final DefaultElementMapperStore defaultElementMapperStore;

    private final NClassMap<Object, NElementSerializer> serializers_lvl1_customMappersByType = new NClassMapImpl<>(null, NElementSerializer.class);
    private final List<NElementKeyResolverEntry> serializers_lvl2_customMappersByKey = new ArrayList<>();

    private final NClassMap<Object, NElementDeserializer> deserializers_lvl1_customMappersByType = new NClassMapImpl<>(null, NElementDeserializer.class);
    private final List<NElementKeyResolverEntry> deserializers_lvl2_customMappersByKey = new ArrayList<>();

    private final NClassMap<Object, NElementSimplifier> destructors_lvl1_customMappersByType = new NClassMapImpl<>(null, NElementSimplifier.class);
    private final List<NElementKeyResolverEntry> destructors_lvl2_customMappersByKey = new ArrayList<>();


    private static class MapHelper {

    }

    private List<Predicate<Type>> indestructibleTypesFilters = new ArrayList<>(
            Collections.singletonList(
                    CoreNElementUtils.DEFAULT_INDESTRUCTIBLE
            )
    );//CoreNElementUtils.DEFAULT_INDESTRUCTIBLE;
    private NReflectRepository reflectRepository;

    static class NElementKeyResolverEntry<T, V> {
        NElementKeyResolver<T> resolver;
        Map<T, V> byKey = new HashMap<>();

        public NElementKeyResolverEntry(NElementKeyResolver<T> resolver) {
            this.resolver = resolver;
        }

        public NElementKeyResolverEntry<T, V> copy() {
            NElementKeyResolverEntry<T, V> newInstance = new NElementKeyResolverEntry<>(resolver);
            newInstance.byKey.putAll(byKey);
            return newInstance;
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
                for (Class c : u.serializers_lvl1_customMappersByType.keySet()) {
                    setSerializer(c, u.serializers_lvl1_customMappersByType.get(c));
                }
                for (NElementKeyResolverEntry e : u.serializers_lvl2_customMappersByKey) {
                    this.serializers_lvl2_customMappersByKey.add(e.copy());
                }
                for (Class c : u.deserializers_lvl1_customMappersByType.keySet()) {
                    setDeserializer(c, u.deserializers_lvl1_customMappersByType.get(c));
                }
                for (NElementKeyResolverEntry e : u.deserializers_lvl2_customMappersByKey) {
                    this.deserializers_lvl2_customMappersByKey.add(e.copy());
                }
                for (Class c : u.deserializers_lvl1_customMappersByType.keySet()) {
                    setSimplifier(c, u.destructors_lvl1_customMappersByType.get(c));
                }
                for (NElementKeyResolverEntry e : u.destructors_lvl2_customMappersByKey) {
                    this.destructors_lvl2_customMappersByKey.add(e.copy());
                }
            }
            this.indestructibleTypesFilters = new ArrayList<>(other.getSimpleTypesFilters());
        }
        return this;
    }

    @Override
    public <T> NElementDeserializerBuilder<T> deserializerBuilderOf(Type type) {
        return new DefaultNElementDeserializerBuilder<>(reflectRepository, type);
    }

    @Override
    public <T> NElementDeserializerBuilder<T> deserializerBuilderOf(Class<T> type) {
        return new DefaultNElementDeserializerBuilder<>(reflectRepository, type);
    }

    public List<Predicate<Type>> getSimpleTypesFilters() {
        return indestructibleTypesFilters;
    }

    @Override
    public NElementMapperStore addSimpleTypesFilter(Predicate<Type> destructTypeFilter) {
        if (destructTypeFilter != null && !indestructibleTypesFilters.contains(destructTypeFilter)) {
            indestructibleTypesFilters.add(destructTypeFilter);
        }
        return this;
    }

    @Override
    public NElementMapperStore removeSimpleTypesFilter(Predicate<Type> destructTypeFilter) {
        if (destructTypeFilter != null) {
            indestructibleTypesFilters.remove(destructTypeFilter);
        }
        return this;
    }

    @Override
    public NElementMapperStore removeAllSimpleTypesFilters() {
        this.indestructibleTypesFilters.clear();
        return this;
    }

    @Override
    public NElementMapperStore addSimpleTypesFilter(DefaultSimpleTypesFilter destructTypeFilter) {
        if (destructTypeFilter != null) {
            switch (destructTypeFilter) {
                case ALL: {
                    addSimpleTypesFilter(CoreNElementUtils.DEFAULT_INDESTRUCTIBLE);
                    break;
                }
                case PRIMITIVES: {
                    addSimpleTypesFilter(CoreNElementUtils.DEFAULT_INDESTRUCTIBLE_PRIMITIVE);
                    break;
                }
            }
        }
        return this;
    }

    @Override
    public NElementMapperStore removeSimpleTypesFilter(DefaultSimpleTypesFilter destructTypeFilter) {
        if (destructTypeFilter != null) {
            switch (destructTypeFilter) {
                case ALL: {
                    removeSimpleTypesFilter(CoreNElementUtils.DEFAULT_INDESTRUCTIBLE);
                    break;
                }
                case PRIMITIVES: {
                    removeSimpleTypesFilter(CoreNElementUtils.DEFAULT_INDESTRUCTIBLE);
                    break;
                }
            }
        }
        return this;
    }

//    public final UserElementMapperStore setMapper(Type cls, NElementMapper instance) {
//        if (instance == null) {
//            lvl1_customMappersByType.remove((Class) cls);
//        } else {
//            lvl1_customMappersByType.put((Class) cls, instance);
//        }
//        return this;
//    }
//
//    public final <K, T> NElementMapperStore setMapper(NElementKeyResolver<K> resolver, K key, Type type, NElementMapper<T> instance) {
//        NElementKeyResolverEntry ok = null;
//        for (NElementKeyResolverEntry e : lvl2_customMappersByKey) {
//            if (e.resolver.equals(resolver)) {
//                ok = e;
//                break;
//            }
//        }
//        if (ok == null) {
//            ok = new NElementKeyResolverEntry<>(resolver);
//            lvl2_customMappersByKey.add(ok);
//        }
//        if (type instanceof Class) {
//            lvl1_customMappersByType.put((Class) type, instance);
//        }
//        ok.byKey.put(key, instance);
//        return this;
//    }
//
//    public <T> NElementMapperStore setMapper(NElementType elementType, Type type, NElementMapper<T> instance) {
//        return setMapper(NELEMENTTYPE_KEY_RESOLVER, elementType, type, instance);
//    }
//
//    public final <T> NElementMapperStore setMapper(NElementType elementType, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementMapper<T> instance) {
//        if (nameSelectorStrategy == null) {
//            nameSelectorStrategy = NNameSelectorStrategy.CASE_SENSITIVE;
//        }
//        NElementKeyResolver<NElementTypeAndName> resolver = CASE_SENSITIVE_NAME_RESOLVER;
//        switch (nameSelectorStrategy) {
//            case CASE_INSENSITIVE: {
//                resolver = CASE_INSENSITIVE_NAME_RESOLVER;
//                break;
//            }
//            case FORMAT_INSENSITIVE: {
//                resolver = FORMAT_INSENSITIVE_NAME_RESOLVER;
//                break;
//            }
//        }
//        return setMapper(resolver, new NElementTypeAndName(elementType, name), type, instance);
//    }
//
//    public final <T> NElementMapperStore setMapper(NElementType elementType, String name, Type type, NElementMapper<T> instance) {
//        return setMapper(CASE_SENSITIVE_NAME_RESOLVER, new NElementTypeAndName(elementType, name), type, instance);
//    }
//
//    @Override
//    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, Type type, NElementMapper<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            setMapper(elementType, type, instance);
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementMapper<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            setMapper(elementType, name, nameSelectorStrategy, type, instance);
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, String name, Type type, NElementMapper<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            setMapper(elementType, name, type, instance);
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, String[] names, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementMapper<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            for (String name : names) {
//                setMapper(elementType, name, nameSelectorStrategy, type, instance);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setMapper(NElementType[] elementTypes, String[] names, Type type, NElementMapper<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            for (String name : names) {
//                setMapper(elementType, name, type, instance);
//            }
//        }
//        return this;
//    }
//
//
//    @Override
//    public <T> NElementMapper<T> getMapper(NElement element) {
//        return getMapper(element, false);
//    }
//
//    @Override
//    public <T> NElementMapper<T> getMapper(Type type) {
//        return getMapper(type, false);
//    }
//
//    public <T> NElementMapper<T> getMapper(Type type, boolean defaultOnly) {
//        if (type == null) {
//            return DefaultElementMapperStore.F_NULL;
//        }
//        Class cls = ReflectUtils.getRawClass(type).get();
//        if (NSession.class.isAssignableFrom(cls)) {
//            throw new NIllegalArgumentException(NMsg.ofC(
//                    "%s is not serializable", type
//            ));
//        }
//        if (cls.isArray()) {
//            NElementMapper f = defaultElementMapperStore.getCoreMappers().getExact(cls);
//            if (f != null) {
//                return f;
//            }
//            return DefaultElementMapperStore.F_NUTS_ARR;
//        }
//        if (!defaultOnly) {
//            NElementMapper f = lvl1_customMappersByType.get(cls);
//            if (f != null) {
//                return f;
//            }
//            final NElementMapper r = defaultElementMapperStore.getCoreMappers().get(cls);
//            if (r != null) {
//                return r;
//            }
//        }
//        final NElementMapper r = defaultElementMapperStore.getDefaultMappers().get(cls);
//        if (r != null) {
//            return r;
//        }
//        throw new NIllegalArgumentException(NMsg.ofC(
//                "unable to find element mapper for type : %s", type
//        ));
//    }
//
//
//    public <T> NElementMapper<T> getMapper(NElement element, boolean defaultOnly) {
//        NAssert.requireNamedNonNull(element, "element");
//        if (!defaultOnly) {
//            for (NElementKeyResolverEntry e : lvl2_customMappersByKey) {
//                Object k = e.resolver.keyOf(element);
//                if (k != null) {
//                    NElementMapper u = (NElementMapper) e.byKey.get(k);
//                    if (u != null) {
//                        return u;
//                    }
//                }
//            }
//        }
//        final NElementMapper r = defaultElementMapperStore.getMapper(element, this);
//        if (r != null) {
//            return r;
//        }
//        throw new NIllegalArgumentException(NMsg.ofC(
//                "unable to find element mapper for element type %s. element is : %s", element.type().id(), element
//        ));
//    }

    /// ////////////////////////////////////////////////////////

    @Override
    public final UserElementMapperStore setSerializer(Type cls, NElementSerializer instance) {
        if (instance == null) {
            serializers_lvl1_customMappersByType.remove((Class) cls);
        } else {
            serializers_lvl1_customMappersByType.put((Class) cls, instance);
        }
        return this;
    }

//    public final <K, T> NElementMapperStore setSerializer(NElementKeyResolver<K> resolver, K key, Type type, NElementSerializer<T> instance) {
//        NElementKeyResolverEntry ok = null;
//        for (NElementKeyResolverEntry e : serializers_lvl2_customMappersByKey) {
//            if (e.resolver.equals(resolver)) {
//                ok = e;
//                break;
//            }
//        }
//        if (ok == null) {
//            ok = new NElementKeyResolverEntry<>(resolver);
//            serializers_lvl2_customMappersByKey.add(ok);
//        }
//        if (type instanceof Class) {
//            serializers_lvl1_customMappersByType.put((Class) type, instance);
//        }
//        ok.byKey.put(key, instance);
//        return this;
//    }
//
//    public <T> NElementMapperStore setSerializer(NElementType elementType, Type type, NElementSerializer<T> instance) {
//        return setSerializer(NELEMENTTYPE_KEY_RESOLVER, elementType, type, instance);
//    }
//
//    public final <T> NElementMapperStore setSerializer(NElementType elementType, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementSerializer<T> instance) {
//        if (nameSelectorStrategy == null) {
//            nameSelectorStrategy = NNameSelectorStrategy.CASE_SENSITIVE;
//        }
//        NElementKeyResolver<NElementTypeAndName> resolver = CASE_SENSITIVE_NAME_RESOLVER;
//        switch (nameSelectorStrategy) {
//            case CASE_INSENSITIVE: {
//                resolver = CASE_INSENSITIVE_NAME_RESOLVER;
//                break;
//            }
//            case FORMAT_INSENSITIVE: {
//                resolver = FORMAT_INSENSITIVE_NAME_RESOLVER;
//                break;
//            }
//        }
//        return setSerializer(resolver, new NElementTypeAndName(elementType, name), type, instance);
//    }

//    public final <T> NElementMapperStore setSerializer(NElementType elementType, String name, Type type, NElementSerializer<T> instance) {
//        return setSerializer(CASE_SENSITIVE_NAME_RESOLVER, new NElementTypeAndName(elementType, name), type, instance);
//    }

//    @Override
//    public <T> NElementMapperStore setSerializer(NElementType[] elementTypes, Type type, NElementSerializer<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            setSerializer(elementType, type, instance);
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setSerializer(NElementType[] elementTypes, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementSerializer<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            setSerializer(elementType, name, nameSelectorStrategy, type, instance);
//        }
//        return this;
//    }

//    @Override
//    public <T> NElementMapperStore setSerializer(NElementType[] elementTypes, String name, Type type, NElementSerializer<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            setSerializer(elementType, name, type, instance);
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setSerializer(NElementType[] elementTypes, String[] names, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementSerializer<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            for (String name : names) {
//                setSerializer(elementType, name, nameSelectorStrategy, type, instance);
//            }
//        }
//        return this;
//    }

//    @Override
//    public <T> NElementMapperStore setSerializer(NElementType[] elementTypes, String[] names, Type type, NElementSerializer<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            for (String name : names) {
//                setSerializer(elementType, name, type, instance);
//            }
//        }
//        return this;
//    }


//    @Override
//    public <T> NElementSerializer<T> getSerializer(NElement element) {
//        return getSerializer(element, false);
//    }

    @Override
    public <T> NElementSerializer<T> getSerializer(Type type) {
        return getSerializer(type, false);
    }

    @Override
    public <T> NElementSerializer<T> getSerializer(Type type, boolean defaultOnly) {
        if (type == null) {
            return DefaultElementMapperStore.F_NULL;
        }
        Class cls = ReflectUtils.getRawClass(type).get();
        if (NSession.class.isAssignableFrom(cls)) {
            throw new NIllegalArgumentException(NMsg.ofC(
                    "%s is not serializable", type
            ));
        }
        if (cls.isArray()) {
            NElementSerializer f = defaultElementMapperStore.getCoreSerializers().getExact(cls);
            if (f != null) {
                return f;
            }
            return DefaultElementMapperStore.F_NUTS_ARR;
        }
        if (!defaultOnly) {
            NElementSerializer f = serializers_lvl1_customMappersByType.get(cls);
            if (f != null) {
                return f;
            }
            final NElementSerializer r = defaultElementMapperStore.getCoreSerializers().get(cls);
            if (r != null) {
                return r;
            }
        }
        final NElementSerializer r = defaultElementMapperStore.getDefaultSerializers().get(cls);
        if (r != null) {
            return r;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to find element mapper for type : %s", type
        ));
    }


//    public <T> NElementSerializer<T> getSerializer(NElement element, boolean defaultOnly) {
//        NAssert.requireNamedNonNull(element, "element");
//        if (!defaultOnly) {
//            for (NElementKeyResolverEntry e : serializers_lvl2_customMappersByKey) {
//                Object k = e.resolver.keyOf(element);
//                if (k != null) {
//                    NElementMapper u = (NElementMapper) e.byKey.get(k);
//                    if (u != null) {
//                        return u;
//                    }
//                }
//            }
//        }
//        final NElementSerializer r = defaultElementMapperStore.getSerializer(element, this);
//        if (r != null) {
//            return r;
//        }
//        throw new NIllegalArgumentException(NMsg.ofC(
//                "unable to find element serializer for element type %s. element is : %s", element.type().id(), element
//        ));
//    }
    ///////////////////////////////////////

    /// ////////////////////////////////////////////////////////

    @Override
    public final NElementMapperStore setDeserializer(Type cls, NElementDeserializer instance) {
        if (instance == null) {
            deserializers_lvl1_customMappersByType.remove((Class) cls);
        } else {
            deserializers_lvl1_customMappersByType.put((Class) cls, instance);
        }
        return this;
    }

    @Override
    public final <K, T> NElementMapperStore setDeserializer(NElementKeyResolver<K> resolver, K key, Type type, NElementDeserializer<T> instance) {
        NElementKeyResolverEntry ok = null;
        for (NElementKeyResolverEntry e : serializers_lvl2_customMappersByKey) {
            if (e.resolver.equals(resolver)) {
                ok = e;
                break;
            }
        }
        if (ok == null) {
            ok = new NElementKeyResolverEntry<>(resolver);
            deserializers_lvl2_customMappersByKey.add(ok);
        }
        if (type instanceof Class) {
            deserializers_lvl1_customMappersByType.put((Class) type, instance);
        }
        ok.byKey.put(key, instance);
        return this;
    }

    @Override
    public <T> NElementMapperStore setDeserializer(NElementType elementType, Type type, NElementDeserializer<T> instance) {
        return setDeserializer(NELEMENTTYPE_KEY_RESOLVER, elementType, type, instance);
    }

    @Override
    public final <T> NElementMapperStore setDeserializer(NElementType elementType, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDeserializer<T> instance) {
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
        return setDeserializer(resolver, new NElementTypeAndName(elementType, name), type, instance);
    }

    @Override
    public final <T> NElementMapperStore setDeserializer(NElementType elementType, String name, Type type, NElementDeserializer<T> instance) {
        return setDeserializer(CASE_SENSITIVE_NAME_RESOLVER, new NElementTypeAndName(elementType, name), type, instance);
    }

    @Override
    public <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, Type type, NElementDeserializer<T> instance) {
        for (NElementType elementType : elementTypes) {
            setDeserializer(elementType, type, instance);
        }
        return this;
    }

    @Override
    public <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDeserializer<T> instance) {
        for (NElementType elementType : elementTypes) {
            setDeserializer(elementType, name, nameSelectorStrategy, type, instance);
        }
        return this;
    }

    @Override
    public <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String name, Type type, NElementDeserializer<T> instance) {
        for (NElementType elementType : elementTypes) {
            setDeserializer(elementType, name, type, instance);
        }
        return this;
    }

    @Override
    public <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String[] names, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDeserializer<T> instance) {
        for (NElementType elementType : elementTypes) {
            for (String name : names) {
                setDeserializer(elementType, name, nameSelectorStrategy, type, instance);
            }
        }
        return this;
    }

    @Override
    public <T> NElementMapperStore setDeserializer(NElementType[] elementTypes, String[] names, Type type, NElementDeserializer<T> instance) {
        for (NElementType elementType : elementTypes) {
            for (String name : names) {
                setDeserializer(elementType, name, type, instance);
            }
        }
        return this;
    }


    @Override
    public <T> NElementDeserializer<T> getDeserializer(NElement element) {
        return getDeserializer(element, false);
    }

    @Override
    public <T> NElementDeserializer<T> getDeserializer(Type type) {
        return getDeserializer(type, false);
    }

    @Override
    public <T> NElementDeserializer<T> getDeserializer(Type type, boolean defaultOnly) {
        if (type == null) {
            return DefaultElementMapperStore.F_NULL;
        }
        Class cls = ReflectUtils.getRawClass(type).get();
        if (NSession.class.isAssignableFrom(cls)) {
            throw new NIllegalArgumentException(NMsg.ofC(
                    "%s is not serializable", type
            ));
        }
        if (cls.isArray()) {
            NElementDeserializer f = defaultElementMapperStore.getCoreDeserializers().getExact(cls);
            if (f != null) {
                return f;
            }
            return DefaultElementMapperStore.F_NUTS_ARR;
        }
        if (!defaultOnly) {
            NElementDeserializer f = deserializers_lvl1_customMappersByType.get(cls);
            if (f != null) {
                return f;
            }
            final NElementDeserializer r = defaultElementMapperStore.getCoreDeserializers().get(cls);
            if (r != null) {
                return r;
            }
        }
        final NElementDeserializer r = defaultElementMapperStore.getDefaultDeserializers().get(cls);
        if (r != null) {
            return r;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to find element mapper for type : %s", type
        ));
    }


    @Override
    public <T> NElementDeserializer<T> getDeserializer(NElement element, boolean defaultOnly) {
        NAssert.requireNamedNonNull(element, "element");
        if (!defaultOnly) {
            for (NElementKeyResolverEntry e : deserializers_lvl2_customMappersByKey) {
                Object k = e.resolver.keyOf(element);
                if (k != null) {
                    NElementMapper u = (NElementMapper) e.byKey.get(k);
                    if (u != null) {
                        return u;
                    }
                }
            }
        }
        final NElementDeserializer r = defaultElementMapperStore.getDeserializer(element, this);
        if (r != null) {
            return r;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to find element serializer for element type %s. element is : %s", element.type().id(), element
        ));
    }

    /// ////////////////////////////////////
    /// ////////////////////////////////////////////////////////

    @Override
    public final UserElementMapperStore setSimplifier(Type cls, NElementSimplifier instance) {
        if (instance == null) {
            destructors_lvl1_customMappersByType.remove((Class) cls);
        } else {
            destructors_lvl1_customMappersByType.put((Class) cls, instance);
        }
        return this;
    }

//    public final <K, T> NElementMapperStore setDestructor(NElementKeyResolver<K> resolver, K key, Type type, NElementDestructor<T> instance) {
//        NElementKeyResolverEntry ok = null;
//        for (NElementKeyResolverEntry e : serializers_lvl2_customMappersByKey) {
//            if (e.resolver.equals(resolver)) {
//                ok = e;
//                break;
//            }
//        }
//        if (ok == null) {
//            ok = new NElementKeyResolverEntry<>(resolver);
//            destructors_lvl2_customMappersByKey.add(ok);
//        }
//        if (type instanceof Class) {
//            destructors_lvl1_customMappersByType.put((Class) type, instance);
//        }
//        ok.byKey.put(key, instance);
//        return this;
//    }
//
//    public <T> NElementMapperStore setDestructor(NElementType elementType, Type type, NElementDestructor<T> instance) {
//        return setDestructor(NELEMENTTYPE_KEY_RESOLVER, elementType, type, instance);
//    }
//
//    public final <T> NElementMapperStore setDestructor(NElementType elementType, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDestructor<T> instance) {
//        if (nameSelectorStrategy == null) {
//            nameSelectorStrategy = NNameSelectorStrategy.CASE_SENSITIVE;
//        }
//        NElementKeyResolver<NElementTypeAndName> resolver = CASE_SENSITIVE_NAME_RESOLVER;
//        switch (nameSelectorStrategy) {
//            case CASE_INSENSITIVE: {
//                resolver = CASE_INSENSITIVE_NAME_RESOLVER;
//                break;
//            }
//            case FORMAT_INSENSITIVE: {
//                resolver = FORMAT_INSENSITIVE_NAME_RESOLVER;
//                break;
//            }
//        }
//        return setDestructor(resolver, new NElementTypeAndName(elementType, name), type, instance);
//    }
//
//    public final <T> NElementMapperStore setDestructor(NElementType elementType, String name, Type type, NElementDestructor<T> instance) {
//        return setDestructor(CASE_SENSITIVE_NAME_RESOLVER, new NElementTypeAndName(elementType, name), type, instance);
//    }
//
//    @Override
//    public <T> NElementMapperStore setDestructor(NElementType[] elementTypes, Type type, NElementDestructor<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            setDestructor(elementType, type, instance);
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setDestructor(NElementType[] elementTypes, String name, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDestructor<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            setDestructor(elementType, name, nameSelectorStrategy, type, instance);
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setDestructor(NElementType[] elementTypes, String name, Type type, NElementDestructor<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            setDestructor(elementType, name, type, instance);
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setDestructor(NElementType[] elementTypes, String[] names, NNameSelectorStrategy nameSelectorStrategy, Type type, NElementDestructor<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            for (String name : names) {
//                setDestructor(elementType, name, nameSelectorStrategy, type, instance);
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public <T> NElementMapperStore setDestructor(NElementType[] elementTypes, String[] names, Type type, NElementDestructor<T> instance) {
//        for (NElementType elementType : elementTypes) {
//            for (String name : names) {
//                setDestructor(elementType, name, type, instance);
//            }
//        }
//        return this;
//    }


//    @Override
//    public <T> NElementDestructor<T> getDestructor(NElement element) {
//        return getDestructor(element, false);
//    }

    @Override
    public <T> NElementSimplifier<T> getSimplifier(Type type) {
        return getSimplifier(type, false);
    }

    @Override
    public <T> NElementSimplifier<T> getSimplifier(Type type, boolean defaultOnly) {
        if (type == null) {
            return DefaultElementMapperStore.F_NULL;
        }
        Class cls = ReflectUtils.getRawClass(type).get();
        if (NSession.class.isAssignableFrom(cls)) {
            throw new NIllegalArgumentException(NMsg.ofC(
                    "%s is not serializable", type
            ));
        }
        if (cls.isArray()) {
            NElementSimplifier f = defaultElementMapperStore.getCoreDestructors().getExact(cls);
            if (f != null) {
                return f;
            }
            return DefaultElementMapperStore.F_NUTS_ARR;
        }
        if (!defaultOnly) {
            NElementSimplifier f = destructors_lvl1_customMappersByType.get(cls);
            if (f != null) {
                return f;
            }
            final NElementSimplifier r = defaultElementMapperStore.getCoreDestructors().get(cls);
            if (r != null) {
                return r;
            }
        }
        final NElementSimplifier r = defaultElementMapperStore.getDefaultDestructors().get(cls);
        if (r != null) {
            return r;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to find element mapper for type : %s", type
        ));
    }

//
//    public <T> NElementDestructor<T> getDestructor(NElement element, boolean defaultOnly) {
//        NAssert.requireNamedNonNull(element, "element");
//        if (!defaultOnly) {
//            for (NElementKeyResolverEntry e : destructors_lvl2_customMappersByKey) {
//                Object k = e.resolver.keyOf(element);
//                if (k != null) {
//                    NElementMapper u = (NElementMapper) e.byKey.get(k);
//                    if (u != null) {
//                        return u;
//                    }
//                }
//            }
//        }
//        final NElementDestructor r = defaultElementMapperStore.getDestructor(element, this);
//        if (r != null) {
//            return r;
//        }
//        throw new NIllegalArgumentException(NMsg.ofC(
//                "unable to find element serializer for element type %s. element is : %s", element.type().id(), element
//        ));
//    }

    /// ////////////////////////////////////


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
        private final NElementType type;
        private final String name;

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
