package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.NSession;

import java.lang.reflect.Type;

public class NElementMapperString implements NElementMapper<String> {

    @Override
    public Object destruct(String src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(String o, Type typeOfSrc, NElementFactoryContext context) {
        return context.elem().ofString(String.valueOf(o));
    }

    @Override
    public String createObject(NElement o, Type to, NElementFactoryContext context) {
        NSession session = context.getSession();
        return o.asString().get();
    }
}
