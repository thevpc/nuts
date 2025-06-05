package net.thevpc.nuts.elem;

public interface NElementMapperBuilderInstanceFactory<T2> {
    T2 newInstance(NElementMapperBuilderFactoryContext<T2> context);
}
