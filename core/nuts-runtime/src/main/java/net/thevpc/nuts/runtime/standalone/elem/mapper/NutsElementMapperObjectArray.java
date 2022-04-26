package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Type;

public class NutsElementMapperObjectArray implements NutsElementMapper<Object[]> {

    public NutsElementMapperObjectArray() {
    }

    @Override
    public NutsElement createElement(Object[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(Object[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

    @Override
    public Object[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsArrayElement earr = o.asArray().get(session);
        Object[] arr = new Object[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (Object) context.elementToObject(earr.get(i).get(session), Object.class);
        }
        return arr;
    }
}
