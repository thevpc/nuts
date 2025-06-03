package net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.util.NClassMap;
import net.thevpc.nuts.util.NMsg;

import java.lang.reflect.Type;

public class UserElementMapperStore implements NElementMapperStore {
    private CoreElementMapperStore coreElementMapperStore;
    private DefaultElementMapperStore defaultElementMapperStore;
    private final NClassMap<NElementMapper> customMappers = new NClassMap<>(null, NElementMapper.class);
    public UserElementMapperStore() {
        NWorkspaceModel model = NWorkspaceExt.of().getModel();
        coreElementMapperStore = model.coreElementMapperStore;
        defaultElementMapperStore = model.defaultElementMapperStore;
    }

    public final void setMapper(Type cls, NElementMapper instance) {
        if (instance == null) {
            NElementMapper cc = coreElementMapperStore.getCoreMappers().get(cls);
            if (cc != null) {
                customMappers.put((Class)cls, cc);
            } else {
                customMappers.remove((Class)cls);
            }
        } else {
            customMappers.put((Class)cls, instance);
        }
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
            NElementMapper f = customMappers.get(cls);
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
}
