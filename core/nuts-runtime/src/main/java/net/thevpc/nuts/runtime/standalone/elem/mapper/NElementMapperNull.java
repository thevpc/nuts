package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.reflect.NReflectUtils;

import java.lang.reflect.Type;

public class NElementMapperNull implements NElementMapper<Object> {

    @Override
    public Object toSimple(NElementSerializerContext<Object> context) {
        return null;
    }

    @Override
    public NElement toElement(NElementSerializerContext<Object> context) {
        return NElement.ofNull();
    }

    @Override
    public Object toObject(NElementDeserializerContext context) {
        Type to = context.instanceType();
        return to==null?null:NReflectUtils.getJavaDefaultValue(to);
    }

}
