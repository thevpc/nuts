package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.runtime.standalone.elem.mapper.NElementFactoryContextAdapter;

import java.lang.reflect.Type;

public class NElementDeserializerContextImpl<T> extends NElementFactoryContextAdapter implements NElementDeserializerContext {
    private final NElement element;
    private final Type to;

    public static <T> NElementDeserializerContextImpl<T> of(NElement element, Type to, NElementFactoryContext context){
        return new NElementDeserializerContextImpl<>(element, to, context);
    }
    public NElementDeserializerContextImpl(NElement element, Type to, NElementFactoryContext context) {
        super(context);
        this.element = element;
        this.to = to;
    }

    @Override
    public NElement element() {
        return element;
    }

    @Override
    public Type instanceType() {
        return to;
    }
}
