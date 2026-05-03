package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.artifact.DefaultNEnvCondition;
import net.thevpc.nuts.artifact.NEnvCondition;
import net.thevpc.nuts.artifact.NEnvConditionBuilder;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.DefaultNEnvConditionBuilder;

import java.lang.reflect.Type;

public class NElementMapperNEnvConditionBuilder implements NElementMapper<NEnvConditionBuilder> {

    @Override
    public Object toSimple(NElementSerializerContext<NEnvConditionBuilder> context) {
        return context.defaultToSimple(
                new DefaultNEnvConditionBuilder(context.instance()), null
        );
    }

    @Override
    public NElement toElement(NElementSerializerContext<NEnvConditionBuilder> context) {
        return context.defaultCreateElement(
                new DefaultNEnvConditionBuilder(context.instance()), null
        );
    }

    @Override
    public NEnvConditionBuilder toObject(NElementDeserializerContext context) {
        NEnvCondition builder = context.defaultToObject(context.element(), DefaultNEnvCondition.class);
        return new DefaultNEnvConditionBuilder(builder);
    }

}
