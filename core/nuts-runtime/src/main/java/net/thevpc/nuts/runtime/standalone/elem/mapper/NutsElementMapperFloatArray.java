package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsArrayElement;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Type;

public class NutsElementMapperFloatArray implements NutsElementMapper<float[]> {

    public NutsElementMapperFloatArray() {
    }

    @Override
    public NutsElement createElement(float[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(float[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

    @Override
    public float[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsArrayElement earr = o.asArray().get(session);
        float[] arr = new float[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (float) context.elementToObject(earr.get(i).get(session), float.class);
        }
        return arr;
    }
}
