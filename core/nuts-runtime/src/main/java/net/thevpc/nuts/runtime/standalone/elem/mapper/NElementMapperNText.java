package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.elem.mapper.builder.NElementSerializerContextImpl;
import net.thevpc.nuts.text.NText;

import java.lang.reflect.Type;

public class NElementMapperNText implements NElementMapper<NText> {

    @Override
    public Object toSimple(NElementSerializerContext<NText> context) {
        return context.instance().filteredText();
    }

    @Override
    public NElement toElement(NElementSerializerContext<NText> context) {
        return context.defaultCreateElement(this.toSimple(NElementSerializerContextImpl.of(context.instance(), null, context)), null);
    }

    @Override
    public NText toObject(NElementDeserializerContext context) {
        String i = context.defaultToObject(context.element(), String.class);
        //return NTexts.of(context.getSession()).parse(i).toText();
        return NText.ofPlain(i);
    }
}
