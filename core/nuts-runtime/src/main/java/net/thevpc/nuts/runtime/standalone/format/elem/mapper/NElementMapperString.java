package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NStringElement;

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
        return o.asStringValue().get();
    }
}
