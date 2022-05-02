package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsArrayElement;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Type;

public class NutsElementMapperPrimitiveIntArray implements NutsElementMapper<int[]> {

    public NutsElementMapperPrimitiveIntArray() {
    }

    @Override
    public NutsElement createElement(int[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(int[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

    @Override
    public int[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsArrayElement earr = o.asArray().get(session);
        int[] arr = new int[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int) context.elementToObject(earr.get(i).get(session), int.class);
        }
        return arr;
    }
}
