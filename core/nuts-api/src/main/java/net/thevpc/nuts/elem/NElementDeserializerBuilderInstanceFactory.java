package net.thevpc.nuts.elem;

public interface NElementDeserializerBuilderInstanceFactory<T2> {
    T2 newInstance(NElementDeserializerBuilderFactoryContext<T2> context);
}
