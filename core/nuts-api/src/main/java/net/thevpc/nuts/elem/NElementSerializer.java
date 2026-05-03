package net.thevpc.nuts.elem;

@FunctionalInterface
public interface NElementSerializer<T> {
    NElement toElement(NElementSerializerContext<T> context);
}
