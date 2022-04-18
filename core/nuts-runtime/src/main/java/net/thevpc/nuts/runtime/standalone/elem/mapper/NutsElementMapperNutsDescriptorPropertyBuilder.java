package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDescriptorPropertyBuilder implements NutsElementMapper<NutsDescriptorPropertyBuilder> {

    @Override
    public Object destruct(NutsDescriptorPropertyBuilder src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(
                new DefaultNutsDescriptorPropertyBuilder(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsDescriptorPropertyBuilder o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(
                new DefaultNutsDescriptorPropertyBuilder(o), null
        );
    }

    @Override
    public NutsDescriptorPropertyBuilder createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsDescriptorPropertyBuilder builder = context.defaultElementToObject(o, DefaultNutsDescriptorPropertyBuilder.class);
        return new DefaultNutsDescriptorPropertyBuilder(builder);
    }

}
