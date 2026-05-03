package net.thevpc.nuts.elem;

@FunctionalInterface
public interface NElementDeserializer<T> {
    T createObject(NElementDeserializerContext context);
}
