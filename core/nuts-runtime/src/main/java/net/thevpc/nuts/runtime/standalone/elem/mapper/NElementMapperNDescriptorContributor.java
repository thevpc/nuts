package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorContributorBuilder;
import net.thevpc.nuts.NDescriptorContributor;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorContributor implements NElementMapper<NDescriptorContributor> {

    @Override
    public Object destruct(NDescriptorContributor src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(
                new DefaultNDescriptorContributorBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptorContributor o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(
                new DefaultNDescriptorContributorBuilder(o), null
        );
    }

    @Override
    public NDescriptorContributor createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        DefaultNDescriptorContributorBuilder builder = (DefaultNDescriptorContributorBuilder) context.defaultElementToObject(o, DefaultNDescriptorContributorBuilder.class);
        return new DefaultNDescriptorContributorBuilder(builder).build();
    }

}
