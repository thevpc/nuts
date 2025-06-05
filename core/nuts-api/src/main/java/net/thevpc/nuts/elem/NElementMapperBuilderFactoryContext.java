package net.thevpc.nuts.elem;

public interface NElementMapperBuilderFactoryContext<T2> extends NElementFactoryContext {
    NElement element();

    Class<T2> to();
}
