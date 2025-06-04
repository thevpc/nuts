package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.util.NLiteral;

import java.lang.reflect.Type;

public class NElementMapperNLiteral implements NElementMapper<NLiteral> {

    @Override
    public Object destruct(NLiteral src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(
                src.asObject().orNull(), null
        );
    }

    @Override
    public NElement createElement(NLiteral o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(o.asObject().orNull(), null);
    }

    @Override
    public NLiteral createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        Object any = context.defaultCreateObject(o, Object.class);
        return NLiteral.of(any);
    }

}
