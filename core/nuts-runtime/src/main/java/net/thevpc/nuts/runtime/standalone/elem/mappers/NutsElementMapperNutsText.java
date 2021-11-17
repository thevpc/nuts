package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;

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
