package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTexts;

import java.lang.reflect.Type;

public class NElementMapperNText implements NElementMapper<NText> {

    @Override
    public Object destruct(NText src, Type typeOfSrc, NElementFactoryContext context) {
        return src.filteredText();
    }

    @Override
    public NElement createElement(NText o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NText createObject(NElement o, Type to, NElementFactoryContext context) {
        String i = context.defaultElementToObject(o, String.class);
        //return NutsTexts.of(context.getSession()).parse(i).toText();
        return NText.ofPlain(i);
    }
}
