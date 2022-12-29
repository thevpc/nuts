package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveLongArray implements NElementMapper<long[]> {

    public NElementMapperPrimitiveLongArray() {
    }

    @Override
    public NElement createElement(long[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(long[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public long[] createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        NArrayElement earr = o.asArray().get(session);
        long[] arr = new long[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (long) context.elementToObject(earr.get(i).get(session), long.class);
        }
        return arr;
    }
}
