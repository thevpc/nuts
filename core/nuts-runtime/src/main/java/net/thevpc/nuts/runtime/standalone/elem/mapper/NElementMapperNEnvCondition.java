package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.artifact.NEnvCondition;
import net.thevpc.nuts.artifact.NEnvConditionBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNEnvConditionBuilder;

import java.lang.reflect.Type;

public class NElementMapperNEnvCondition implements NElementMapper<NEnvCondition> {

    @Override
    public Object toSimple(NEnvCondition src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(
                new DefaultNEnvConditionBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NEnvCondition o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(new DefaultNEnvConditionBuilder(o), null);
    }

    @Override
    public NEnvCondition createObject(NElementDeserializerContext context) {
        NEnvConditionBuilder builder = context.defaultToObject(context.element(), DefaultNEnvConditionBuilder.class);
        return new DefaultNEnvConditionBuilder(builder).build();
    }

}
