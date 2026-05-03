package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperFloatArray implements NElementMapper<float[]> {

    public NElementMapperFloatArray() {
    }

    @Override
    public NElement createElement(float[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object toSimple(float[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public float[] createObject(NElementDeserializerContext context) {
        NArrayElement earr = context.element().asArray().get();
        float[] arr = new float[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (float) context.toObject(earr.get(i).get(), float.class);
        }
        return arr;
    }
}
