package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperObjectArray implements NElementMapper<Object[]> {

    public NElementMapperObjectArray() {
    }

    @Override
    public NElement toElement(NElementSerializerContext<Object[]> context) {
        return DefaultNElementFactoryService._createArray1(context.instance(), context);
    }

    @Override
    public Object toSimple(NElementSerializerContext<Object[]> context) {
        return DefaultNElementFactoryService._destructArray1(context.instance(), context);
    }

    @Override
    public Object[] toObject(NElementDeserializerContext context) {
        NArrayElement earr = context.element().asArray().get();
        Object[] arr = new Object[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (Object) context.toObject(earr.get(i).get(), Object.class);
        }
        return arr;
    }
}
