package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorLicenseBuilder;
import net.thevpc.nuts.NDescriptorLicense;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorLicense implements NElementMapper<NDescriptorLicense> {

    @Override
    public Object destruct(NDescriptorLicense src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(
                new DefaultNDescriptorLicenseBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptorLicense o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(
                new DefaultNDescriptorLicenseBuilder(o), null
        );
    }

    @Override
    public NDescriptorLicense createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        DefaultNDescriptorLicenseBuilder builder = (DefaultNDescriptorLicenseBuilder) context.defaultElementToObject(o, DefaultNDescriptorLicenseBuilder.class);
        return new DefaultNDescriptorLicenseBuilder(builder).build();
    }

}
