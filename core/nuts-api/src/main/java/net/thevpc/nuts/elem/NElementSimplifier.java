package net.thevpc.nuts.elem;

@FunctionalInterface
public interface NElementSimplifier<T> {
    Object toSimple(NElementSerializerContext<T> context);
}
