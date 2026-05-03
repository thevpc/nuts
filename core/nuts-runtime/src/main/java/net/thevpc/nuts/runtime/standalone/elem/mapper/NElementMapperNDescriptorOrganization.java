package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorOrganizationBuilder;
import net.thevpc.nuts.artifact.NDescriptorOrganization;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorOrganization implements NElementMapper<NDescriptorOrganization> {

    @Override
    public Object toSimple(NElementSerializerContext<NDescriptorOrganization> context) {
        return context.defaultToSimple(
                new DefaultNDescriptorOrganizationBuilder(context.instance()), null
        );
    }

    @Override
    public NElement toElement(NElementSerializerContext<NDescriptorOrganization> context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorOrganizationBuilder(context.instance()), null
        );
    }

    @Override
    public NDescriptorOrganization toObject(NElementDeserializerContext context) {
        DefaultNDescriptorOrganizationBuilder builder = context.defaultToObject(context.element(), DefaultNDescriptorOrganizationBuilder.class);
        return new DefaultNDescriptorOrganizationBuilder(builder).build();
    }

}
