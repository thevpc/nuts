package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDescriptorBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDescriptor implements NutsElementMapper<NutsDescriptor> {

    @Override
    public Object destruct(NutsDescriptor src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(
                context.getSession().descriptor().descriptorBuilder().setAll(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsDescriptor o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(
                context.getSession().descriptor().descriptorBuilder().setAll(o), null
        );
    }

    @Override
    public NutsDescriptor createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsDescriptorBuilder builder = (DefaultNutsDescriptorBuilder) context.defaultElementToObject(o, DefaultNutsDescriptorBuilder.class);
        return context.getSession().descriptor().descriptorBuilder().setAll(builder).build();
    }

}
