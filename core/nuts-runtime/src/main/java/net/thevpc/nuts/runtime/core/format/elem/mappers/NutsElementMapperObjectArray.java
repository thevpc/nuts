package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFactoryService;

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
        NutsArrayElement earr = o.asArray();
        Object[] arr = new Object[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (Object) context.elementToObject(earr.get(i), Object.class);
        }
        return arr;
    }
}
