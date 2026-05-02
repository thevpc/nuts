package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperNDescriptor implements NElementMapper<NDescriptor> {

    @Override
    public Object toSimple(NDescriptor src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(
                new DefaultNDescriptorBuilder().copyFrom(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptor o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorBuilder().copyFrom(o), null
        );
    }

    @Override
    public NDescriptor createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        DefaultNDescriptorBuilder builder = (DefaultNDescriptorBuilder) context.defaultToObject(o, DefaultNDescriptorBuilder.class);
        return new DefaultNDescriptorBuilder().copyFrom(builder).build();
    }

}
