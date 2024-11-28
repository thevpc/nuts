package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.DefaultNDescriptorPropertyBuilder;

import java.lang.reflect.Type;

public class NElementMapperNDescriptorPropertyBuilder implements NElementMapper<NDescriptorPropertyBuilder> {

    @Override
    public Object destruct(NDescriptorPropertyBuilder src, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultDestruct(
                new DefaultNDescriptorPropertyBuilder(src), null
        );
    }

    @Override
    public NElement createElement(NDescriptorPropertyBuilder o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(
                new DefaultNDescriptorPropertyBuilder(o), null
        );
    }

    @Override
    public NDescriptorPropertyBuilder createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        DefaultNDescriptorPropertyBuilder builder = context.defaultElementToObject(o, DefaultNDescriptorPropertyBuilder.class);
        return new DefaultNDescriptorPropertyBuilder(builder);
    }

}
