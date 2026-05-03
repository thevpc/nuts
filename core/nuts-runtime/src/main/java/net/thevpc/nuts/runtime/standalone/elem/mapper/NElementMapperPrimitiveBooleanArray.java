package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveBooleanArray implements NElementMapper<boolean[]> {

    public NElementMapperPrimitiveBooleanArray() {
    }

    @Override
    public Object toSimple(NElementSerializerContext<boolean[]> context) {
        return DefaultNElementFactoryService._destructArray1(context.instance(), context);
    }

    @Override
    public NElement toElement(NElementSerializerContext<boolean[]> context) {
        return DefaultNElementFactoryService._createArray1(context.instance(), context);
    }

    @Override
    public boolean[] toObject(NElementDeserializerContext context) {
        NElement element = context.element();
        NArrayElement earr = element.asArray().get();
        boolean[] arr = new boolean[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (boolean) context.toObject(earr.get(i).get(), boolean.class);
        }
        return arr;
    }
}
