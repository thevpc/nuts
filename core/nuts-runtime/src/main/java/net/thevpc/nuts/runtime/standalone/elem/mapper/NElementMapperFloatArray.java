package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperFloatArray implements NElementMapper<float[]> {

    public NElementMapperFloatArray() {
    }

    @Override
    public NElement toElement(NElementSerializerContext<float[]> context) {
        return DefaultNElementFactoryService._createArray1(context.instance(), context);
    }

    @Override
    public Object toSimple(NElementSerializerContext<float[]> context) {
        return DefaultNElementFactoryService._destructArray1(context.instance(), context);
    }

    @Override
    public float[] toObject(NElementDeserializerContext context) {
        NArrayElement earr = context.element().asArray().get();
        float[] arr = new float[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = context.toObject(earr.get(i).get(), float.class);
        }
        return arr;
    }
}
