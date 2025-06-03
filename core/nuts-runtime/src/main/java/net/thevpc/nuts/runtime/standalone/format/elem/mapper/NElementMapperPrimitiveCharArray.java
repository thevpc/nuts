package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.format.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveCharArray implements NElementMapper<char[]> {

    public NElementMapperPrimitiveCharArray() {
    }

    @Override
    public Object destruct(char[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public NElement createElement(char[] src, Type typeOfSrc, NElementFactoryContext context) {
        return NElements.ofString(new String(src));
    }

    @Override
    public char[] createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NArrayElement earr = o.asArray().get();
        String s = (String) context.elementToObject(o, String.class);
        return s.toCharArray();
    }
}
