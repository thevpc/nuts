package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsDescriptorPropertyBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDescriptorProperty implements NutsElementMapper<NutsDescriptorProperty> {

    @Override
    public Object destruct(NutsDescriptorProperty src, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return context.defaultDestruct(
                NutsDescriptorPropertyBuilder.of(session).setAll(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsDescriptorProperty o, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return context.defaultObjectToElement(
                NutsDescriptorPropertyBuilder.of(session).setAll(o), null
        );
    }

    @Override
    public NutsDescriptorProperty createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsDescriptorPropertyBuilder builder = (DefaultNutsDescriptorPropertyBuilder) context.defaultElementToObject(o, DefaultNutsDescriptorPropertyBuilder.class);
        NutsSession session = context.getSession();
        return NutsDescriptorPropertyBuilder.of(session).setAll(builder).build();
    }

}
