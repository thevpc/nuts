package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
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
        NutsArrayElement earr = o.asArray();
        short[] arr = new short[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (short) context.elementToObject(earr.get(i), short.class);
        }
        return arr;
    }
}
