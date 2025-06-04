package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperString implements NElementMapper<String> {

    @Override
    public Object destruct(String src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(String o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofString(String.valueOf(o));
    }

    @Override
    public String createObject(NElement o, Type to, NElementFactoryContext context) {
        return o.asStringValue().get();
    }
}
