package net.thevpc.nuts.spi;

import net.thevpc.nuts.log.NLogger;

import java.util.logging.Level;

/**
 * NLogSPI interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NLogSPI extends NLogger {
    /**
     * Checks if is loggable.
     *
     * @param level level
     * @return is loggable result
     */
    default boolean isLoggable(Level level){
        return true;
    }
}
