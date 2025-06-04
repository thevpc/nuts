package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorOrganizationBuilder;
import net.thevpc.nuts.NDescriptorOrganization;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorOrganization implements NElementMapper<NDescriptorOrganization> {

    @Override
    public Object destruct(NDescriptorOrganization src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(
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
    public NDescriptorOrganization createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        DefaultNDescriptorOrganizationBuilder builder = (DefaultNDescriptorOrganizationBuilder) context.defaultCreateObject(o, DefaultNDescriptorOrganizationBuilder.class);
        return new DefaultNDescriptorOrganizationBuilder(builder).build();
    }

}
