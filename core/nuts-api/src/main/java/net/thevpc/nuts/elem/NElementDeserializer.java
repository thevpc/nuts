package net.thevpc.nuts.elem;

/**
 * NElementDeserializer interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NElementDeserializer<T> {
    /**
     * Converts to object.
     *
     * @param context context
     * @return to object result
     */
    T toObject(NElementDeserializerContext context);
}
