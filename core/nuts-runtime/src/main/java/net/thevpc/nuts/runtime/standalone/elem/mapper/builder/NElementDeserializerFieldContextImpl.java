package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementDeserializerFieldContext;
import net.thevpc.nuts.runtime.standalone.elem.mapper.NElementFactoryContextAdapter;

import java.lang.reflect.Type;

class NElementDeserializerFieldContextImpl<T> extends NElementFactoryContextAdapter implements NElementDeserializerFieldContext<T> {
    private final T instance;
    private final NElement arg;
    private final NElement element;
    private final Type instanceType;

    public NElementDeserializerFieldContextImpl(T instance, NElement arg, NElement element, Type instanceType, NElementFactoryContext context) {
        super(context);
        this.instance = instance;
        this.arg = arg;
        this.element = element;
        this.instanceType = instanceType;
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
    public Type instanceType() {
        return instanceType;
    }
}
