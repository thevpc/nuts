package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.text.NText;

import java.lang.reflect.Type;

public class NElementMapperNText implements NElementMapper<NText> {

    @Override
    public Object toSimple(NText src, Type typeOfSrc, NElementFactoryContext context) {
        return src.filteredText();
    }

    @Override
    public NElement createElement(NText o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(this.toSimple(o, null, context), null);
    }

    @Override
    public NText createObject(NElementDeserializerContext context) {
        String i = context.defaultToObject(context.element(), String.class);
        //return NTexts.of(context.getSession()).parse(i).toText();
        return NText.ofPlain(i);
    }
}
