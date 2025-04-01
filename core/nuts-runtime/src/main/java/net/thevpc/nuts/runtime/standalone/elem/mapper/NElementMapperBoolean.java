package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.NSession;

import java.lang.reflect.Type;

public class NElementMapperBoolean implements NElementMapper<Boolean> {

    @Override
    public Object destruct(Boolean src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Boolean o, Type typeOfSrc, NElementFactoryContext context) {
        return context.elem().ofBoolean((Boolean) o);
    }

    @Override
    public Boolean createObject(NElement o, Type to, NElementFactoryContext context) {
        switch (((Class) to).getName()) {
            case "boolean":
            case "java.lang.Boolean":
                return o.asBoolean().get();
        }
        throw new UnsupportedOperationException("Not supported.");
    }

}
