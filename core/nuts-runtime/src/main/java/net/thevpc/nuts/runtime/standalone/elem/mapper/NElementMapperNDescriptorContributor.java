package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorContributorBuilder;
import net.thevpc.nuts.artifact.NDescriptorContributor;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorContributor implements NElementMapper<NDescriptorContributor> {

    @Override
    public Object toSimple(NDescriptorContributor src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(
                new DefaultNDescriptorContributorBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptorContributor o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorContributorBuilder(o), null
        );
    }

    @Override
    public NDescriptorContributor createObject(NElementDeserializerContext context) {
        DefaultNDescriptorContributorBuilder builder = context.defaultToObject(context.element(), DefaultNDescriptorContributorBuilder.class);
        return new DefaultNDescriptorContributorBuilder(builder).build();
    }

}
