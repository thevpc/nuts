package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveShortArray implements NElementMapper<short[]> {

    public NElementMapperPrimitiveShortArray() {
    }

    @Override
    public NElement createElement(short[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(short[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public short[] createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        NArrayElement earr = o.asArray().get();
        short[] arr = new short[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (short) context.elementToObject(earr.get(i).get(), short.class);
        }
        return arr;
    }
}
