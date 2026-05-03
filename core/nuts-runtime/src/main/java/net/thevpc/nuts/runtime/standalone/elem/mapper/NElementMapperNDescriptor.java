package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperNDescriptor implements NElementMapper<NDescriptor> {

    @Override
    public Object toSimple(NElementSerializerContext<NDescriptor> context) {
        return context.defaultToSimple(
                new DefaultNDescriptorBuilder().copyFrom(context.instance()), null
        );
    }

    @Override
    public NElement toElement(NElementSerializerContext<NDescriptor> context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorBuilder().copyFrom(context.instance()), null
        );
    }

    @Override
    public NDescriptor toObject(NElementDeserializerContext context) {
        NElement element = context.element();
        DefaultNDescriptorBuilder builder = (DefaultNDescriptorBuilder) context.defaultToObject(element, DefaultNDescriptorBuilder.class);
        return new DefaultNDescriptorBuilder().copyFrom(builder).build();
    }

}
