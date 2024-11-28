package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorPropertyBuilder;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorProperty implements NElementMapper<NDescriptorProperty> {

    @Override
    public Object destruct(NDescriptorProperty src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(
                new DefaultNDescriptorPropertyBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptorProperty o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(
                new DefaultNDescriptorPropertyBuilder(o), null
        );
    }

    @Override
    public NDescriptorProperty createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        DefaultNDescriptorPropertyBuilder builder = (DefaultNDescriptorPropertyBuilder) context.defaultElementToObject(o, DefaultNDescriptorPropertyBuilder.class);
        return new DefaultNDescriptorPropertyBuilder(builder).build();
    }

}
