package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveBooleanArray implements NElementMapper<boolean[]> {

    public NElementMapperPrimitiveBooleanArray() {
    }

    @Override
    public Object destruct(boolean[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public NElement createElement(boolean[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public boolean[] createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NArrayElement earr = o.asArray().get();
        boolean[] arr = new boolean[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (boolean) context.elementToObject(earr.get(i).get(), boolean.class);
        }
        return arr;
    }
}
