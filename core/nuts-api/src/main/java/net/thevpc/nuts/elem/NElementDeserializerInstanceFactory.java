package net.thevpc.nuts.elem;

public interface NElementDeserializerInstanceFactory<T> {
    T newInstance(NElementDeserializerContext context);
}
