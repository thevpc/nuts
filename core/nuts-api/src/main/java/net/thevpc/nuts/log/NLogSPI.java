package net.thevpc.nuts.log;

import java.util.logging.Level;

public interface NLogSPI extends NLogger{
    default boolean isLoggable(Level level){
        return true;
    }
}
