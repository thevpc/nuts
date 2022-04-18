package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;

import java.lang.reflect.Type;

public class NutsElementMapperNutsEnvConditionBuilder implements NutsElementMapper<NutsEnvConditionBuilder> {

    @Override
    public Object destruct(NutsEnvConditionBuilder src, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultDestruct(
                new DefaultNutsEnvConditionBuilder(src), null
        );
    }

    @Override
    public NutsElement createElement(NutsEnvConditionBuilder o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.defaultObjectToElement(
                new DefaultNutsEnvConditionBuilder(o), null
        );
    }

    @Override
    public NutsEnvConditionBuilder createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsEnvCondition builder = context.defaultElementToObject(o, DefaultNutsEnvCondition.class);
        return new DefaultNutsEnvConditionBuilder(builder);
    }

}
