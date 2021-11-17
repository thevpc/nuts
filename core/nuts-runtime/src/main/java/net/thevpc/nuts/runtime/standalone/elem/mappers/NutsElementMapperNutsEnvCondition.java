package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsEnvConditionBuilder;

import java.lang.reflect.Type;

public class NutsElementMapperNutsEnvCondition implements NutsElementMapper<NutsEnvCondition> {

    @Override
    public Object destruct(NutsEnvCondition src, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return context.defaultDestruct(
                NutsEnvConditionBuilder.of(session).setAll(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsEnvCondition o, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return context.defaultObjectToElement(
                NutsEnvConditionBuilder.of(session).setAll(o), null
        );
    }

    @Override
    public NutsEnvCondition createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        DefaultNutsEnvConditionBuilder builder = (DefaultNutsEnvConditionBuilder) context.defaultElementToObject(o, DefaultNutsEnvConditionBuilder.class);
        NutsSession session = context.getSession();
        return NutsEnvConditionBuilder.of(session).setAll(builder).build();
    }

}
