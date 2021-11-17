package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
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
        return context.elem().forString(new String(src));
    }

    @Override
    public char[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsArrayElement earr = o.asArray();
        String s = (String) context.elementToObject(o, String.class);
        return s.toCharArray();
    }
}
