package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.DefaultNEnvConditionBuilder;

import java.lang.reflect.Type;

public class NElementMapperNEnvCondition implements NElementMapper<NEnvCondition> {

    @Override
    public Object destruct(NEnvCondition src, Type typeOfSrc, NElementFactoryContext context) {
        NSession session = context.getSession();
        return context.defaultDestruct(
                new DefaultNEnvConditionBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NEnvCondition o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(new DefaultNEnvConditionBuilder(o), null);
    }

    @Override
    public NEnvCondition createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NEnvConditionBuilder builder = context.defaultElementToObject(o, DefaultNEnvConditionBuilder.class);
        return new DefaultNEnvConditionBuilder(builder).build();
    }

}
