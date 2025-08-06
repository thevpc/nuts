package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.io.NPath;

import java.lang.reflect.Type;

public class NElementMapperNPath implements NElementMapper<NPath> {

    @Override
    public Object destruct(NPath src, Type typeOfSrc, NElementFactoryContext context) {
        return src.toString();
    }

    @Override
    public NElement createElement(NPath o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(destruct(o, null, context), null);
    }

    @Override
    public NPath createObject(NElement o, Type to, NElementFactoryContext context) {
        String i = context.defaultCreateObject(o, String.class);
        return NPath.of(i);
    }
}
