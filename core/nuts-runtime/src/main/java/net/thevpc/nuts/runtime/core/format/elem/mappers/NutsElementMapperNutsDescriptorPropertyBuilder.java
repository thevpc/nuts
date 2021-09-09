package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsDescriptorPropertyBuilder;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDescriptorPropertyBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsDescriptorPropertyBuilder implements NutsElementMapper<NutsDescriptorPropertyBuilder> {

    @Override
    public Object destruct(NutsDescriptorPropertyBuilder src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(
                context.getSession().getWorkspace().descriptor().propertyBuilder().setAll(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsDescriptorPropertyBuilder o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(
                context.getSession().getWorkspace().descriptor().propertyBuilder().setAll(o), null
        );
    }

    @Override
    public NutsDescriptorPropertyBuilder createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsDescriptorPropertyBuilder builder = (DefaultNutsDescriptorPropertyBuilder) context.defaultElementToObject(o, DefaultNutsDescriptorPropertyBuilder.class);
        return context.getSession().getWorkspace().descriptor().propertyBuilder().setAll(builder);
    }

}
