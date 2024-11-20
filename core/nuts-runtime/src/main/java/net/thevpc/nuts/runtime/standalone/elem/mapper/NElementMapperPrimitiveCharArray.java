package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

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
        return context.elem().ofString(new String(src));
    }

    @Override
    public char[] createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        NArrayElement earr = o.asArray().get();
        String s = (String) context.elementToObject(o, String.class);
        return s.toCharArray();
    }
}
