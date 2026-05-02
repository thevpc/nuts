package net.thevpc.nuts.elem;

public interface NElementDeserializerBuilderInstanceContext<T2> extends NElementFactoryContext {
    T2 instance();

    NElement element();

    Class<T2> to();
}
