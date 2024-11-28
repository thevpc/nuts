package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;

public class NElementMapperNDescriptor implements NElementMapper<NDescriptor> {

    @Override
    public Object destruct(NDescriptor src, Type typeOfSrc, NElementFactoryContext context) {
        NSession session = context.getSession();
        return context.defaultDestruct(
                new DefaultNDescriptorBuilder().setAll(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptor o, Type typeOfSrc, NElementFactoryContext context) {
        NSession session = context.getSession();
        return context.defaultObjectToElement(
                new DefaultNDescriptorBuilder().setAll(o), null
        );
    }

    @Override
    public NDescriptor createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        DefaultNDescriptorBuilder builder = (DefaultNDescriptorBuilder) context.defaultElementToObject(o, DefaultNDescriptorBuilder.class);
        NSession session = context.getSession();
        return new DefaultNDescriptorBuilder().setAll(builder).build();
    }

}
