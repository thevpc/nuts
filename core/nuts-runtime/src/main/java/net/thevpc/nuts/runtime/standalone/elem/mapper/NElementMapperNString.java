package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NTexts;

import java.lang.reflect.Type;

public class NElementMapperNString implements NElementMapper<NString> {

    @Override
    public Object destruct(NString src, Type typeOfSrc, NElementFactoryContext context) {
        if(context.isNtf()) {
            return src.toString();
        }else{
            return src.filteredText();
        }
    }

    @Override
    public NElement createElement(NString o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NString createObject(NElement o, Type to, NElementFactoryContext context) {
        String i = context.defaultElementToObject(o, String.class);
        return NTexts.of().parse(i);
    }
}
