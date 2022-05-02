package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTexts;

import java.lang.reflect.Type;

public class NutsElementMapperNutsText implements NutsElementMapper<NutsText> {

    @Override
    public Object destruct(NutsText src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src.filteredText();
    }

    @Override
    public NutsElement createElement(NutsText o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NutsText createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        String i = context.defaultElementToObject(o, String.class);
        //return NutsTexts.of(context.getSession()).parse(i).toText();
        return NutsTexts.of(context.getSession()).ofPlain(i).toText();
    }
}
