package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;

public class NElementMapperNValue implements NElementMapper<NValue> {

    @Override
    public Object destruct(NValue src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(
                src.getRaw(), null
        );
    }

    @Override
    public NElement createElement(NValue o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(o.getRaw(), null);
    }

    @Override
    public NValue createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        Object any = context.defaultElementToObject(o, Object.class);
        return NValue.of(any);
    }

}
