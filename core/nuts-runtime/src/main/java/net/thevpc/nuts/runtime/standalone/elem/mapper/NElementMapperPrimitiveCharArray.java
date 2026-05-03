package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Type;

public class NElementMapperPrimitiveCharArray implements NElementMapper<char[]> {

    public NElementMapperPrimitiveCharArray() {
    }

    @Override
    public Object toSimple(NElementSerializerContext<char[]> context) {
        return DefaultNElementFactoryService._destructArray1(context.instance(), context);
    }

    @Override
    public NElement toElement(NElementSerializerContext<char[]> context) {
        return NElement.ofString(new String(context.instance()));
    }

    @Override
    public char[] toObject(NElementDeserializerContext context) {
        NElement element = context.element();
        NArrayElement earr = element.asArray().get();
        String s = context.toObject(element, String.class);
        return s.toCharArray();
    }
}
