package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NDescriptorPropertyBuilder;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorPropertyBuilder;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorPropertyBuilder implements NElementMapper<NDescriptorPropertyBuilder> {

    @Override
    public Object toSimple(NDescriptorPropertyBuilder src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(
                new DefaultNDescriptorPropertyBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptorPropertyBuilder o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorPropertyBuilder(o), null
        );
    }

    @Override
    public NDescriptorPropertyBuilder createObject(NElementDeserializerContext context) {
        DefaultNDescriptorPropertyBuilder builder = context.defaultToObject(context.element(), DefaultNDescriptorPropertyBuilder.class);
        return new DefaultNDescriptorPropertyBuilder(builder);
    }

}
