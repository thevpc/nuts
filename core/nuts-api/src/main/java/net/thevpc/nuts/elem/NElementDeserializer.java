package net.thevpc.nuts.elem;

@FunctionalInterface
public interface NElementDeserializer<T> {
    T toObject(NElementDeserializerContext context);
}
