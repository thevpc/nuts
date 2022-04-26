package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Type;

public class NutsElementMapperPrimitiveBooleanArray implements NutsElementMapper<boolean[]> {

    public NutsElementMapperPrimitiveBooleanArray() {
    }

    @Override
    public Object destruct(boolean[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

    @Override
    public NutsElement createElement(boolean[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._createArray1(src, context);
    }

    @Override
    public boolean[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsArrayElement earr = o.asArray().get(session);
        boolean[] arr = new boolean[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (boolean) context.elementToObject(earr.get(i).get(session), boolean.class);
        }
        return arr;
    }
}
