package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.NutsEnvConditionBuilder;
import net.thevpc.nuts.runtime.core.model.DefaultNutsEnvConditionBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsEnvConditionBuilder implements NutsElementMapper<NutsEnvConditionBuilder> {

    @Override
    public Object destruct(NutsEnvConditionBuilder src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(
                context.getSession().getWorkspace().descriptor().envConditionBuilder().setAll(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsEnvConditionBuilder o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(
                context.getSession().getWorkspace().descriptor().envConditionBuilder().setAll(o), null
        );
    }

    @Override
    public NutsEnvConditionBuilder createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsEnvConditionBuilder builder = (DefaultNutsEnvConditionBuilder) context.defaultElementToObject(o, DefaultNutsEnvConditionBuilder.class);
        return context.getSession().getWorkspace().descriptor().envConditionBuilder().setAll(builder);
    }

}
