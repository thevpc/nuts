package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NDescriptorProperty;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorPropertyBuilder;

public class NElementMapperNDescriptorProperty implements NElementMapper<NDescriptorProperty> {

    @Override
    public Object toSimple(NElementSerializerContext<NDescriptorProperty> context) {
        return context.defaultToSimple(
                new DefaultNDescriptorPropertyBuilder(context.instance()), null
        );
    }

    @Override
    public NElement toElement(NElementSerializerContext<NDescriptorProperty>  context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorPropertyBuilder(context.instance()), null
        );
    }

    @Override
    public NDescriptorProperty toObject(NElementDeserializerContext context) {
        DefaultNDescriptorPropertyBuilder builder = context.defaultToObject(context.element(), DefaultNDescriptorPropertyBuilder.class);
        return new DefaultNDescriptorPropertyBuilder(builder).build();
    }

}
