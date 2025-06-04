package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.text.NText;

import java.lang.reflect.Type;

public class NElementMapperNText implements NElementMapper<NText> {

    @Override
    public Object destruct(NText src, Type typeOfSrc, NElementFactoryContext context) {
        return src.filteredText();
    }

    @Override
    public NElement createElement(NText o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(destruct(o, null, context), null);
    }

    @Override
    public NText createObject(NElement o, Type to, NElementFactoryContext context) {
        String i = context.defaultCreateObject(o, String.class);
        //return NTexts.of(context.getSession()).parse(i).toText();
        return NText.ofPlain(i);
    }
}
