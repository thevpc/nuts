package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveIntArray implements NElementMapper<int[]> {

    public NElementMapperPrimitiveIntArray() {
    }

    @Override
    public NElement toElement(NElementSerializerContext<int[]> context) {
        return DefaultNElementFactoryService._createArray1(context.instance(), context);
    }

    @Override
    public Object toSimple(NElementSerializerContext<int[]> context) {
        return DefaultNElementFactoryService._destructArray1(context.instance(), context);
    }

    @Override
    public int[] toObject(NElementDeserializerContext context) {
        NArrayElement earr = context.element().asArray().get();
        int[] arr = new int[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int) context.toObject(earr.get(i).get(), int.class);
        }
        return arr;
    }
}
