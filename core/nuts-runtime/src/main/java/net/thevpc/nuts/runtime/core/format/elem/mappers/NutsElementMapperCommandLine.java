package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;

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
        return context.getSession().commandLine().create(i);
    }
}
