package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperBoolean implements NElementMapper<Boolean> {

    @Override
    public Object toSimple(Boolean src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Boolean o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofBoolean((Boolean) o);
    }

    @Override
    public Boolean createObject(NElementDeserializerContext context) {
        Type to = context.to();
        NElement element = context.element();
        if(to==null){
            to=Boolean.class;
        }
        switch (((Class) to).getName()) {
            case "boolean":
            case "java.lang.Boolean":
                return element.asBooleanValue().get();
        }
        throw new UnsupportedOperationException("Not supported.");
    }

}
