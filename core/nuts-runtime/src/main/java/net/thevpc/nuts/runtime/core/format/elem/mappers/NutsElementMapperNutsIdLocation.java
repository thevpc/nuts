package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.NutsIdLocation;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsIdLocationBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsIdLocation implements NutsElementMapper<NutsIdLocation> {

    @Override
    public Object destruct(NutsIdLocation src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(new DefaultNutsIdLocationBuilder(context.getSession()).set(src), null);
    }

    @Override
    public NutsElement createElement(NutsIdLocation o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(new DefaultNutsIdLocationBuilder(context.getSession()).set(o), null);
    }

    @Override
    public NutsIdLocation createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsIdLocationBuilder builder = (DefaultNutsIdLocationBuilder) context.defaultElementToObject(o, DefaultNutsIdLocationBuilder.class);
        return builder.build();
    }

}
