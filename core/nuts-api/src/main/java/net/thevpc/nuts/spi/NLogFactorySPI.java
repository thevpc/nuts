package net.thevpc.nuts.spi;

/**
 * NLogFactorySPI interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NLogFactorySPI extends NComponent {
    /**
     * Returns the log spi.
     *
     * @param name name
     * @return get log spi result
     */
    NLogSPI getLogSPI(String name);
}
