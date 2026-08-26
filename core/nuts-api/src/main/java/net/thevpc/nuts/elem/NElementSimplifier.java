package net.thevpc.nuts.elem;

/**
 * NElementSimplifier interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@FunctionalInterface
public interface NElementSimplifier<T> {
    /**
     * Converts to simple.
     *
     * @param context context
     * @return to simple result
     */
    Object toSimple(NElementSerializerContext<T> context);
}
