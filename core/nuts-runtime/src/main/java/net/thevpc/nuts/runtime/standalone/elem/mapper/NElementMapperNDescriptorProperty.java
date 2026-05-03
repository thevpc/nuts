package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NDescriptorProperty;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorPropertyBuilder;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorProperty implements NElementMapper<NDescriptorProperty> {

    @Override
    public Object toSimple(NDescriptorProperty src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultToSimple(
                new DefaultNDescriptorPropertyBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptorProperty o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(
                new DefaultNDescriptorPropertyBuilder(o), null
        );
    }

    @Override
    public NDescriptorProperty createObject(NElementDeserializerContext context) {
        DefaultNDescriptorPropertyBuilder builder = context.defaultToObject(context.element(), DefaultNDescriptorPropertyBuilder.class);
        return new DefaultNDescriptorPropertyBuilder(builder).build();
    }

}
