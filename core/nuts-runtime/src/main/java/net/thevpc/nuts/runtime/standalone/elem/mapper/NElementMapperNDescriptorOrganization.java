package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorOrganizationBuilder;
import net.thevpc.nuts.artifact.NDescriptorOrganization;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorOrganization implements NElementMapper<NDescriptorOrganization> {

    @Override
    public Object toSimple(NDescriptorOrganization src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(
                new DefaultNDescriptorOrganizationBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptorOrganization o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorOrganizationBuilder(o), null
        );
    }

    @Override
    public NDescriptorOrganization createObject(NElementDeserializerContext context) {
        DefaultNDescriptorOrganizationBuilder builder = context.defaultToObject(context.element(), DefaultNDescriptorOrganizationBuilder.class);
        return new DefaultNDescriptorOrganizationBuilder(builder).build();
    }

}
