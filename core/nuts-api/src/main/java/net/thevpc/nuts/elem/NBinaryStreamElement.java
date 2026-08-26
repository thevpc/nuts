package net.thevpc.nuts.elem;

import net.thevpc.nuts.io.NInputStreamProvider;

/**
 * NBinaryStreamElement interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NBinaryStreamElement extends NElement{
    /**
     * Value.
     *
     * @return value result
     */
    NInputStreamProvider value();
    /**
     * Builder.
     *
     * @return builder result
     */
    NBinaryStreamElementBuilder builder();

    /**
     * Bloc identifier.
     *
     * @return bloc identifier result
     */
    String blocIdentifier();
}
