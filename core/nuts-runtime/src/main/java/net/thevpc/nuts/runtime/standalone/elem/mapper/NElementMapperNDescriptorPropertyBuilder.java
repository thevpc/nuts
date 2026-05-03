package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NDescriptorPropertyBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorPropertyBuilder;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorPropertyBuilder implements NElementMapper<NDescriptorPropertyBuilder> {

    @Override
    public Object toSimple(NElementSerializerContext<NDescriptorPropertyBuilder> context) {
        return context.defaultToSimple(
                new DefaultNDescriptorPropertyBuilder(context.instance()), null
        );
    }

    @Override
    public NElement toElement(NElementSerializerContext<NDescriptorPropertyBuilder> context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorPropertyBuilder(context.instance()), null
        );
    }

    @Override
    public NDescriptorPropertyBuilder toObject(NElementDeserializerContext context) {
        DefaultNDescriptorPropertyBuilder builder = context.defaultToObject(context.element(), DefaultNDescriptorPropertyBuilder.class);
        return new DefaultNDescriptorPropertyBuilder(builder);
    }

}
