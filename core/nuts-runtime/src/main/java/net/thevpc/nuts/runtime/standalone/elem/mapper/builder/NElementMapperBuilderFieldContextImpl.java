package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapperBuilderFieldContext;
import net.thevpc.nuts.runtime.standalone.elem.mapper.NElementFactoryContextAdapter;

class NElementMapperBuilderFieldContextImpl<T> extends NElementFactoryContextAdapter implements NElementMapperBuilderFieldContext<T> {
    private final T instance;
    private final NElement arg;
    private final NElement element;
    private final Class<T> to;

    public NElementMapperBuilderFieldContextImpl(T instance, NElement arg, NElement element, Class<T> to, NElementFactoryContext context) {
        super(context);
        this.instance = instance;
        this.arg = arg;
        this.element = element;
        this.to = to;
    }

    @Override
    public T instance() {
        return instance;
    }

    @Override
    public NElement field() {
        return arg;
    }

    @Override
    public NElement element() {
        return element;
    }

    @Override
    public Class<T> to() {
        return to;
    }
}
