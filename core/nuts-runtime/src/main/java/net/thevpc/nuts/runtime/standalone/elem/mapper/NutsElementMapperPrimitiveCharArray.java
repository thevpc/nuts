package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Type;

public class NutsElementMapperPrimitiveCharArray implements NutsElementMapper<char[]> {

    public NutsElementMapperPrimitiveCharArray() {
    }

    @Override
    public Object destruct(char[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

    @Override
    public NutsElement createElement(char[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().ofString(new String(src));
    }

    @Override
    public char[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsArrayElement earr = o.asArray().get(session);
        String s = (String) context.elementToObject(o, String.class);
        return s.toCharArray();
    }
}
