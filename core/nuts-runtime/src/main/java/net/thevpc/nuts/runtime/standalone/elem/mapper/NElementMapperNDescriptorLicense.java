package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorLicenseBuilder;
import net.thevpc.nuts.artifact.NDescriptorLicense;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorLicense implements NElementMapper<NDescriptorLicense> {

    @Override
    public Object toSimple(NDescriptorLicense src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(
                new DefaultNDescriptorLicenseBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptorLicense o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorLicenseBuilder(o), null
        );
    }

    @Override
    public NDescriptorLicense createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        DefaultNDescriptorLicenseBuilder builder = (DefaultNDescriptorLicenseBuilder) context.defaultToObject(o, DefaultNDescriptorLicenseBuilder.class);
        return new DefaultNDescriptorLicenseBuilder(builder).build();
    }

}
