package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.util.NLiteral;

import java.lang.reflect.Type;

public class NElementMapperNLiteral implements NElementMapper<NLiteral> {

    @Override
    public Object toSimple(NLiteral src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(
                src.asObject().orNull(), null
        );
    }

    @Override
    public NElement createElement(NLiteral o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(o.asObject().orNull(), null);
    }

    @Override
    public NLiteral createObject(NElementDeserializerContext context) {
        NElement element = context.element();
        Object any = context.defaultToObject(element, Object.class);
        return NLiteral.of(any);
    }

}
