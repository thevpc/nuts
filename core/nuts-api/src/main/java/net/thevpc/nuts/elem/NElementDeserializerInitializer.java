package net.thevpc.nuts.elem;

/**
 * NElementDeserializerInitializer interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementDeserializerInitializer<T> {
    /**
     * Initialize instance.
     *
     * @param context context
     * @return initialize instance result
     */
    boolean initializeInstance(NElementDeserializerInstanceContext<T> context);
}
