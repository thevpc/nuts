package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;

import java.lang.reflect.Type;

public class NutsElementMapperCommandLine implements NutsElementMapper<NutsCommandLine> {

    @Override
    public Object destruct(NutsCommandLine src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src.toStringArray();
    }

    @Override
    public NutsElement createElement(NutsCommandLine o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NutsCommandLine createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        String[] i = context.defaultElementToObject(o, String[].class);
        return NutsCommandLine.of(i);
    }
}
