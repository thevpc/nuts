package net.thevpc.nuts.elem;

/**
 * NElementSerializer interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NElementSerializer<T> {
    /**
     * Converts to element.
     *
     * @param context context
     * @return to element result
     */
    NElement toElement(NElementSerializerContext<T> context);
}
