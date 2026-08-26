package net.thevpc.nuts.net;

import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

/**
 * NConnectionStringBuilderFactory interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NConnectionStringBuilderFactory extends NComponent {
    /**
     * Creates a new instance of create.
     *
     * @return create result
     */
    NConnectionStringBuilder create();

    /**
     * Creates a new instance of create.
     *
     * @param expression expression
     * @return create result
     */
    NOptional<NConnectionStringBuilder> create(String expression);
}
