package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.DefaultNEnvCondition;
import net.thevpc.nuts.NEnvCondition;
import net.thevpc.nuts.NEnvConditionBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNEnvConditionBuilder;

import java.lang.reflect.Type;

public class NElementMapperNEnvConditionBuilder implements NElementMapper<NEnvConditionBuilder> {

    @Override
    public Object destruct(NEnvConditionBuilder src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(
                new DefaultNEnvConditionBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NEnvConditionBuilder o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(
                new DefaultNEnvConditionBuilder(o), null
        );
    }

    @Override
    public NEnvConditionBuilder createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NEnvCondition builder = context.defaultCreateObject(o, DefaultNEnvCondition.class);
        return new DefaultNEnvConditionBuilder(builder);
    }

}
