package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.reflect.NReflectUtils;

import java.lang.reflect.Type;

public class NElementMapperNull implements NElementMapper<Object> {

    @Override
    public Object toSimple(Object src, Type typeOfSrc, NElementFactoryContext context) {
        return null;
    }

    @Override
    public NElement createElement(Object o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofNull();
    }

    @Override
    public Object createObject(NElementDeserializerContext context) {
        Type to = context.to();
        return to==null?null:NReflectUtils.getJavaDefaultValue(to);
    }

}
