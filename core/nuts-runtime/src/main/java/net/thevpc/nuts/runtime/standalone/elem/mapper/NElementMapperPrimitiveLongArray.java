package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveLongArray implements NElementMapper<long[]> {

    public NElementMapperPrimitiveLongArray() {
    }

    @Override
    public NElement toElement(NElementSerializerContext<long[]> context) {
        return DefaultNElementFactoryService._createArray1(context.instance(), context);
    }

    @Override
    public Object toSimple(NElementSerializerContext<long[]> context) {
        return DefaultNElementFactoryService._destructArray1(context.instance(), context);
    }

    @Override
    public long[] toObject(NElementDeserializerContext context) {
        NArrayElement earr = context.element().asArray().get();
        long[] arr = new long[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (long) context.toObject(earr.get(i).get(), long.class);
        }
        return arr;
    }
}
