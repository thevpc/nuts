package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;

import java.lang.reflect.Type;

public class NElementMapperBoolean implements NElementMapper<Boolean> {

    @Override
    public Object toSimple(NElementSerializerContext<Boolean> context) {
        return context.instance();
    }

    @Override
    public NElement toElement(NElementSerializerContext<Boolean> context) {
        return NElement.ofBoolean(context.instance());
    }

    @Override
    public Boolean toObject(NElementDeserializerContext context) {
        Type to = context.instanceType();
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
