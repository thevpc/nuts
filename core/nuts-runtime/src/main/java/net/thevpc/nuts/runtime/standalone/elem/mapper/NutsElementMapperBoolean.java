package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.NutsSession;

import java.lang.reflect.Type;

public class NutsElementMapperBoolean implements NutsElementMapper<Boolean> {

    @Override
    public Object destruct(Boolean src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(Boolean o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().ofBoolean((Boolean) o);
    }

    @Override
    public Boolean createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        switch (((Class) to).getName()) {
            case "boolean":
            case "java.lang.Boolean":
                return o.asBoolean().get(session);
        }
        throw new UnsupportedOperationException("Not supported.");
    }

}
