package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementDeserializerBuilderFactoryContext;
import net.thevpc.nuts.runtime.standalone.elem.mapper.NElementFactoryContextAdapter;

import java.lang.reflect.Type;

class NElementDeserializerBuilderFactoryContextImpl<T> extends NElementFactoryContextAdapter implements NElementDeserializerBuilderFactoryContext<T> {
    private final NElement element;
    private final Type to;

    public NElementDeserializerBuilderFactoryContextImpl(NElement element, Type to, NElementFactoryContext context) {
        super(context);
        this.element = element;
        this.to = to;
    }

    @Override
    public NElement element() {
        return element;
    }

    @Override
    public Class<T> to() {
        return (Class<T>) to;
    }
}
