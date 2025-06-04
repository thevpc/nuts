package net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.format.elem.CoreNElementUtils;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NClassMap;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NNameFormat;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Predicate;

public class UserElementMapperStore implements NElementMapperStore {
    public static final NElementTypeNElementKeyResolver NELEMENTTYPE_KEY_RESOLVER = new NElementTypeNElementKeyResolver();
    public static final NElementTypeAndNameNElementKeyResolver NELEMENTTYPE_AND_NAME_KEY_RESOLVER = new NElementTypeAndNameNElementKeyResolver();
    public static final NElementTypeAndNameNoCaseNElementKeyResolver NELEMENTTYPE_AND_NAME_NO_CASE_KEY_RESOLVER = new NElementTypeAndNameNoCaseNElementKeyResolver();
    public static final NElementTypeAndNameNoFormatNElementKeyResolver NELEMENTTYPE_AND_NAME_NO_FORMAT_KEY_RESOLVER = new NElementTypeAndNameNoFormatNElementKeyResolver();
    private CoreElementMapperStore coreElementMapperStore;
    private DefaultElementMapperStore defaultElementMapperStore;
    private final NClassMap<NElementMapper> lvl1_customMappersByType = new NClassMap<>(null, NElementMapper.class);
    private final List<NElementKeyResolverEntry> lvl2_customMappersByKey = new ArrayList<>();
    private Predicate<Class<?>> indestructibleObjects;

    static class NElementKeyResolverEntry<T> {
        NElementKeyResolver<T> resolver;
        Map<T, NElementMapper> byKey = new HashMap<>();
        Class<T> type;

        public NElementKeyResolverEntry(NElementKeyResolver<T> resolver) {
            this.resolver = resolver;
        }
    }

    public Predicate<Class<?>> getIndestructibleObjects() {
        return indestructibleObjects;
    }

    @Override
    public NElements setIndestructibleFormat() {
        return setIndestructibleObjects(CoreNElementUtils.DEFAULT_INDESTRUCTIBLE_FORMAT);
    }

    public NElements setIndestructibleObjects(Predicate<Class<?>> destructTypeFilter) {
        this.indestructibleObjects = destructTypeFilter;
        return this;
    }


    public UserElementMapperStore() {
        NWorkspaceModel model = NWorkspaceExt.of().getModel();
        coreElementMapperStore = model.coreElementMapperStore;
        defaultElementMapperStore = model.defaultElementMapperStore;
    }

    public final void setMapper(Type cls, NElementMapper instance) {
        if (instance == null) {
            NElementMapper cc = coreElementMapperStore.getCoreMappers().get(cls);
            if (cc != null) {
                lvl1_customMappersByType.put((Class) cls, cc);
            } else {
                lvl1_customMappersByType.remove((Class) cls);
            }
        } else {
            lvl1_customMappersByType.put((Class) cls, instance);
        }
    }

    public final <K, T> void setMapper(NElementKeyResolver<K> resolver, K key, Class<T> type, NElementMapper<T> instance) {
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
        lvl1_customMappersByType.put(type, instance);
        ok.byKey.put(key, instance);
    }

    public final <T> void setMapper(NElementType elementType, Class<T> type, NElementMapper instance) {
        setMapper(NELEMENTTYPE_KEY_RESOLVER, elementType, type, instance);
    }

    public final <T> void setMapper(NElementType elementType, String name, NMappedNameStrategy mappedName, Class<T> type, NElementMapper instance) {
        setMapper(NELEMENTTYPE_AND_NAME_KEY_RESOLVER, new NElementTypeAndName(elementType, name), type, instance);
    }

    public final <T> void setMapper(NElementType elementType, String name, Class<T> type, NElementMapper instance) {
        setMapper(NELEMENTTYPE_AND_NAME_KEY_RESOLVER, new NElementTypeAndName(elementType, name), type, instance);
    }

    public static enum NMappedNameStrategy {
        IGNORE,
        CASE_INSENSITIVE,
        FORMAT_INSENSITIVE,
    }

    public NElementMapper getMapper(Type type, boolean defaultOnly) {
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
            NElementMapper f = defaultElementMapperStore.getDefaultMappers().getExact(cls);
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
        }
        final NElementMapper r = defaultElementMapperStore.getDefaultMappers().get(cls);
        if (r != null) {
            return r;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to find serialization factory for %s", type
        ));
    }


    public <T> NElementMapper<T> getMapper(NElement element, boolean defaultOnly) {
        NAssert.requireNonNull(element, "element");
        if (!defaultOnly) {
            NElementMapper f = lvl1_customMappersByType.get(cls);
            if (f != null) {
                return f;
            }
        }
        final NElementMapper r = defaultElementMapperStore.getDefaultMappers().get(cls);
        if (r != null) {
            return r;
        }
        throw new NIllegalArgumentException(NMsg.ofC(
                "unable to find serialization factory for %s", type
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
