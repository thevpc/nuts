package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsDescriptorProperty;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDescriptorPropertyBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDescriptorProperty implements NutsElementMapper<NutsDescriptorProperty> {

    @Override
    public Object destruct(NutsDescriptorProperty src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(
                context.getSession().descriptor().propertyBuilder().setAll(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsDescriptorProperty o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(
                context.getSession().descriptor().propertyBuilder().setAll(o), null
        );
    }

    @Override
    public NutsDescriptorProperty createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsDescriptorPropertyBuilder builder = (DefaultNutsDescriptorPropertyBuilder) context.defaultElementToObject(o, DefaultNutsDescriptorPropertyBuilder.class);
        return context.getSession().descriptor().propertyBuilder().setAll(builder).build();
    }

}
