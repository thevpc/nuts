package net.thevpc.nuts.elem;

public interface NElementDeserializerInitializer<T> {
    boolean initializeInstance(NElementDeserializerInstanceContext<T> context);
}
