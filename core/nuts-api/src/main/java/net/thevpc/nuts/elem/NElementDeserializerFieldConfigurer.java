package net.thevpc.nuts.elem;

/**
 * NElementDeserializerFieldConfigurer interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDeserializerFieldConfigurer<T> {
    /**
     * Configure field.
     *
     * @param context context
     * @return configure field result
     */
    boolean configureField(NElementDeserializerFieldContext<T> context);
}
