package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Type;

public class NutsElementMapperPrimitiveDoubleArray implements NutsElementMapper<double[]> {

    public NutsElementMapperPrimitiveDoubleArray() {
    }

    @Override
    public NutsElement createElement(double[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(double[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

    @Override
    public double[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsArrayElement earr = o.asArray();
        double[] arr = new double[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (double) context.elementToObject(earr.get(i), double.class);
        }
        return arr;
    }
}
