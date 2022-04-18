package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsEnvCondition implements NutsElementMapper<NutsEnvCondition> {

    @Override
    public Object destruct(NutsEnvCondition src, Type typeOfSrc, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        return context.defaultDestruct(
                new DefaultNutsEnvConditionBuilder(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsEnvCondition o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(new DefaultNutsEnvConditionBuilder(o), null);
    }

    @Override
    public NutsEnvCondition createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsEnvConditionBuilder builder = context.defaultElementToObject(o, DefaultNutsEnvConditionBuilder.class);
        return new DefaultNutsEnvConditionBuilder(builder).build();
    }

}
