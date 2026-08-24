package net.thevpc.nuts.spi;

/**
 * NAppResolverSPI interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NAppResolverSPI extends NComponent{
    /**
     * Resolve current application.
     *
     * @return resolve current application result
     */
    Object resolveCurrentApplication();
}
