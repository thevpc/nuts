package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsDescriptorBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDescriptor implements NutsElementMapper<NutsDescriptor> {

    @Override
    public Object destruct(NutsDescriptor src, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return context.defaultDestruct(
                NutsDescriptorBuilder.of(session).setAll(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsDescriptor o, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return context.defaultObjectToElement(
                NutsDescriptorBuilder.of(session).setAll(o), null
        );
    }

    @Override
    public NutsDescriptor createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsDescriptorBuilder builder = (DefaultNutsDescriptorBuilder) context.defaultElementToObject(o, DefaultNutsDescriptorBuilder.class);
        NutsSession session = context.getSession();
        return NutsDescriptorBuilder.of(session).setAll(builder).build();
    }

}
