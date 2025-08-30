package net.thevpc.nuts.log;

import net.thevpc.nuts.util.NMsg;

import java.util.logging.Level;

public interface NLogSPI {
    default boolean isLoggable(Level level){
        return true;
    }

    void log(NMsg message);
}
