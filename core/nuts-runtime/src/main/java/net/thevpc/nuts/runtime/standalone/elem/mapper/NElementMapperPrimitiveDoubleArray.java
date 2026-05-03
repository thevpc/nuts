package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveDoubleArray implements NElementMapper<double[]> {

    public NElementMapperPrimitiveDoubleArray() {
    }

    @Override
    public NElement toElement(NElementSerializerContext<double[]> context) {
        return DefaultNElementFactoryService._createArray1(context.instance(), context);
    }

    @Override
    public Object toSimple(NElementSerializerContext<double[]> context) {
        return DefaultNElementFactoryService._destructArray1(context.instance(), context);
    }

    @Override
    public double[] toObject(NElementDeserializerContext context) {
        NArrayElement earr = context.element().asArray().get();
        double[] arr = new double[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (double) context.toObject(earr.get(i).get(), double.class);
        }
        return arr;
    }
}
