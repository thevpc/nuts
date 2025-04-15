package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.format.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveIntArray implements NElementMapper<int[]> {

    public NElementMapperPrimitiveIntArray() {
    }

    @Override
    public NElement createElement(int[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(int[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public int[] createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NArrayElement earr = o.asArray().get();
        int[] arr = new int[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (int) context.elementToObject(earr.get(i).get(), int.class);
        }
        return arr;
    }
}
