package net.thevpc.nuts.elem;

public interface NElementMapperBuilderFieldContext<T2> extends NElementFactoryContext {
    T2 instance();

    NElement field();

    NElement element();

    Class<T2> to();
}
