package net.thevpc.nuts.elem;

import net.thevpc.nuts.io.NReaderProvider;

/**
 * NCharStreamElement interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NCharStreamElement extends NElement{
    /**
     * Value.
     *
     * @return value result
     */
    NReaderProvider value();
    /**
     * Bloc identifier.
     *
     * @return bloc identifier result
     */
    String blocIdentifier();
    /**
     * Builder.
     *
     * @return builder result
     */
    NCharStreamElementBuilder builder();
}
