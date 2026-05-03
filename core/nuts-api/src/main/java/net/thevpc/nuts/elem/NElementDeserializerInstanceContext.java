package net.thevpc.nuts.elem;

public interface NElementDeserializerInstanceContext<T> extends NElementFactoryContext {
    T instance();

    NElement element();

    Class<T> to();
}
