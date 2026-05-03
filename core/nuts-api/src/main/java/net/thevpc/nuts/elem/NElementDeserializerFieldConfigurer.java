package net.thevpc.nuts.elem;

public interface NElementDeserializerFieldConfigurer<T> {
    boolean prepareField(NElementDeserializerFieldContext<T> context);
}
