package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveCharArray implements NElementMapper<char[]> {

    public NElementMapperPrimitiveCharArray() {
    }

    @Override
    public Object toSimple(char[] src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

    @Override
    public NElement createElement(char[] src, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofString(new String(src));
    }

    @Override
    public char[] createObject(NElementDeserializerContext context) {
        NElement element = context.element();
        NArrayElement earr = element.asArray().get();
        String s = (String) context.toObject(element, String.class);
        return s.toCharArray();
    }
}
