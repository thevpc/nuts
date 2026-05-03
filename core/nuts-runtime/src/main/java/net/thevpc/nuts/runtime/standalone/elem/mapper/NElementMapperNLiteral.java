package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.util.NLiteral;

import java.lang.reflect.Type;

public class NElementMapperNLiteral implements NElementMapper<NLiteral> {

    @Override
    public Object toSimple(NElementSerializerContext<NLiteral> context) {
        return context.defaultToSimple(
                context.instance().asObject().orNull(), null
        );
    }

    @Override
    public NElement toElement(NElementSerializerContext<NLiteral> context) {
        return context.defaultCreateElement(context.instance().asObject().orNull(), null);
    }

    @Override
    public NLiteral toObject(NElementDeserializerContext context) {
        NElement element = context.element();
        Object any = context.defaultToObject(element, Object.class);
        return NLiteral.of(any);
    }

}
