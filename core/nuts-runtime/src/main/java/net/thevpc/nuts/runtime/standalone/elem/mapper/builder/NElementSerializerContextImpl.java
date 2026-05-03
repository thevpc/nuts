package net.thevpc.nuts.runtime.standalone.elem.mapper.builder;

import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.runtime.standalone.elem.mapper.NElementFactoryContextAdapter;

import java.lang.reflect.Type;

public class NElementSerializerContextImpl<T> extends NElementFactoryContextAdapter implements NElementSerializerContext<T> {
    private final T instance;
    private final Type instanceType;

    public static <T> NElementSerializerContextImpl<T> of(T instance, Type to, NElementFactoryContext context){
        return new NElementSerializerContextImpl<>(instance, to, context);
    }
    public NElementSerializerContextImpl(T instance, Type instanceType, NElementFactoryContext context) {
        super(context);
        this.instance = instance;
        this.instanceType = instanceType;
    }


    @Override
    public T instance() {
        return instance;
    }

    @Override
    public Type instanceType() {
        return instanceType;
    }
}
