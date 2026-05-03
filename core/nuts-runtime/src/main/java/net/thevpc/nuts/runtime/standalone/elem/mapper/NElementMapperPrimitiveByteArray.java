package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveByteArray implements NElementMapper<byte[]> {

    public NElementMapperPrimitiveByteArray() {
    }

    @Override
    public NElement toElement(NElementSerializerContext<byte[]> context) {
        return DefaultNElementFactoryService._createArray1(context.instance(), context);
    }

    @Override
    public Object toSimple(NElementSerializerContext<byte[]> context) {
        return DefaultNElementFactoryService._destructArray1(context.instance(), context);
    }

    @Override
    public byte[] toObject(NElementDeserializerContext context) {
        NArrayElement earr = context.element().asArray().get();
        byte[] arr = new byte[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) context.toObject(earr.get(i).get(), byte.class);
        }
        return arr;
    }
}
