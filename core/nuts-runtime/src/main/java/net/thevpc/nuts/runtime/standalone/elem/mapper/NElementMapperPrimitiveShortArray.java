package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveShortArray implements NElementMapper<short[]> {

    public NElementMapperPrimitiveShortArray() {
    }

    @Override
    public NElement toElement(NElementSerializerContext<short[]> context) {
        short[] src = context.instance();
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object toSimple(NElementSerializerContext<short[]> context) {
        short[] src = context.instance();
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public short[] toObject(NElementDeserializerContext context) {
        NArrayElement earr = context.element().asArray().get();
        short[] arr = new short[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (short) context.toObject(earr.get(i).get(), short.class);
        }
        return arr;
    }
}
