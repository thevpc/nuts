package net.thevpc.nuts.spi;

import net.thevpc.nuts.log.NLogger;

import java.util.logging.Level;

public interface NLogSPI extends NLogger {
    default boolean isLoggable(Level level){
        return true;
    }
}
