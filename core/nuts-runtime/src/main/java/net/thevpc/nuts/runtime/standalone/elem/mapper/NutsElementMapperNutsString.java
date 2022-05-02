package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.text.NutsTexts;

import java.lang.reflect.Type;

public class NutsElementMapperNutsString implements NutsElementMapper<NutsString> {

    @Override
    public Object destruct(NutsString src, Type typeOfSrc, NutsElementFactoryContext context) {
        if(context.isNtf()) {
            return src.toString();
        }else{
            return src.filteredText();
        }
    }

    @Override
    public NutsElement createElement(NutsString o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NutsString createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        String i = context.defaultElementToObject(o, String.class);
        return NutsTexts.of(context.getSession()).parse(i);
    }
}
