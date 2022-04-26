package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Type;

public class NutsElementMapperPrimitiveShortArray implements NutsElementMapper<short[]> {

    public NutsElementMapperPrimitiveShortArray() {
    }

    @Override
    public NutsElement createElement(short[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(short[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

    @Override
    public short[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsArrayElement earr = o.asArray().get(session);
        short[] arr = new short[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (short) context.elementToObject(earr.get(i).get(session), short.class);
        }
        return arr;
    }
}
