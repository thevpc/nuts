package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorContributorBuilder;
import net.thevpc.nuts.artifact.NDescriptorContributor;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorContributor implements NElementMapper<NDescriptorContributor> {

    @Override
    public Object toSimple(NElementSerializerContext<NDescriptorContributor> context) {
        return context.defaultToSimple(
                new DefaultNDescriptorContributorBuilder(context.instance()), null
        );
    }

    @Override
    public NElement toElement(NElementSerializerContext<NDescriptorContributor> context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorContributorBuilder(context.instance()), null
        );
    }

    @Override
    public NDescriptorContributor toObject(NElementDeserializerContext context) {
        DefaultNDescriptorContributorBuilder builder = context.defaultToObject(context.element(), DefaultNDescriptorContributorBuilder.class);
        return new DefaultNDescriptorContributorBuilder(builder).build();
    }

}
