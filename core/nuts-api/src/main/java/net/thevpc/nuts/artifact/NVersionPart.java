package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NGetter;

/**
 * NVersionPart interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NVersionPart {
    /**
     * Type.
     *
     * @return type result
     */
    @NGetter
    NVersionPartType type();

    /**
     * Value.
     *
     * @return value result
     */
    @NGetter
    String value();
}
