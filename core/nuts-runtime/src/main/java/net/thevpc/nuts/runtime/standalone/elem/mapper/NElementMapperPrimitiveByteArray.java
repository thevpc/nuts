package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveByteArray implements NElementMapper<byte[]> {

    public NElementMapperPrimitiveByteArray() {
    }

    @Override
    public NElement createElement(byte[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(byte[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public byte[] createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NArrayElement earr = o.asArray().get();
        byte[] arr = new byte[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) context.createObject(earr.get(i).get(), byte.class);
        }
        return arr;
    }
}
