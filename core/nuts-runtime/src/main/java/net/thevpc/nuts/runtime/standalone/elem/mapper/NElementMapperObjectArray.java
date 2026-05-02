package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperObjectArray implements NElementMapper<Object[]> {

    public NElementMapperObjectArray() {
    }

    @Override
    public NElement createElement(Object[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object toSimple(Object[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public Object[] createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NArrayElement earr = o.asArray().get();
        Object[] arr = new Object[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (Object) context.toObject(earr.get(i).get(), Object.class);
        }
        return arr;
    }
}
