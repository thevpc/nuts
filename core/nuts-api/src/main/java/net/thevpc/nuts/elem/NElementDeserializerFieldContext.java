package net.thevpc.nuts.elem;

public interface NElementDeserializerFieldContext<T> extends NElementFactoryContext {
    T instance();

    NElement field();

    NElement element();

    Class<T> to();
}
