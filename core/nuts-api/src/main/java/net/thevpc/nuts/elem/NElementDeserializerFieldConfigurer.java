package net.thevpc.nuts.elem;

public interface NElementDeserializerFieldConfigurer<T> {
    boolean configureField(NElementDeserializerFieldContext<T> context);
}
