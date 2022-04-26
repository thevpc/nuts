package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Type;

public class NutsElementMapperPrimitiveLongArray implements NutsElementMapper<long[]> {

    public NutsElementMapperPrimitiveLongArray() {
    }

    @Override
    public NutsElement createElement(long[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(long[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

    @Override
    public long[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsArrayElement earr = o.asArray().get(session);
        long[] arr = new long[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (long) context.elementToObject(earr.get(i).get(session), long.class);
        }
        return arr;
    }
}
