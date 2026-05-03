package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorLicenseBuilder;
import net.thevpc.nuts.artifact.NDescriptorLicense;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorLicense implements NElementMapper<NDescriptorLicense> {

    @Override
    public Object toSimple(NElementSerializerContext<NDescriptorLicense> context) {
        return context.defaultToSimple(
                new DefaultNDescriptorLicenseBuilder(context.instance()), null
        );
    }

    @Override
    public NElement toElement(NElementSerializerContext<NDescriptorLicense> context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorLicenseBuilder(context.instance()), null
        );
    }

    @Override
    public NDescriptorLicense toObject(NElementDeserializerContext context) {
        DefaultNDescriptorLicenseBuilder builder = (DefaultNDescriptorLicenseBuilder) context.defaultToObject(context.element(), DefaultNDescriptorLicenseBuilder.class);
        return new DefaultNDescriptorLicenseBuilder(builder).build();
    }

}
