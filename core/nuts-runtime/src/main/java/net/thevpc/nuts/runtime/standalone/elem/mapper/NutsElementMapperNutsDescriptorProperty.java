package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDescriptorProperty implements NutsElementMapper<NutsDescriptorProperty> {

    @Override
    public Object destruct(NutsDescriptorProperty src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(
                new DefaultNutsDescriptorPropertyBuilder(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsDescriptorProperty o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(
                new DefaultNutsDescriptorPropertyBuilder(o), null
        );
    }

    @Override
    public NutsDescriptorProperty createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsDescriptorPropertyBuilder builder = (DefaultNutsDescriptorPropertyBuilder) context.defaultElementToObject(o, DefaultNutsDescriptorPropertyBuilder.class);
        return new DefaultNutsDescriptorPropertyBuilder(builder).build();
    }

}
