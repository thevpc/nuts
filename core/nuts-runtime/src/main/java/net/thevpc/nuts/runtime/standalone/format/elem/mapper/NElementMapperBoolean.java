package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;

public class NElementMapperBoolean implements NElementMapper<Boolean> {

    @Override
    public Object destruct(Boolean src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Boolean o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofBoolean((Boolean) o);
    }

    @Override
    public Boolean createObject(NElement o, Type to, NElementFactoryContext context) {
        if(to==null){
            to=Boolean.class;
        }
        switch (((Class) to).getName()) {
            case "boolean":
            case "java.lang.Boolean":
                return o.asBooleanValue().get();
        }
        throw new UnsupportedOperationException("Not supported.");
    }

}
