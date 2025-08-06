package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapperBuilderInstanceContext;
import net.thevpc.nuts.runtime.standalone.elem.mapper.NElementFactoryContextAdapter;

import java.lang.reflect.Type;

class NElementMapperBuilderInstanceContextImpl<T> extends NElementFactoryContextAdapter implements NElementMapperBuilderInstanceContext<T> {
    private final T finalInstance;
    private final NElement element;
    private final Type to;

    public NElementMapperBuilderInstanceContextImpl(T finalInstance, NElement element, Type to, NElementFactoryContext context) {
        super(context);
        this.finalInstance = finalInstance;
        this.element = element;
        this.to = to;
    }

    @Override
    public T instance() {
        return finalInstance;
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
